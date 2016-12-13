package io.github.kjens93.conversations.collections;

import io.github.kjens93.promises.Commitment;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by kjensen on 11/27/16.
 */
public class NotifyingQueue_UT {

    @Test
    public void test() throws InterruptedException {

        NotifyingQueue<String> q = new NotifyingQueue<>();

        ((Commitment) () -> q.add("Hello!")).async();

        synchronized (q) {
            q.wait(1000);
        }

        assertThat(q.poll())
                .isEqualTo("Hello!");

    }

    @Test
    public void test_waiting_too_soon() throws InterruptedException {

        NotifyingQueue<String> q = new NotifyingQueue<>();

        synchronized (q) {
            q.wait(1000);
        }

        ((Commitment) () -> q.add("Hello!")).async();

        assertThat(q.poll())
                .isNull();

    }

}
