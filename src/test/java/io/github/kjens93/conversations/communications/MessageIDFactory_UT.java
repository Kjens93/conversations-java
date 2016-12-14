package io.github.kjens93.conversations.communications;

import io.github.kjens93.conversations.messages.MessageID;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by kjensen on 12/13/16.
 */
public class MessageIDFactory_UT {

    @Test
    public void test_readAndIncrement() throws NoSuchAlgorithmException {

        int pID = SecureRandom.getInstanceStrong().nextInt();

        MessageIDFactory factory = new MessageIDFactory(pID);

        IntStream.range(1, 1001).forEach(i -> {
            assertThat(factory.nextMessageID())
                    .isEqualTo(new MessageID(pID, (short) i));
        });

    }

}
