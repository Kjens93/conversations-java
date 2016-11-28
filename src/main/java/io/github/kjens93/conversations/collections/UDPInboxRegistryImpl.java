package io.github.kjens93.conversations.collections;

import io.github.kjens93.conversations.messages.MessageID;
import lombok.experimental.Delegate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kjensen on 11/27/16.
 */
class UDPInboxRegistryImpl implements UDPInboxRegistry {

    @Delegate
    private Map<MessageID, UDPInbox> map = new HashMap<>();

}
