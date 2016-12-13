package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.TestMessage;
import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.messages.MessageID;
import io.github.kjens93.conversations.security.SigningUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.security.PublicKey;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by kjensen on 12/12/16.
 */
public class SignatureVerification_IT {

    private ConversationHandle handle1;
    private ConversationHandle handle2;
    private Endpoint ep1;
    private Endpoint ep2;

    @Before
    public void setup() {
        CommSubsystem subsystem1 = new CommSubsystem(1);
        CommSubsystem subsystem2 = new CommSubsystem(2);
        MessageID conversationId = subsystem1.udpCommunicator().newMessageId();
        handle1 = Mockito.spy(new ConversationHandle(subsystem1, conversationId));
        handle2 = Mockito.spy(new ConversationHandle(subsystem2, conversationId));
        ep1 = subsystem1.getUdpEndpoint();
        ep2 = subsystem2.getUdpEndpoint();
    }

    @Test(timeout = 500)
    public void test_verify_success() throws TimeoutException {
        handle2.enableSignatureVerification(ep1);
        handle1.send(new TestMessage(), ep2);
        handle2.receiveOne()
                .ofType(TestMessage.class)
                .fromSender(ep1)
                .await();

    }

    @Test(timeout = 500)
    public void test_verify_fail() {
        handle2.enableSignatureVerification(ep1);
        PublicKey bogusKey = SigningUtils.generateKeyPair().getPublic();
        Mockito.when(handle2.getPublicKeyForSender(ep1)).thenReturn(bogusKey);
        handle1.send(new TestMessage(), ep2);
        assertThatThrownBy(() -> {
            handle2.receiveOne()
                    .ofType(TestMessage.class)
                    .fromSender(ep1)
                    .await();
        }).isInstanceOf(SecurityException.class);

    }

    @Test(timeout = 5000)
    public void test_TCP_success() {
        handle2.enableSignatureVerification(ep1);
        handle1.sendViaTCP(new TestMessage(), ep2).async();
        handle2.receiveViaTCP(TestMessage.class, ep1).await();
    }

    @Test(timeout = 5000)
    public void test_TCP_failure() {
        handle2.enableSignatureVerification(ep1);
        PublicKey bogusKey = SigningUtils.generateKeyPair().getPublic();
        Mockito.when(handle2.getPublicKeyForSender(ep1)).thenReturn(bogusKey);
        handle1.sendViaTCP(new TestMessage(), ep2).async();
        assertThatThrownBy(() -> {
            handle2.receiveViaTCP(TestMessage.class, ep1).await();
        }).isInstanceOf(SecurityException.class);

    }

}
