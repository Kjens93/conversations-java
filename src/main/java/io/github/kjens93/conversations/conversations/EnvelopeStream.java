package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by kjensen on 11/27/16.
 */
public interface EnvelopeStream<T extends Message> {

    <V extends Message> EnvelopeStream<V> ofType(Class<V> type);

    EnvelopeStream<T> fromSender(Endpoint sender);

    Envelope<T> get(long timeout, TimeUnit unit) throws TimeoutException;

    Envelope<T> get();

    void await(long timeout, TimeUnit unit) throws TimeoutException;

    void await();

}

