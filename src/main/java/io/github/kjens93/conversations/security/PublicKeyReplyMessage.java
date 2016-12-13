package io.github.kjens93.conversations.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.kjens93.conversations.messages.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.security.PublicKey;

/**
 * Created by kjensen on 12/11/16.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class PublicKeyReplyMessage extends Message {

    private final byte[] encoded;

    public PublicKeyReplyMessage(PublicKey publicKey) {
        this.encoded = publicKey.getEncoded();
    }

    @JsonIgnore
    public PublicKey getPublicKey() {
        return SigningUtils.decodePublicKey(encoded);
    }

}
