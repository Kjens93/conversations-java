package io.github.kjens93.conversations.messages;

import io.github.kjens93.conversations.communications.Endpoint;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.DatagramPacket;

/**
 * Created by kjensen on 11/26/16.
 */
@Data
@AllArgsConstructor
public class Envelope<T extends Message> {

    private T message;
    private Endpoint remoteEndpoint;

    public DatagramPacket toDatagramPacket() {
        byte[] bytes = Serializer.serialize(message);
        int len = bytes.length;
        return new DatagramPacket(bytes, len, remoteEndpoint);
    }

    public static <V extends Message> Envelope<V> from(DatagramPacket packet, Class<V> clazz) {
        V message = Serializer.deserialize(clazz, packet.getData());
        Endpoint ep = new Endpoint(packet.getAddress(), packet.getPort());
        return new Envelope<>(message, ep);
    }

}
