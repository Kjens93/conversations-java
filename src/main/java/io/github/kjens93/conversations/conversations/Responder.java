package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;

import java.util.concurrent.TimeUnit;

/**
 * Created by kjensen on 11/26/16.
 */
@FunctionalInterface
public interface Responder<T extends Message> extends Conversation {

    void respond(ConversationActions actions, Envelope<T> initialMessage) throws Exception;

    @Override
    @SuppressWarnings("unchecked")
    default void run(ConversationActions actions) throws Exception {
        Envelope envelope = actions.receiveOne()
                .get(1, TimeUnit.SECONDS);
        respond(actions, envelope);
    }

}
