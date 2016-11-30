package io.github.kjens93.conversations.conversations;

import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.communications.UDPCommunicator;
import io.github.kjens93.conversations.messages.Message;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Created by kjensen on 11/28/16.
 */
@Getter(AccessLevel.PACKAGE)
@Accessors(fluent = true)
public class CommSubsystem {

    @Getter(AccessLevel.PACKAGE)
    private static final CommSubsystem staticInstance = new CommSubsystem();

    private final ConversationFactory conversationFactory;

    private final UDPCommunicator udpCommunicator;

    public CommSubsystem() {
        conversationFactory = new ConversationFactory();
        udpCommunicator = new UDPCommunicator(conversationFactory);
    }

    public CommSubsystem(int processId) {
        conversationFactory = new ConversationFactory();
        udpCommunicator = new UDPCommunicator(conversationFactory, processId);
    }

    public Endpoint getUdpEndpoint() {
        return udpCommunicator.getEndpoint();
    }

    public <T extends Message> void registerResponder(Class<T> clazz, Responder<T> responder) {
        conversationFactory.registerResponder(clazz, responder);
    }

}
