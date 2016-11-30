package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.messages.Message;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by kjensen on 11/28/16.
 */
public class TCPConnection_UT {

    private ConversationHandle handle1;
    private CommSubsystem subsystem2;
    private Endpoint ep2;

    @Before
    public void setup() {
        handle1 = new ConversationHandle(new CommSubsystem(1).udpCommunicator());
        subsystem2 = new CommSubsystem(2);
        ep2 = subsystem2.getUdpEndpoint();
    }

    @Test
    public void test_simple_read_write() throws InterruptedException, ExecutionException, TimeoutException, ReliabilityException {

        subsystem2.registerResponder(Message.class, (actions, msg) -> {
            actions.send(new Message(), msg.getRemoteEndpoint());
            actions.waitForTCPConnection(msg.getRemoteEndpoint())
                    .await(10, TimeUnit.SECONDS);
        });

        handle1.reliableSend(new Message(), ep2, Message.class);

        handle1.openNewTCPConnection(ep2)
                .andThen(conn -> {
                    conn.writeUTF("Hi")
                            .flush();
        }).await(10, TimeUnit.SECONDS);

    }

}
