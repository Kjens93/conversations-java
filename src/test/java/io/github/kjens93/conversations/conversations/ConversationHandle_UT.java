package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.TestMessage;
import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.messages.Envelope;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by kjensen on 11/27/16.
 */
public class ConversationHandle_UT {

    private static final CommSubsystem subsystem = new CommSubsystem(1);
    private static final Endpoint ep = new Endpoint("127.0.0.1", 12345);
    private ConversationHandle handle;

    @Before
    public void setup() {
        handle = new ConversationHandle(subsystem);
    }

    @Test
    public void test_conversation() throws Exception {

        handle.send(new Envelope<>(new TestMessage(), ep));
        handle.send(new TestMessage(), ep);

        handle.getInbox().add(new Envelope<>(new TestMessage(), ep));
        handle.getInbox().add(new Envelope<>(new TestMessage(), ep));
        handle.getInbox().add(new Envelope<>(new TestMessage(), ep));
        handle.getInbox().add(new Envelope<>(new TestMessage(), ep));

        Envelope<TestMessage> env1 = handle.receiveOne()
                .ofType(TestMessage.class)
                .fromSender(ep)
                .get(5, TimeUnit.SECONDS);
        handle.receiveOne()
                .ofType(TestMessage.class)
                .fromSender(ep)
                .await(5, TimeUnit.SECONDS);
        Envelope<TestMessage> env2 = handle.receiveOne()
                .ofType(TestMessage.class)
                .fromSender(ep)
                .get();
        handle.receiveOne()
                .ofType(TestMessage.class)
                .fromSender(ep)
                .await();

    }

    @Test
    public void test_conversation_fail_on_no_receive() throws Exception {

        assertThatThrownBy(() -> {
            handle.send(new TestMessage(), ep);
            handle.receiveOne()
                    .ofType(TestMessage.class)
                    .fromSender(ep)
                    .await(2, TimeUnit.SECONDS);
        }).isInstanceOf(TimeoutException.class);

    }

    @Test
    public void test_conversation_fail_on_receive_before_send() throws Exception {

        assertThatThrownBy(() -> {
            handle.receiveOne()
                    .ofType(TestMessage.class)
                    .fromSender(ep)
                    .await(2, TimeUnit.SECONDS);
        }).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("send()");

    }

    @Test
    public void test_retry() throws Exception {

        AtomicInteger counter = new AtomicInteger(0);

        int value = handle.retry(3, () -> {
            if(counter.incrementAndGet() < 3)
                throw new IllegalStateException();
            synchronized (counter) {
                counter.notify();
            }
            return counter.get();
        });

        assertThat(counter.get())
                .isEqualTo(3)
                .isEqualTo(value);

    }

}
