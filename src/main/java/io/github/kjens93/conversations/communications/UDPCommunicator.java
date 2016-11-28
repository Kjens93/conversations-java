package io.github.kjens93.conversations.communications;

import com.jezhumble.javasysmon.JavaSysMon;
import io.github.kjens93.conversations.collections.UDPInbox;
import io.github.kjens93.conversations.collections.UDPInboxRegistry;
import io.github.kjens93.conversations.conversations.ConversationFactory;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.conversations.messages.MessageID;
import io.github.kjens93.conversations.messages.Serializer;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;

import static io.github.kjens93.async.Async.async;

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

    public UDPCommunicator(ConversationFactory conversationFactory) {
        this(conversationFactory, new JavaSysMon().currentPid());
    }

    public UDPCommunicator(ConversationFactory conversationFactory, int processId) {
        this.conversationFactory = conversationFactory;
        this.processId = processId;
        startReceiving();
    }

    public UDPInboxRegistry inboxes() {
        return inboxes;
    }

    public MessageID send(Envelope envelope) {
        try {
            Message message = envelope.getMessage();
            if(message.getMessageId() == null || message.getConversationId() == null) {
                MessageID id = newMessageId();
                message.setMessageId(id);
            }
            DatagramPacket packet = envelope.toDatagramPacket();
            socket.send(packet);
            log.log(Level.INFO, "    Sent: " + envelope.getMessage() + " to: " + envelope.getRemoteEndpoint());
            return message.getMessageId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Endpoint getEndpoint() {
        return new Endpoint(localhost, socket.getLocalPort());
    }

    private MessageID newMessageId() {
        return new MessageID(processId, ++messageIdCounter);
    }

    private void startReceiving() {
        async(() -> {
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
        });
    }

    private void receive(DatagramPacket packet) {
        Message message = Serializer.deserialize(Message.class, packet.getData());
        Endpoint remote = new Endpoint(packet.getAddress(), packet.getPort());
        receive(new Envelope<>(message, remote));
    }

    private void receive(Envelope envelope) {
        log.log(Level.INFO, "Received: " + envelope.getMessage() + " from: " + envelope.getRemoteEndpoint());
        MessageID conversationId = envelope.getMessage().getConversationId();
        if(inboxes.containsKey(conversationId)) {
            UDPInbox inbox = inboxes.get(conversationId);
            inbox.add(envelope);
        } else {
            conversationFactory.startConversationOnNewThread(envelope, this);
        }
    }

}
