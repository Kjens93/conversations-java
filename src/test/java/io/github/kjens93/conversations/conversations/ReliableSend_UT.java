package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.communications.UDPCommunicator;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static io.github.kjens93.async.Async.async;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by kjensen on 11/27/16.
 */
public class ReliableSend_UT {

    private static final UDPCommunicator udpCommunicator = new CommSubsystem(1).udpCommunicator();
    private final Endpoint ep = new Endpoint("127.0.0.1", 12345);
    private ConversationHandle handle;

    @Before
    public void setup() {
        handle = new ConversationHandle(udpCommunicator);
        handle.setReliableRetries(3);
        handle.setReliableTimeout(500, TimeUnit.MILLISECONDS);
    }

    @Test
    @Ignore
    public void test_reliableSend_success_attempt1() throws ReliabilityException {

        async(() -> {
            Thread.sleep(500);
            handle.getInbox().add(new Envelope<>(new Message(), ep));
        });

        handle.reliableSend(new Message(), ep, Message.class);

    }

    @Test
    @Ignore
    public void test_reliableSend_success_attempt2() throws ReliabilityException {

        async(() -> {
            Thread.sleep(1000);
            handle.getInbox().add(new Envelope<>(new Message(), ep));
        });

        handle.reliableSend(new Message(), ep, Message.class);

    }

    @Test
    @Ignore
    public void test_reliableSend_success_attempt3() throws ReliabilityException {

        async(() -> {
            Thread.sleep(1500);
            handle.getInbox().add(new Envelope<>(new Message(), ep));
        });

        handle.reliableSend(new Message(), ep, Message.class);

    }

    @Test
    public void test_reliableSend_fail() {

        assertThatThrownBy(() ->{
            handle.reliableSend(new Message(), ep, Message.class);
        }).isInstanceOf(ReliabilityException.class);

    }

}
