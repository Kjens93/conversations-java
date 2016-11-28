package io.github.kjens93.conversations.collections;

import org.junit.Test;

import static io.github.kjens93.async.Async.async;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by kjensen on 11/27/16.
 */
public class NotifyingQueue_UT {

    @Test
    public void test() throws InterruptedException {

        NotifyingQueue<String> q = new NotifyingQueue<>();

        async(() -> q.add("Hello!"));

        synchronized (q) {
            q.wait(5000);
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

        async(() -> q.add("Hello!"));

        assertThat(q.poll())
                .isNull();

    }

}
