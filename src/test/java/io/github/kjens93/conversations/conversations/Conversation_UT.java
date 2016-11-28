package io.github.kjens93.conversations.conversations;

import io.github.kjens93.async.Commitment;
import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.communications.UDPCommunicator;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.github.kjens93.async.Async.async;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by kjensen on 11/27/16.
 */
public class Conversation_UT {

    private static final UDPCommunicator udpCommunicator = new CommSubsystem(1).udpCommunicator();
    private static final Endpoint ep = new Endpoint("127.0.0.1", 12345);
    private ConversationHandle handle;

    @Before
    public void setup() {
        handle = new ConversationHandle(udpCommunicator);
    }

    @Test
    public void test_conversation() throws Exception {

        Conversation conversation = actions -> {
            actions.send(new Envelope<>(new Message(), ep));
            actions.send(new Message(), ep);
            Envelope<Message> env1 = actions.receiveOne()
                    .ofType(Message.class)
                    .fromSender(ep)
                    .get(5, TimeUnit.SECONDS);
            actions.receiveOne()
                    .ofType(Message.class)
                    .fromSender(ep)
                    .await(5, TimeUnit.SECONDS);
            Envelope<Message> env2 = actions.receiveOne()
                    .ofType(Message.class)
                    .fromSender(ep)
                    .get();
            actions.receiveOne()
                    .ofType(Message.class)
                    .fromSender(ep)
                    .await();
        };



        Commitment c = async(() -> conversation.run(handle));

        Thread.sleep(500);

        handle.getInbox().add(new Envelope<>(new Message(), ep));
        handle.getInbox().add(new Envelope<>(new Message(), ep));
        handle.getInbox().add(new Envelope<>(new Message(), ep));
        handle.getInbox().add(new Envelope<>(new Message(), ep));

        c.await();

    }

    @Test
    public void test_conversation_fail_on_no_receive() throws Exception {

        Conversation conversation = actions -> {
            actions.send(new Message(), ep);
            actions.receiveOne()
                    .ofType(Message.class)
                    .fromSender(ep)
                    .await(2, TimeUnit.SECONDS);
        };

        Commitment c = async(() -> conversation.run(handle));

        assertThatThrownBy(c::await)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(TimeoutException.class);

    }

    @Test
    public void test_conversation_fail_on_receive_before_send() throws Exception {

        Conversation conversation = actions -> {
            actions.receiveOne()
                    .ofType(Message.class)
                    .fromSender(ep)
                    .await(2, TimeUnit.SECONDS);
        };

        Commitment c = async(() -> conversation.run(handle));

        assertThatThrownBy(c::await)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessageContaining("send()");

    }

}
