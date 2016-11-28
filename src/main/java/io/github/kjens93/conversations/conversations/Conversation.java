package io.github.kjens93.conversations.conversations;

/**
 * Created by kjensen on 11/26/16.
 */
@FunctionalInterface
public interface Conversation {

    void run(ConversationActions actions) throws Exception;

}
