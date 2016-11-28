package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.communications.UDPCommunicator;
import io.github.kjens93.conversations.messages.Envelope;
import io.github.kjens93.conversations.messages.Message;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by kjensen on 11/27/16.
 */
public class EnvelopeStream_UT {

    private static final UDPCommunicator udpCommunicator = new CommSubsystem(1).udpCommunicator();
    private static final Endpoint ep1 = new Endpoint("127.0.0.1", 12345);
    private static final Endpoint ep2 = new Endpoint("127.0.0.1", 54321);
    private ConversationHandle handle;

    @Before
    public void setup() {
        handle = new ConversationHandle(udpCommunicator);
    }

    @Test
    public void test_receiveOne() {

        Envelope env = new Envelope<>(new Message(), ep1);

        handle.send(env);
        handle.getInbox().add(env);

        assertThat(handle.receiveOne().get())
            .isEqualTo(env);

    }

    @Test
    public void test_receiveOne_ofType() {

        Envelope env = new Envelope<>(new Message(), ep1);

        handle.send(env);
        handle.getInbox().add(env);

        assertThat(handle.receiveOne().ofType(Message.class).get())
                .isEqualTo(env);

        Envelope<MyMessage> newEnv = new Envelope<>(new MyMessage(), ep1);

        handle.getInbox().add(env);
        handle.getInbox().add(newEnv);

        assertThat(handle.receiveOne().ofType(MyMessage.class).get())
                .isEqualTo(newEnv);

        assertThat(handle.receiveOne().ofType(Message.class).get())
                .isEqualTo(env);

    }

    @Test
    public void test_receiveOne_fromSender() {

        Envelope env1 = new Envelope<>(new Message(), ep1);
        Envelope env2 = new Envelope<>(new Message(), ep2);

        handle.send(env1);
        handle.getInbox().add(env1);
        handle.getInbox().add(env2);

        assertThat(handle.receiveOne().fromSender(ep2).get())
                .isEqualTo(env2);

        assertThat(handle.receiveOne().fromSender(ep1).get())
                .isEqualTo(env1);

    }

    private class MyMessage extends Message {

    }


}
