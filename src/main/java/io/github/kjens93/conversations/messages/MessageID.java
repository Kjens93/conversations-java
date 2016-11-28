package io.github.kjens93.conversations.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by kjensen on 11/6/16.
 */
@Data
@AllArgsConstructor
public class MessageID {

    private final int processID;
    private final short messageID;

    @JsonCreator
    public MessageID(String string) {
        this.processID = Integer.parseInt(string.split(":")[0]);
        this.messageID = Short.parseShort(string.split(":")[1]);
    }

    @Override
    @JsonValue
    public String toString() {
        return processID + ":" + messageID;
    }

}
