package io.github.kjens93.conversations.conversations;

import com.google.common.base.Throwables;
import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.communications.TCPConnection;
import io.github.kjens93.conversations.messages.Message;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test(timeout = 500)
    public void test_simple_read_write() throws InterruptedException, ExecutionException, TimeoutException, ReliabilityException {

        subsystem2.registerResponder(Message.class, (actions, msg) -> {
            actions.send(new Message(), msg.getRemoteEndpoint());
            actions.waitForTCPConnection(msg.getRemoteEndpoint())
                    .andThen(conn -> {
                        try {
                            String hi = conn.readUTF();
                            System.out.println(hi);
                            Message message = (Message) conn.readObject();
                            System.out.println(message);
                            conn.writeUTF("Done").flush();
                        } catch (IOException | ClassNotFoundException e) {
                            Throwables.propagate(e);
                        }
                    })
                    .andThen(TCPConnection::close)
                    .await();
        });

        handle1.reliableSend(new Message(), ep2, Message.class);

        handle1.openNewTCPConnection(ep2)
                .andThen(conn -> {
                    try {
                        conn.writeUTF("Hi").flush();
                        conn.writeObject(new Message()).flush();
                        conn.readUTF();
                    } catch (IOException e) {
                        Throwables.propagate(e);
                    }
                })
                .andThen(TCPConnection::close)
                .await();

    }

    @Test(timeout = 500)
    public void test_send_receive_serializable() {

        String objectToSend = "Hello world!";

        subsystem2.registerResponder(Message.class, (actions, initialMessage) -> {
            actions.sendViaTCP(objectToSend, initialMessage.getRemoteEndpoint()).await();
        });

        handle1.send(new Message(), subsystem2.getUdpEndpoint());
        String result = handle1.receiveViaTCP(String.class, subsystem2.getUdpEndpoint()).get();
        assertThat(result).isEqualTo(result);

    }



}
