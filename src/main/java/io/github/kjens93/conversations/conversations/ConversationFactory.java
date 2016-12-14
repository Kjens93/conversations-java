package io.github.kjens93.conversations.conversations;

import com.google.common.base.Throwables;
import io.github.kjens93.conversations.collections.UDPInbox;
import io.github.kjens93.conversations.communications.UDPCommunicator;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.conversations.messages.MessageID;
import io.github.kjens93.promises.Commitment;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Created by kjensen on 11/27/16.
 */
@Log
@RequiredArgsConstructor
public class ConversationFactory {

    private final CommSubsystem subsystem;
    private final Map<Class, Responder> responders = new ConcurrentHashMap<>();

    public void startConversationOnNewThread(Envelope envelope, UDPCommunicator udpCommunicator) {
        MessageID conversationId = envelope.getMessage().getConversationId();
        UDPInbox inbox = udpCommunicator.inboxes().getOrNew(conversationId);
        inbox.add(envelope);
        Responder responder = responders.get(envelope.getMessage().getClass());
        if(responder == null)
            throw new IllegalStateException("Responder not registered for incoming message: " + envelope);
        ConversationHandle handle = new ConversationHandle(subsystem, conversationId);
        ((Commitment)() -> {
            try {
                responder.run(handle);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Exception occurred in responder ["
                        + responder.getClass().getSimpleName() + "] with conversation id: ["
                        + handle.getConversationId() + "]", e);
                Throwables.propagate(e);
            } finally {
                handle.close();
            }
        }).async();
    }

    public <T extends Message> void registerResponder(Class<T> clazz, Responder<T> responder) {
        responders.put(clazz, responder);
    }

}
