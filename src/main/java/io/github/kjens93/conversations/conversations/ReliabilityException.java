package io.github.kjens93.conversations.conversations;

/**
 * Created by kjensen on 11/27/16.
 */
public class ReliabilityException extends RuntimeException {

    public ReliabilityException() {
    }

    public ReliabilityException(String message) {
        super(message);
    }

    public ReliabilityException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReliabilityException(Throwable cause) {
        super(cause);
    }

    public ReliabilityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
