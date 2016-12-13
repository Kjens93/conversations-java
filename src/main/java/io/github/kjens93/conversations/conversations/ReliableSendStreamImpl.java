package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * Created by kjensen on 12/12/16.
 */
@RequiredArgsConstructor
class ReliableSendStreamImpl implements ReliableSendStream {

    private final ConversationHandle source;
    private final Envelope envelope;
    private final int retries;
    private final long timeout;
    private final TimeUnit unit;


    @Override
    public <T extends Message> Step2<T> expecting(Class<T> clazz) {
        return new Step2Impl<>(source, envelope, clazz, retries, timeout, unit);
    }

    @RequiredArgsConstructor
    private static class Step2Impl<T extends Message> implements Step2<T> {

        private final ConversationHandle handle;
        private final Envelope envelope;
        private final Class<T> expectedReturnType;
        private final int retries;
        private final long timeout;
        private final TimeUnit unit;

        @Override
        public Step2Impl<T> withReliableRetries(int retries) {
            return new Step2Impl<>(handle, envelope, expectedReturnType, retries, timeout, unit);
        }

        @Override
        public Step2Impl<T> withReliableTimeouts(long timeout, TimeUnit unit) {
            return new Step2Impl<>(handle, envelope, expectedReturnType, retries, timeout, unit);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Envelope<T> get() throws SecurityException, ReliabilityException {
            return handle.retry(retries, () -> {
                handle.send(envelope);
                return handle.receiveOne()
                        .fromSender(envelope.getRemoteEndpoint())
                        .ofType(expectedReturnType)
                        .get(timeout, unit);
            });
        }

    }

}
