package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.promises.Commitment;

/**
 * Created by kjensen on 11/30/16.
 */
public interface Conversations {

    static <T extends Message> void registerResponder(Class<T> clazz, Responder<T> responder) {
        CommSubsystem.staticInstance().registerResponder(clazz, responder);
    }

    static Commitment newConversation(Conversation conversation) {
        return CommSubsystem.staticInstance().newConversation(conversation);
    }

}
