package io.github.kjens93.conversations.conversations;

import com.google.common.base.Throwables;
import io.github.kjens93.conversations.TestMessage;
import io.github.kjens93.conversations.collections.UDPInbox;
import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.promises.Commitment;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by kjensen on 11/27/16.
 */
public class Conversation_UT {

    private static final CommSubsystem subsystem = new CommSubsystem(1);
    private static final Endpoint ep = new Endpoint("127.0.0.1", 12345);
    private ConversationHandle handle;

    @Before
    public void setup() {
        handle = new ConversationHandle(subsystem);
    }

    @Test
    public void test_conversation() throws Exception {

        Conversation conversation = actions -> {
            actions.send(new Envelope<>(new TestMessage(), ep));
            actions.send(new TestMessage(), ep);
            Envelope<TestMessage> env1 = actions.receiveOne()
                    .ofType(TestMessage.class)
                    .fromSender(ep)
                    .get(5, TimeUnit.SECONDS);
            actions.receiveOne()
                    .ofType(TestMessage.class)
                    .fromSender(ep)
                    .await(5, TimeUnit.SECONDS);
            Envelope<TestMessage> env2 = actions.receiveOne()
                    .ofType(TestMessage.class)
                    .fromSender(ep)
                    .get();
            actions.receiveOne()
                    .ofType(TestMessage.class)
                    .fromSender(ep)
                    .await();
        };



        Commitment c = ((Commitment)() -> {
            try {
                conversation.run(handle);
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }).async(Throwable::printStackTrace);

        Thread.sleep(1000);

        UDPInbox box = handle.getInbox();

        box.add(new Envelope<>(new TestMessage(), ep));
        box.add(new Envelope<>(new TestMessage(), ep));
        box.add(new Envelope<>(new TestMessage(), ep));
        box.add(new Envelope<>(new TestMessage(), ep));

        c.await();

    }

    @Test
    public void test_conversation_fail_on_no_receive() throws Exception {

        Conversation conversation = actions -> {
            actions.send(new TestMessage(), ep);
            actions.receiveOne()
                    .ofType(TestMessage.class)
                    .fromSender(ep)
                    .await(2, TimeUnit.SECONDS);
        };

        Commitment c = () -> {
            try {
                conversation.run(handle);
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        };

        assertThatThrownBy(c::await)
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(TimeoutException.class);

    }

    @Test
    public void test_conversation_fail_on_receive_before_send() throws Exception {

        Conversation conversation = actions -> {
            actions.receiveOne()
                    .ofType(TestMessage.class)
                    .fromSender(ep)
                    .await(2, TimeUnit.SECONDS);
        };

        Commitment c = () -> {
            try {
                conversation.run(handle);
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        };

        assertThatThrownBy(c::await)
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("send()");

    }

}
