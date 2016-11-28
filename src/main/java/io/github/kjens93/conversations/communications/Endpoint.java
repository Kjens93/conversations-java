package io.github.kjens93.conversations.communications;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by kjensen on 11/23/16.
 */
public class Endpoint extends InetSocketAddress {

    public Endpoint(InetSocketAddress addr) {
        super(addr.getAddress(), addr.getPort());
    }

    public Endpoint(int port) {
        super(port);
    }

    public Endpoint(InetAddress addr, int port) {
        super(addr, port);
    }

    public Endpoint(String hostname, int port) {
        super(hostname, port);
    }

}
