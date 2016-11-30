package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.promises.Promise;

/**
 * Created by kjensen on 11/27/16.
 */
public interface EnvelopeStream<T extends Message> extends Promise<Envelope<T>> {

    <V extends Message> EnvelopeStream<V> ofType(Class<V> type);

    EnvelopeStream<T> fromSender(Endpoint sender);
}

