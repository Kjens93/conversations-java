package io.github.kjens93.conversations.communications;

import lombok.extern.java.Log;

import java.net.*;
import java.util.logging.Level;

/**
 * Created by kjensen on 12/13/16.
 */
@Log
class LoggingDatagramSocket extends DatagramSocket {

    public LoggingDatagramSocket(int port) throws SocketException {
        super(port);
        if(isBound())
            log.log(Level.INFO, String.format(Strings.opnd_udp, port));
    }

    @Override
    public void close() {
        if(!isClosed()) {
            int port = getLocalPort();
            super.close();
            log.log(Level.INFO, String.format(Strings.clsd_udp, port));
        }
    }

}
