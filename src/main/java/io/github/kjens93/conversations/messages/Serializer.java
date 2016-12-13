package io.github.kjens93.conversations.messages;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by kjensen on 11/26/16.
 */
public interface Serializer {

    static <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try {
            return new ObjectMapper().readerFor(clazz).readValue(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] serialize(Object object) {
        try {
            return new ObjectMapper().writeValueAsBytes(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] serializeWithMixIn(Object object, Class<?> mixIn) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.addMixIn(object.getClass(), mixIn);
            return mapper.writeValueAsBytes(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
