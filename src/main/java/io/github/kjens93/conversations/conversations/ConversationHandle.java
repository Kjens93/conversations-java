package io.github.kjens93.conversations.conversations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.github.kjens93.conversations.collections.UDPInbox;
import io.github.kjens93.conversations.communications.*;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.conversations.messages.MessageID;
import io.github.kjens93.conversations.messages.Serializer;
import io.github.kjens93.conversations.security.PublicKeyRequestConversationForInitiator;
import io.github.kjens93.conversations.security.SigningUtils;
import io.github.kjens93.funkier.ThrowingRunnable;
import io.github.kjens93.funkier.ThrowingSupplier;
import io.github.kjens93.promises.Commitment;
import io.github.kjens93.promises.Promise;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * Created by kjensen on 11/26/16.
 */
class ConversationHandle implements ConversationActions {

    private final CommSubsystem subsystem;

    private final Map<Endpoint, PublicKey> foreignPublicKeys = new HashMap<>();

    private final PrivateKey privateKey;

    @Getter(AccessLevel.PACKAGE)
    private final UDPCommunicator udpCommunicator;

    @Getter(AccessLevel.PACKAGE)
    private final Queue<Envelope> messageSendHistory = new CircularFifoQueue<>(100);

    @Getter(AccessLevel.PACKAGE)
    private final Queue<Envelope> messageReceiveHistory = new CircularFifoQueue<>(100);

    @Getter(AccessLevel.PACKAGE)
    private MessageID conversationId;

    @Getter(AccessLevel.PUBLIC)
    private UDPInbox inbox;

    @Setter(AccessLevel.PUBLIC)
    private int reliableRetries = 3;
    private long reliableTimeout = 2000;
    private TimeUnit reliableTimeoutUnit = TimeUnit.MILLISECONDS;

    ConversationHandle(CommSubsystem subsystem) {
        this.subsystem = subsystem;
        this.udpCommunicator = subsystem.udpCommunicator();
        this.privateKey = subsystem.signingKeyPair().getPrivate();
    }

    ConversationHandle(CommSubsystem subsystem, MessageID conversationId) {
        this(subsystem);
        this.conversationId = conversationId;
        this.inbox = udpCommunicator.inboxes().getOrNew(conversationId);
    }

    @Override
    public void send(Envelope envelope) {
        if(conversationId != null) {
            envelope.getMessage().setConversationId(conversationId);
        }
        MessageID messageId = envelope.getMessage().getMessageId();
        if(messageId == null) {
            messageId = udpCommunicator.newMessageId();
            envelope.getMessage().setMessageId(messageId);
        }
        if(conversationId == null) {
            conversationId = messageId;
            inbox = udpCommunicator.inboxes().getOrNew(conversationId);
        }
        SigningUtils.sign(envelope.getMessage(), privateKey);
        udpCommunicator.send(envelope);
        messageSendHistory.add(envelope);
    }

    @Override
    public void send(Message message, Endpoint recipient) {
        send(new Envelope<>(message, recipient));
    }

    @Override
    public void setReliableTimeout(long timeout, TimeUnit unit) {
        reliableTimeout = timeout;
        reliableTimeoutUnit = unit;
    }

    @Override
    public ReliableSendStream reliableSend(Envelope envelope) {
        return new ReliableSendStreamImpl(this, envelope, reliableRetries, reliableTimeout, reliableTimeoutUnit);
    }

    @Override
    public ReliableSendStream reliableSend(Message message, Endpoint recipient) {
        return reliableSend(new Envelope<>(message, recipient));
    }

    @Override
    public void enableSignatureVerification(Endpoint peer) {
        if(!foreignPublicKeys.containsKey(peer)) {
            fillPublicKeyForSender(peer);
        }
    }

    @Override
    public void retry(int attempts, ThrowingRunnable runnable) throws ReliabilityException {
        ReliabilityException exception = new ReliabilityException("Could not complete the block after " + attempts + " attempts.");
        for(int i = 0; i < attempts; i++) {
            try {
                runnable.run();
                return;
            } catch(Exception e) {
                exception.addSuppressed(e);
            }
        }
        throw exception;
    }

    @Override
    public <T> T retry(int attempts, ThrowingSupplier<T> supplier) throws ReliabilityException {
        ReliabilityException exception = new ReliabilityException("Could not complete the block after " + attempts + " attempts.");
        for(int i = 0; i < attempts; i++) {
            try {
                return supplier.get();
            } catch(Exception e) {
                exception.addSuppressed(e);
            }
        }
        throw exception;
    }

    @Override
    public EnvelopeStream<Message> receiveOne() {
        verifyConversationIdAndInbox();
        return new EnvelopeStreamImpl<>(this, Message.class, null);
    }

    @Override
    public <S> Commitment sendViaTCP(S object, Endpoint recipient) {
        return () -> {
            try(TCPConnection conn = openNewTCPConnection(recipient).get()) {
                conn.writeUTF("OBJECT+SIG").flush();
                String ready = conn.readUTF();
                if(!ready.equalsIgnoreCase("READY"))
                    throw new IllegalStateException("Recipient is not ready. They responded with " + ready + " instead of READY.");
                byte[] bytes = Serializer.serialize(object);
                byte[] signature = SigningUtils.sign(bytes, privateKey);
                conn.writeInt(bytes.length).flush()
                        .write(bytes).flush()
                        .writeInt(signature.length).flush()
                        .write(signature).flush();
                String ack = conn.readUTF();
                if (!ready.equalsIgnoreCase("ACK"))
                    throw new IllegalStateException("Sending to recipient failed. They sent " + ack + " instead of ACK.");
            }
        };
    }

    @Override
    public <S> Promise<S> receiveViaTCP(Class<S> clazz, Endpoint initiator) {
        return () -> {
            try(TCPConnection conn = waitForTCPConnection(initiator).get()) {
                String ready = conn.readUTF();
                if (!ready.equalsIgnoreCase("OBJECT+SIG"))
                    throw new IllegalStateException("Recipient is not sending an object. They sent " + ready + " instead of OBJECT+SIG.");
                conn.writeUTF("READY").flush();
                try {
                    int size = conn.readInt();
                    byte[] bytes = new byte[size];
                    conn.readFully(bytes);
                    S result = new ObjectMapper().readerFor(clazz).readValue(bytes);
                    if(foreignPublicKeys.containsKey(initiator)) {
                        int sigSize = conn.readInt();
                        byte[] signature = new byte[sigSize];
                        conn.readFully(signature);
                        SigningUtils.verifyLoud(bytes, signature, foreignPublicKeys.get(initiator));
                    }
                    conn.writeUTF("ACK").flush();
                    return result;
                } catch (Exception e) {
                    conn.writeUTF("ERR").flush();
                    Throwables.propagate(e);
                    throw new RuntimeException("Unexpected", e);
                }
            }
        };
    }

    @Override
    public Promise<TCPConnection> openNewTCPConnection(Endpoint recipient) {
        return ((Promise<TCPConnection>)() -> {
            try {
                ServerSocket serverSocket = SocketFactory.createTCPServerSocket();
                int port = serverSocket.getLocalPort();
                Message msg = new TCPConnectionOpenMessage(port);
                send(msg, recipient);
                serverSocket.setSoTimeout(10 * 60 * 1000);
                Socket socket = serverSocket.accept();
                System.out.println("Accepted TCP connection from remote " + socket.getRemoteSocketAddress() + " on local port " + socket.getLocalPort());
                return new TCPConnection(socket, serverSocket);
            } catch (IOException e) {
                Throwables.propagate(e);
                return null;
            }
        }).async();
    }

    @Override
    public Promise<TCPConnection> waitForTCPConnection(Endpoint initiator) {
        return ((Promise<TCPConnection>)() -> {
            try {
                Endpoint newEndpoint = receiveOne()
                        .ofType(TCPConnectionOpenMessage.class)
                        .fromSender(initiator)
                        .map(msg -> {
                            InetAddress host = initiator.getAddress();
                            int port = msg.getMessage().getPort();
                            return new Endpoint(host, port);
                        }).get();
                Socket socket = SocketFactory.createTCPConnectionSocket();
                socket.connect(newEndpoint);
                System.out.println("Connected TCP socket on local port " + socket.getLocalPort() + " to remote " + newEndpoint);
                return new TCPConnection(socket);
            } catch(IOException e) {
                Throwables.propagate(e);
                throw new RuntimeException(e);
            }
        }).async();
    }

    PublicKey getPublicKeyForSender(Endpoint peer) {
        return foreignPublicKeys.get(peer);
    }

    private void fillPublicKeyForSender(Endpoint peer) {
        System.out.println("Endpoint: " + subsystem.getUdpEndpoint() + " is trying to get the public key from: " + peer);
        PublicKeyRequestConversationForInitiator conversation = new PublicKeyRequestConversationForInitiator(peer);
        subsystem.newConversation(conversation).await();
        foreignPublicKeys.put(peer, conversation.getResponse());
    }

    private void verifyConversationIdAndInbox() {
        if(conversationId == null)
            throw new NullPointerException("Conversation ID is null. Make sure you call send() at least once before you call receiveOne().");
        if(inbox == null)
            throw new NullPointerException("Inbox is null. Make sure you call send() at least once before you call receiveOne().");
    }

}
