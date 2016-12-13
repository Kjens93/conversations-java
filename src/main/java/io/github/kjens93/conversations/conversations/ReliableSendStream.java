package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.promises.Promise;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by kjensen on 12/12/16.
 */
public interface ReliableSendStream {

    <T extends Message> Step2<T> expecting(Class<T> clazz);

    interface Step2<T extends Message> extends Promise<Envelope<T>> {

        Step2<T> withReliableRetries(int retries);
        Step2<T> withReliableTimeouts(long timeout, TimeUnit unit);
        Envelope<T> get() throws SecurityException, ReliabilityException;
        default Envelope<T> get(long timeout, TimeUnit unit) throws SecurityException, ReliabilityException, TimeoutException {
            return Promise.super.get(timeout, unit);
        }
        default void await() throws SecurityException, ReliabilityException {
            Promise.super.await();
        }
        default void await(long timeout, TimeUnit unit) throws SecurityException, ReliabilityException, TimeoutException {
            Promise.super.await(timeout, unit);
        }

    }

}
