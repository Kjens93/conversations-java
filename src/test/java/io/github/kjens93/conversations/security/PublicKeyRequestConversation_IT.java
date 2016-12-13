package io.github.kjens93.conversations.security;

import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.conversations.CommSubsystem;
import org.junit.Before;
import org.junit.Test;

import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by kjensen on 12/12/16.
 */
public class PublicKeyRequestConversation_IT {

    private CommSubsystem subsystem1;
    private CommSubsystem subsystem2;
    private Endpoint ep1;
    private Endpoint ep2;

    @Before
    public void setup() {
        subsystem1 = new CommSubsystem(1);
        subsystem2 = new CommSubsystem(2);
        ep1 = subsystem1.getUdpEndpoint();
        ep2 = subsystem2.getUdpEndpoint();
    }

    @Test
    public void test_canRequestPublicKey() {

        PublicKeyRequestConversationForInitiator initiator = new PublicKeyRequestConversationForInitiator(ep2);
        subsystem1.newConversation(initiator).await();
        PublicKey result = initiator.getResponse();

        assertThat(result)
                .isNotNull()
                .isInstanceOf(DSAPublicKey.class);

    }

}
