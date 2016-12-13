package io.github.kjens93.conversations.communications;

import io.github.kjens93.conversations.collections.UDPInbox;
import io.github.kjens93.conversations.collections.UDPInboxRegistry;
import io.github.kjens93.conversations.conversations.ConversationFactory;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.conversations.messages.MessageID;
import io.github.kjens93.conversations.messages.Serializer;
import io.github.kjens93.promises.Commitment;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;

/**
 * Created by kjensen on 11/27/16.
 */
@Log
public class UDPCommunicator {

    private final int processId;
    private short messageIdCounter = 0;
    private final ConversationFactory conversationFactory;
    private final UDPInboxRegistry inboxes = UDPInboxRegistry.newInstance();
    private final DatagramSocket socket = SocketFactory.createUDPSocket();
    private static final InetAddress localhost = InetAddress.getLoopbackAddress();

    public UDPCommunicator(ConversationFactory conversationFactory, int processId) {
        this.conversationFactory = conversationFactory;
        this.processId = processId;
        startReceiving();
    }

    public UDPInboxRegistry inboxes() {
        return inboxes;
    }

    public void send(Envelope envelope) {
        try {
            Message message = envelope.getMessage();
            if(message.getMessageId() == null || message.getConversationId() == null) {
                throw new IllegalStateException("Missing MessageID or ConversationID.");
            }
            DatagramPacket packet = envelope.toDatagramPacket();
            socket.send(packet);
            log.log(Level.INFO, "    Sent: " + message.getClass().getSimpleName() + " to: " + envelope.getRemoteEndpoint());
            log.log(Level.FINE, "    Sent: " + new String(packet.getData()) + " to: " + envelope.getRemoteEndpoint());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Endpoint getEndpoint() {
        return new Endpoint(localhost, socket.getLocalPort());
    }

    public MessageID newMessageId() {
        return new MessageID(processId, ++messageIdCounter);
    }

    private void startReceiving() {
        ((Commitment)() -> {
            while (!socket.isClosed()) {
                try {
                    socket.setSoTimeout(500);
                    socket.setReceiveBufferSize(64000);
                    byte[] buffer = new byte[64000];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    receive(packet);
                } catch (SocketException e) {
                    if (!e.getMessage().contains("closed"))
                        throw new RuntimeException(e);
                } catch (IOException e) {
                    if (!e.getMessage().contains("timed out"))
                        throw new RuntimeException(e);
                }
            }
        }).async(Throwable::printStackTrace);
    }

    private void receive(DatagramPacket packet) {
        Message message = Serializer.deserialize(Message.class, packet.getData());
        Endpoint remote = new Endpoint(packet.getAddress(), packet.getPort());
        log.log(Level.INFO, "Received: " + message.getClass().getSimpleName() + " from: " + remote);
        log.log(Level.FINE, "Received: " + new String(packet.getData()) + " from: " + remote);
        receive(new Envelope<>(message, remote));
    }

    private void receive(Envelope envelope) {
        MessageID conversationId = envelope.getMessage().getConversationId();
        if(inboxes.containsKey(conversationId)) {
            UDPInbox inbox = inboxes.get(conversationId);
            inbox.add(envelope);
        } else {
            conversationFactory.startConversationOnNewThread(envelope, this);
        }
    }

}
