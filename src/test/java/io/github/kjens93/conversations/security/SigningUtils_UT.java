package io.github.kjens93.conversations.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kjens93.conversations.Repeat;
import io.github.kjens93.conversations.RepeatRule;
import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.conversations.messages.Serializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.Rule;
import org.junit.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by kjensen on 12/12/16.
 */
public class SigningUtils_UT {

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Test
    public void test_generateKeyPair() {
        KeyPair keyPair = SigningUtils.generateKeyPair();
        assertThat(keyPair)
                .isNotNull();

        PrivateKey privateKey = keyPair.getPrivate();
        assertThat(privateKey)
                .isNotNull()
                .isInstanceOf(DSAPrivateKey.class);

        PublicKey publicKey = keyPair.getPublic();
        assertThat(publicKey)
                .isNotNull()
                .isInstanceOf(DSAPublicKey.class);
    }

    @Test
    @Repeat(10)
    public void test_sign_verify_someBytes() {
        KeyPair keyPair = SigningUtils.generateKeyPair();

        byte[] bytes = new byte[] { 27, 65, 123, 57, 45, 32, 56, 34, 3, 34, 34 ,2, 43 };
        byte[] signature = SigningUtils.sign(bytes, keyPair.getPrivate());

        assertThat(signature)
                .isNotEmpty();

        assertThat(signature.length)
                .isBetween(45, 48); // I made sure this was a tight bound by running the test 10,000 times.

        assertThat(SigningUtils.verify(bytes, signature, keyPair.getPublic()))
                .isTrue();

        bytes[0] = 5;
        assertThat(SigningUtils.verify(bytes, signature, keyPair.getPublic()))
                .isFalse();

        bytes[0] = 27;

        signature[0] = 0;
        assertThatThrownBy(() -> SigningUtils.verify(bytes, signature, keyPair.getPublic()))
                .hasCauseInstanceOf(SignatureException.class);
    }

    @Test
    public void test_sign_verify_someMessage_asBytes() throws JsonProcessingException {
        KeyPair keyPair = SigningUtils.generateKeyPair();
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes, signature;

        bytes = objectMapper.writeValueAsString(new MyMessage("Hello world")).getBytes();
        signature = SigningUtils.sign(bytes, keyPair.getPrivate());

        assertThat(SigningUtils.verify(bytes, signature, keyPair.getPublic()))
                .isTrue();

        bytes = objectMapper.writeValueAsString(new MyMessage("Hello world My Friend!")).getBytes();

        assertThat(SigningUtils.verify(bytes, signature, keyPair.getPublic()))
                .isFalse();

        bytes = objectMapper.writeValueAsBytes(new MyMessage("Hello world"));
        signature = SigningUtils.sign(bytes, keyPair.getPrivate());

        assertThat(SigningUtils.verify(bytes, signature, keyPair.getPublic()))
                .isTrue();

        bytes = objectMapper.writeValueAsBytes(new MyMessage("Hello world My Friend!"));

        assertThat(SigningUtils.verify(bytes, signature, keyPair.getPublic()))
                .isFalse();

        bytes = Serializer.serialize(new MyMessage("Hello world"));
        signature = SigningUtils.sign(bytes, keyPair.getPrivate());

        assertThat(SigningUtils.verify(bytes, signature, keyPair.getPublic()))
                .isTrue();

        bytes = Serializer.serialize(new MyMessage("Hello world My Friend!"));

        assertThat(SigningUtils.verify(bytes, signature, keyPair.getPublic()))
                .isFalse();
    }

    @Test
    public void test_sign_verify_someMessage() throws JsonProcessingException {
        KeyPair keyPair = SigningUtils.generateKeyPair();
        MyMessage message;

        message = new MyMessage("Hello world");

        assertThat(message.getSignature())
                .isNull();

        SigningUtils.sign(message, keyPair.getPrivate());

        assertThat(message.getSignature())
                .isNotEmpty();

        byte[] signature = message.getSignature();

        assertThat(SigningUtils.verify(message, keyPair.getPublic()))
                .isTrue();

        assertThat(message.getSignature())
                .containsExactly(signature);

        message.setBody("Hello World my Friend!");

        assertThat(SigningUtils.verify(message, keyPair.getPublic()))
                .isFalse();

        assertThat(message.getSignature())
                .containsExactly(signature);
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    private static final class MyMessage extends Message {
        private String body;
    }

}
