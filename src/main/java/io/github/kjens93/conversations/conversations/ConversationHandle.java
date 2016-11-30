package io.github.kjens93.conversations.conversations;

import com.google.common.base.Throwables;
import io.github.kjens93.conversations.collections.UDPInbox;
import io.github.kjens93.conversations.communications.*;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.conversations.messages.MessageID;
import io.github.kjens93.funkier.ThrowingRunnable;
import io.github.kjens93.funkier.ThrowingSupplier;
import io.github.kjens93.promises.Promise;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.TimeUnit;


/**
 * Created by kjensen on 11/26/16.
 */
@Getter
@RequiredArgsConstructor
final class ConversationHandle implements ConversationActions {

    private final UDPCommunicator udpCommunicator;
    private final Queue<Envelope> messageSendHistory = new CircularFifoQueue<>(100);
    private final Queue<Envelope> messageReceiveHistory = new CircularFifoQueue<>(100);
    @Setter(AccessLevel.PACKAGE)
    private MessageID conversationId;
    @Setter(AccessLevel.PACKAGE)
    private UDPInbox inbox;
    private int reliableRetries = 3;
    private long reliableTimeout = 2000;
    private TimeUnit reliableTimeoutUnit = TimeUnit.MILLISECONDS;

    @Override
    public void send(Envelope envelope) {
        if(conversationId != null)
            envelope.getMessage().setConversationId(conversationId);
        MessageID messageId = udpCommunicator.send(envelope);
        if(conversationId == null)
            conversationId = messageId;
        if(inbox == null)
            inbox = udpCommunicator.inboxes().getOrNew(conversationId);
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
    public void setReliableRetries(int retries) {
        reliableRetries = retries;
    }

    @Override
    public <T extends Message> Envelope<T> reliableSend(Envelope envelope, Class<T> expectedResponseType) throws ReliabilityException {
        return retry(reliableRetries, () -> {
            send(envelope);
            return receiveOne()
                    .ofType(expectedResponseType)
                    .fromSender(envelope.getRemoteEndpoint())
                    .get(reliableTimeout, reliableTimeoutUnit);
        });
    }

    @Override
    public <T extends Message> Envelope<T> reliableSend(Message message, Endpoint recipient, Class<T> expectedResponseType) throws ReliabilityException {
        return retry(reliableRetries, () -> {
            send(message, recipient);
            return receiveOne()
                    .ofType(expectedResponseType)
                    .fromSender(recipient)
                    .get(reliableTimeout, reliableTimeoutUnit);
        });
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
        return new EnvelopeStreamImpl<>(messageReceiveHistory, inbox);
    }

    @Override
    public Promise<TCPConnection> openNewTCPConnection(Endpoint recipient) {
        return ((Promise<TCPConnection>)() -> {
            try {
                ServerSocket serverSocket = SocketFactory.createTCPServerSocket();
                String host = udpCommunicator.getEndpoint().getHostString();
                int port = serverSocket.getLocalPort();
                Message msg = new TCPConnectionOpenMessage(host, port);
                send(msg, recipient);
                serverSocket.setSoTimeout(10 * 60 * 1000);
                Socket socket = serverSocket.accept();
                return new TCPConnection(socket, serverSocket);
            } catch (IOException e) {
                Throwables.propagate(e);
                return null;
            }
        }).async(Throwable::printStackTrace);
    }

    @Override
    public Promise<TCPConnection> waitForTCPConnection(Endpoint initiator) {
        return ((Promise<TCPConnection>)() -> {
            try {
                int port = receiveOne()
                        .ofType(TCPConnectionOpenMessage.class)
                        .fromSender(initiator)
                        .get()
                        .getMessage()
                        .getPort();
                Socket socket = SocketFactory.createTCPConnectionSocket();
                Endpoint newEndpoint = new Endpoint(initiator.getAddress(), port);
                socket.connect(newEndpoint);
                return new TCPConnection(socket);
            } catch(IOException e) {
                Throwables.propagate(e);
                return null;
            }
        }).async(Throwable::printStackTrace);
    }

    private void verifyConversationIdAndInbox() {
        if(conversationId == null)
            throw new NullPointerException("Conversation ID is null. Make sure you call send() at least once before you call receiveOne().");
        if(inbox == null)
            throw new NullPointerException("Inbox is null. Make sure you call send() at least once before you call receiveOne().");
    }

}
