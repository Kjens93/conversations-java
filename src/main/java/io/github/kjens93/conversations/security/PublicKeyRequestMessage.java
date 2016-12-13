package io.github.kjens93.conversations.security;

import io.github.kjens93.conversations.messages.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by kjensen on 12/11/16.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class PublicKeyRequestMessage extends Message {
}
