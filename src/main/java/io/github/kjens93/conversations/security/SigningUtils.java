package io.github.kjens93.conversations.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Throwables;
import io.github.kjens93.conversations.messages.Message;
import io.github.kjens93.conversations.messages.Serializer;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by kjensen on 12/11/16.
 */
public interface SigningUtils {

    @JsonIgnoreProperties("@sig")
    abstract class MessageMixIn {
    }

    static void sign(Message message, PrivateKey privateKey) {
        byte[] bytes = Serializer.serializeWithMixIn(message, MessageMixIn.class);
        byte[] signature = sign(bytes, privateKey);
        message.setSignature(signature);
    }

    static byte[] sign(byte[] data, PrivateKey privateKey) {
        try {
            Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
            dsa.initSign(privateKey);
            dsa.update(data);
            return dsa.sign();
        } catch (Throwable e) {
            Throwables.propagate(e);
            throw new RuntimeException("Unexpected", e);
        }
    }

    static boolean verify(Message message, PublicKey publicKey) {
        if(message.getSignature() == null) {
            throw new IllegalStateException("Message of type ["+message.getClass().getName()+"] does not have a signature.");
        }
        byte[] bytes = Serializer.serializeWithMixIn(message, MessageMixIn.class);
        byte[] signature = message.getSignature();
        return verify(bytes, signature, publicKey);
    }

    static void verifyLoud(Message message, PublicKey publicKey) throws SecurityException {
        if(!verify(message, publicKey)) {
            throw new SecurityException("Message of type ["+message.getClass().getName()+"] does not match the provided signature.");
        }
    }

    static boolean verify(byte[] data, byte[] signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
            Throwables.propagate(e);
            throw new RuntimeException("Unexpected", e);
        }
    }

    static void verifyLoud(byte[] data, byte[] signature, PublicKey publicKey) throws SecurityException {
        if(!verify(data, signature, publicKey)) {
            throw new SecurityException("Data does not match the provided signature.");
        }
    }

    static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            return keyGen.generateKeyPair();
        } catch(NoSuchAlgorithmException | NoSuchProviderException e) {
            Throwables.propagate(e);
            throw new RuntimeException("Unexpected", e);
        }
    }

    static PublicKey decodePublicKey(byte[] bytes) {
        try {
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(bytes);
            KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
            return keyFactory.generatePublic(pubKeySpec);
        } catch(InvalidKeySpecException | NoSuchAlgorithmException | NoSuchProviderException e) {
            Throwables.propagate(e);
            throw new RuntimeException("Unexpected", e);
        }
    }

}
