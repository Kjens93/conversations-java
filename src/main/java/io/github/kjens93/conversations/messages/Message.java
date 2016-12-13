package io.github.kjens93.conversations.messages;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by kjensen on 11/26/16.
 */
@Data
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY, property = "@type")
@JsonPropertyOrder({"@cid", "@mid", "@sig", "@type"})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Message implements Serializable {

    @JsonProperty("@cid")
    private MessageID conversationId;
    @JsonProperty("@mid")
    private MessageID messageId;
    @JsonProperty("@sig")
    private byte[] signature;

    public void setMessageId(MessageID messageId) {
        this.messageId = messageId;
        if (conversationId == null)
            this.conversationId = messageId;
    }

}
