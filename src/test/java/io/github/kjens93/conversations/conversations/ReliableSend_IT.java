package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.TestMessage;
import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.messages.Envelope;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by kjensen on 11/27/16.
 */
public class ReliableSend_IT {

    private CommSubsystem subsystem1;
    private CommSubsystem subsystem2;
    private ConversationHandle handle1;
    private Endpoint ep1;
    private Endpoint ep2;

    @Before
    public void setup() {
        subsystem1 = new CommSubsystem(1);
        subsystem2 = new CommSubsystem(2);
        handle1 = new ConversationHandle(subsystem1);
        handle1.setReliableRetries(3);
        handle1.setReliableTimeout(500, TimeUnit.MILLISECONDS);
        ep1 = subsystem1.getUdpEndpoint();
        ep2 = subsystem2.getUdpEndpoint();
    }

    @Test
    public void test_reliableSend_success_attept1() throws ReliabilityException {

        TestMessage m1 = new TestMessage();
        TestMessage m2 = new TestMessage();

        subsystem2.registerResponder(TestMessage.class, (actions, env) -> {
            actions.send(m2, ep1);
        });

        handle1.reliableSend(m1, ep2)
                .expecting(TestMessage.class)
                .await();

        assertThat(handle1.getMessageSendHistory())
                .extracting(Envelope::getMessage)
                .containsExactly(m1);

        assertThat(handle1.getMessageSendHistory())
                .extracting(Envelope::getRemoteEndpoint)
                .extracting(Endpoint::getPort)
                .containsExactly(ep2.getPort());

        assertThat(handle1.getMessageReceiveHistory())
                .extracting(Envelope::getMessage)
                .containsExactly(m2);

        assertThat(handle1.getMessageReceiveHistory())
                .extracting(Envelope::getRemoteEndpoint)
                .extracting(Endpoint::getPort)
                .containsExactly(ep2.getPort());

    }

    @Test
    public void test_reliableSend_success_attept2() throws ReliabilityException {

        TestMessage m1 = new TestMessage();
        TestMessage m2 = new TestMessage();

        subsystem2.registerResponder(TestMessage.class, (actions, env) -> {
            actions.receiveOne().await();
            actions.send(m2, ep1);
        });

        handle1.reliableSend(m1, ep2)
                .expecting(TestMessage.class)
                .await();

        assertThat(handle1.getMessageSendHistory())
                .extracting(Envelope::getMessage)
                .containsExactly(m1, m1);

        assertThat(handle1.getMessageSendHistory())
                .extracting(Envelope::getRemoteEndpoint)
                .extracting(Endpoint::getPort)
                .containsExactly(ep2.getPort(), ep2.getPort());

        assertThat(handle1.getMessageReceiveHistory())
                .extracting(Envelope::getMessage)
                .containsExactly(m2);

        assertThat(handle1.getMessageReceiveHistory())
                .extracting(Envelope::getRemoteEndpoint)
                .extracting(Endpoint::getPort)
                .containsExactly(ep2.getPort());

    }

    @Test
    public void test_reliableSend_success_attept3() throws ReliabilityException {

        TestMessage m1 = new TestMessage();
        TestMessage m2 = new TestMessage();

        subsystem2.registerResponder(TestMessage.class, (actions, env) -> {
            actions.receiveOne().await();
            actions.receiveOne().await();
            actions.send(m2, ep1);
        });

        handle1.reliableSend(m1, ep2)
                .expecting(TestMessage.class)
                .await();

        assertThat(handle1.getMessageSendHistory())
                .extracting(Envelope::getMessage)
                .containsExactly(m1, m1, m1);

        assertThat(handle1.getMessageSendHistory())
                .extracting(Envelope::getRemoteEndpoint)
                .extracting(Endpoint::getPort)
                .containsExactly(ep2.getPort(), ep2.getPort(), ep2.getPort());

        assertThat(handle1.getMessageReceiveHistory())
                .extracting(Envelope::getMessage)
                .containsExactly(m2);

        assertThat(handle1.getMessageReceiveHistory())
                .extracting(Envelope::getRemoteEndpoint)
                .extracting(Endpoint::getPort)
                .containsExactly(ep2.getPort());

    }

    @Test
    public void test_reliableSend_fail() {

        TestMessage m1 = new TestMessage();

        subsystem2.registerResponder(TestMessage.class, (actions, env) -> {
            // Do nothing.
        });

        assertThatThrownBy(() -> {
            handle1.reliableSend(m1, ep2)
                    .expecting(TestMessage.class)
                    .await();
        }).isInstanceOf(ReliabilityException.class);

        assertThat(handle1.getMessageSendHistory())
                .extracting(Envelope::getMessage)
                .containsExactly(m1, m1, m1);

        assertThat(handle1.getMessageSendHistory())
                .extracting(Envelope::getRemoteEndpoint)
                .extracting(Endpoint::getPort)
                .containsExactly(ep2.getPort(), ep2.getPort(), ep2.getPort());

    }

}
