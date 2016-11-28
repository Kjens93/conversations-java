package io.github.kjens93.conversations.collections;

import io.github.kjens93.conversations.messages.Envelope;

import java.util.Collection;
import java.util.Queue;

/**
 * Created by kjensen on 11/27/16.
 */
public class UDPInbox extends NotifyingQueue<Envelope> {

    public UDPInbox() {
        super();
    }

    public UDPInbox(Collection<Envelope> source) {
        super(source);
    }

    public UDPInbox(Queue<Envelope> source) {
        super(source);
    }

}
