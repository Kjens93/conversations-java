package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.conversations.security.SigningUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.security.PublicKey;

/**
 * Created by kjensen on 11/26/16.
 */
@Log
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class EnvelopeStreamImpl<T extends Message> implements EnvelopeStream<T> {

    private final ConversationHandle handle;
    private final Class<T> type;
    private final Endpoint sender;


    @Override
    public <V extends Message> EnvelopeStream<V> ofType(Class<V> type) {
        return new EnvelopeStreamImpl<>(handle, type, sender);
    }

    @Override
    public EnvelopeStream<T> fromSender(Endpoint sender) {
        return new EnvelopeStreamImpl<>(handle, type, sender);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Envelope<T> get() throws SecurityException {
        try {
            while (true) {
                if (handle.getInbox().isEmpty())
                    synchronized (handle.getInbox()) {
                        handle.getInbox().wait();
                    }
                Envelope env = handle.getInbox().poll();
                if (matches(env)) {
                    handle.getMessageReceiveHistory().add(env);
                    return env;
                } else {
                    handle.getInbox().add(env);
                }
            }
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean matches(Envelope envelope) {
        if(envelope == null)
            return false;
        Message msg = envelope.getMessage();
        Endpoint ep = envelope.getRemoteEndpoint();
        if(type != null && !type.isInstance(msg))
            return false;
        if(sender != null && !sender.equals(ep))
            return false;
        PublicKey key = handle.getPublicKeyForSender(ep);
        if(key != null)
            SigningUtils.verifyLoud(msg, key);
        return true;
    }

}
