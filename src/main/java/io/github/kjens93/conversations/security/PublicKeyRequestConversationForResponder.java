package io.github.kjens93.conversations.security;

import io.github.kjens93.conversations.conversations.ConversationActions;
import io.github.kjens93.conversations.conversations.Responder;
import io.github.kjens93.conversations.messages.Envelope;
import lombok.RequiredArgsConstructor;

import java.security.PublicKey;

/**
 * Created by kjensen on 12/12/16.
 */
@RequiredArgsConstructor
public class PublicKeyRequestConversationForResponder implements Responder<PublicKeyRequestMessage> {

    private final PublicKey publicKey;

    @Override
    public void respond(ConversationActions actions, Envelope<PublicKeyRequestMessage> initialMessage) throws Exception {
        actions.send(new PublicKeyReplyMessage(publicKey), initialMessage.getRemoteEndpoint());
    }

}
