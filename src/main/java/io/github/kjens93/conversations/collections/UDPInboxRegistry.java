package io.github.kjens93.conversations.collections;

import io.github.kjens93.conversations.messages.MessageID;

import java.util.Map;

/**
 * Created by kjensen on 11/27/16.
 */
public interface UDPInboxRegistry extends Map<MessageID, UDPInbox> {

    default UDPInbox getOrNew(MessageID key) {
        if(containsKey(key))
            return get(key);
        UDPInbox box = new UDPInbox();
        put(key, box);
        return box;
    }

    static UDPInboxRegistry newInstance() {
        return new UDPInboxRegistryImpl();
    }

}
