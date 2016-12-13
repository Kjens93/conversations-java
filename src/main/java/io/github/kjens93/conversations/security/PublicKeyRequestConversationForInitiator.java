package io.github.kjens93.conversations.security;

import io.github.kjens93.conversations.communications.Endpoint;
import io.github.kjens93.conversations.conversations.Conversation;
import io.github.kjens93.conversations.conversations.ConversationActions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

/**
 * Created by kjensen on 12/12/16.
 */
@RequiredArgsConstructor
public class PublicKeyRequestConversationForInitiator implements Conversation {

    @Getter
    private PublicKey response;
    private final Endpoint peer;

    @Override
    public void run(ConversationActions actions) throws Exception {
        actions.send(new PublicKeyRequestMessage(), peer);
        response = actions.receiveOne()
                .ofType(PublicKeyReplyMessage.class)
                .fromSender(peer)
                .get(5, TimeUnit.SECONDS)
                .getMessage()
                .getPublicKey();
    }
}
