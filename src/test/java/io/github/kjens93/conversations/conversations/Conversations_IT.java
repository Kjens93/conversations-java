package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.TestMessage;
import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.messages.Message;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by kjensen on 11/30/16.
 */
public class Conversations_IT {

    private CommSubsystem s2;
    private Endpoint ep2;

    @Before
    public void setup() {
        s2 = new CommSubsystem(9999);
        ep2 = s2.getUdpEndpoint();
    }

    @Test
    public void test() {

        AtomicBoolean responderCalled = new AtomicBoolean(false);

        s2.registerResponder(TestMessage.class, (actions, initialMessage) -> {
            actions.send(new TestMessage(), initialMessage.getRemoteEndpoint());
            responderCalled.set(true);
        });

        Conversations.newConversation(actions -> {
            actions.send(new TestMessage(), ep2);
            actions.receiveOne()
                    .ofType(Message.class)
                    .fromSender(ep2)
                    .await(500, TimeUnit.MILLISECONDS);
        }).await();

        assertThat(responderCalled.get()).isTrue();

    }

}
