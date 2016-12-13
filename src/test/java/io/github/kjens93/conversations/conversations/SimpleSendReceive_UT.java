package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.TestMessage;
import io.github.kjens93.conversations.communications.Endpoint;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by kjensen on 11/27/16.
 */
public class SimpleSendReceive_UT {

    private ConversationHandle handle1;
    private CommSubsystem subsystem2;
    private Endpoint ep2;

    @Before
    public void setup() {
        handle1 = new ConversationHandle(new CommSubsystem(1));
        subsystem2 = new CommSubsystem(2);
        ep2 = subsystem2.getUdpEndpoint();
    }

    @Test
    public void test_send_success() throws ReliabilityException, InterruptedException {

        AtomicBoolean success = new AtomicBoolean(false);

        subsystem2.registerResponder(TestMessage.class, (actions, initialMessage) -> {
            synchronized (success) {
                success.set(true);
                success.notify();
            }
        });

        handle1.send(new TestMessage(), ep2);

        synchronized (success) {
            success.wait(1000);
        }

        assertThat(success.get()).isTrue();

    }

}
