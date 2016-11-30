package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.communications.TCPConnection;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.funkier.ThrowingRunnable;
import io.github.kjens93.funkier.ThrowingSupplier;
import io.github.kjens93.promises.Commitment;
import io.github.kjens93.promises.Promise;

import java.util.concurrent.TimeUnit;

/**
 * Created by kjensen on 11/26/16.
 */
public interface ConversationActions {

    void send(Envelope envelope);

    void send(Message message, Endpoint recipient);

    void setReliableRetries(int retries);

    void setReliableTimeout(long timeout, TimeUnit unit);

    <T extends Message> Envelope<T> reliableSend(Envelope envelope, Class<T> expectedResponseType) throws ReliabilityException;

    <T extends Message> Envelope<T> reliableSend(Message message, Endpoint recipient, Class<T> expectedResponseType) throws ReliabilityException;

    void retry(int attempts, ThrowingRunnable runnable) throws Exception;

    <T> T retry(int attempts, ThrowingSupplier<T> supplier) throws Exception;

    EnvelopeStream<Message> receiveOne();

    Promise<TCPConnection> openNewTCPConnection(Endpoint recipient);

    Promise<TCPConnection> waitForTCPConnection(Endpoint initiator);

    <S> Commitment sendViaTCP(S object, Endpoint recipient) throws ReliabilityException;

    <S> Promise<S> receiveViaTCP(Class<S> clazz, Endpoint initiator);

}
