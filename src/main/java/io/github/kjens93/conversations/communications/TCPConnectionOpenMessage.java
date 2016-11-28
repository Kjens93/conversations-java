package io.github.kjens93.conversations.communications;

import io.github.kjens93.conversations.messages.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by kjensen on 11/28/16.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TCPConnectionOpenMessage extends Message {

    private final String host;
    private final int port;

}
