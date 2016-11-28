package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.collections.NotifyingQueue;
import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.funkier.ThrowingSupplier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.github.kjens93.async.Timeout.timeout;

/**
 * Created by kjensen on 11/26/16.
 */
@Log
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class EnvelopeStreamImpl<T extends Message> implements EnvelopeStream<T> {

    private final Queue<Envelope> history;
    private final NotifyingQueue<Envelope> source;
    private Class<T> type;
    private Endpoint sender;


    @Override
    public <V extends Message> EnvelopeStream<V> ofType(Class<V> type) {
        return new EnvelopeStreamImpl<>(history, source, type, sender);
    }

    @Override
    public EnvelopeStream<T> fromSender(Endpoint sender) {
        return new EnvelopeStreamImpl<>(history, source, type, sender);
    }

    @Override
    public Envelope<T> get(long timeout, TimeUnit unit) throws TimeoutException {
        return timeout(timeout, unit, (ThrowingSupplier<Envelope<T>>) this::get);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Envelope<T> get() {
        try {
            while (true) {
                if (source.isEmpty())
                    synchronized (source) {
                        source.wait();
                    }
                Envelope env = source.poll();
                if (matches(env)) {
                    history.add(env);
                    return env;
                } else {
                    source.add(env);
                }
            }
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void await(long timeout, TimeUnit unit) throws TimeoutException {
        get(timeout, unit);
    }

    @Override
    public void await() {
        get();
    }

    private boolean matches(Envelope envelope) {
        if(envelope == null)
            return false;
        if(type != null && !type.isInstance(envelope.getMessage()))
            return false;
        if(sender != null && !sender.equals(envelope.getRemoteEndpoint()))
            return false;
        return true;
    }

}
