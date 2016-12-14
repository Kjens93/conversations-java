package io.github.kjens93.conversations.communications;

import lombok.extern.java.Log;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Level;

import static io.github.kjens93.conversations.communications.Strings.bond_tcp;

/**
 * Created by kjensen on 12/13/16.
 */
@Log
class LoggingSocket extends Socket {

    LoggingSocket() {
        super();
        log.log(Level.INFO, Strings.opnd_tcp);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        super.connect(endpoint, timeout);
        log.log(Level.INFO, String.format(bond_tcp, getLocalPort(), getRemoteSocketAddress()));
    }

    @Override
    public void close() {
        if(!isClosed()) {
            int port = getLocalPort();
            try {
                super.close();
                log.log(Level.INFO, String.format(Strings.clsd_tcp, port));
            } catch (IOException e) {
                log.log(Level.WARNING, "Exception occurred while " +
                        "attempting to close TCP connection socket on port " + port, e);
            }
        }
    }

}

