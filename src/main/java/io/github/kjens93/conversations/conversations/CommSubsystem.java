package io.github.kjens93.conversations.conversations;

import com.google.common.base.Throwables;
import com.jezhumble.javasysmon.JavaSysMon;
import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.communications.UDPCommunicator;
import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.conversations.security.PublicKeyRequestConversationForResponder;
import io.github.kjens93.conversations.security.PublicKeyRequestMessage;
import io.github.kjens93.conversations.security.SigningUtils;
import io.github.kjens93.promises.Commitment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.security.KeyPair;
import java.security.PublicKey;

/**
 * Created by kjensen on 11/28/16.
 */
@Accessors(fluent = true)
public class CommSubsystem {

    @Getter(AccessLevel.PACKAGE)
    private static final CommSubsystem staticInstance = new CommSubsystem();

    @Getter(AccessLevel.PACKAGE)
    private final KeyPair signingKeyPair = SigningUtils.generateKeyPair();

    @Getter(AccessLevel.PACKAGE)
    private final ConversationFactory conversationFactory;

    @Getter(AccessLevel.PACKAGE)
    private final UDPCommunicator udpCommunicator;

    private CommSubsystem() {
        this(new JavaSysMon().currentPid());
    }

    public CommSubsystem(int processId) {
        conversationFactory = new ConversationFactory(this);
        udpCommunicator = new UDPCommunicator(conversationFactory, processId);
        PublicKey pub = signingKeyPair.getPublic();
        registerResponder(PublicKeyRequestMessage.class, new PublicKeyRequestConversationForResponder(pub));
    }

    public Endpoint getUdpEndpoint() {
        return udpCommunicator.getEndpoint();
    }

    public <T extends Message> void registerResponder(Class<T> clazz, Responder<T> responder) {
        conversationFactory.registerResponder(clazz, responder);
    }

    public Commitment newConversation(Conversation conversation) {
        return () -> {
            ConversationHandle handle = new ConversationHandle(this);
            try {
                conversation.run(handle);
                handle.close();
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        };
    }

}
