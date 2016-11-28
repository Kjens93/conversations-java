package io.github.kjens93.conversations.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Data;

/**
 * Created by kjensen on 11/26/16.
 */
@Data
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY, property = "@type")
@JsonPropertyOrder({"conversationId", "messageId"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

    private MessageID conversationId;
    private MessageID messageId;

    public void setConversationId(MessageID conversationId) {
        this.conversationId = conversationId;
    }

    public void setMessageId(MessageID messageId) {
        this.messageId = messageId;
        if (conversationId == null)
            this.conversationId = messageId;
    }

}
