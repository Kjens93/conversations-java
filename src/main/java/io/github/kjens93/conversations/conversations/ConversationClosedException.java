package io.github.kjens93.conversations.conversations;

/**
 * Created by kjensen on 12/13/16.
 */
public class ConversationClosedException extends RuntimeException {

    public ConversationClosedException() {
        super();
    }

    public ConversationClosedException(String message) {
        super(message);
    }

    public ConversationClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConversationClosedException(Throwable cause) {
        super(cause);
    }

    public ConversationClosedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
