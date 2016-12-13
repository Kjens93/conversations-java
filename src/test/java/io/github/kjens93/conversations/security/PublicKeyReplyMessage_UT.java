package io.github.kjens93.conversations.security;

import io.github.kjens93.conversations.messages.Serializer;
import org.junit.Test;

import java.security.KeyPair;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by kjensen on 12/12/16.
 */
public class PublicKeyReplyMessage_UT {

    @Test
    public void test_canSerialize() {

        KeyPair keyPair = SigningUtils.generateKeyPair();
        PublicKeyReplyMessage msg = new PublicKeyReplyMessage(keyPair.getPublic());

        byte[] bytes = Serializer.serialize(msg);
        assertThat(bytes)
                .isNotEmpty();

        SigningUtils.sign(msg, keyPair.getPrivate());

        byte[] newBytes = Serializer.serialize(msg);
        assertThat(newBytes)
                .isNotEmpty()
                .isNotEqualTo(bytes);

    }

    @Test
    public void test_canDeserialize() {

        KeyPair keyPair = SigningUtils.generateKeyPair();
        PublicKeyReplyMessage msg = new PublicKeyReplyMessage(keyPair.getPublic());

        SigningUtils.sign(msg, keyPair.getPrivate());

        byte[] bytes = Serializer.serialize(msg);

        PublicKeyReplyMessage des = Serializer.deserialize(PublicKeyReplyMessage.class, bytes);

        assertThat(des)
                .isEqualTo(msg);

    }

}
