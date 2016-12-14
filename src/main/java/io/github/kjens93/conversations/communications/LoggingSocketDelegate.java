package io.github.kjens93.conversations.communications;

import lombok.experimental.Delegate;
import lombok.extern.java.Log;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;

import static io.github.kjens93.conversations.communications.Strings.bond_tcp;

/**
 * Created by kjensen on 12/13/16.
 */
@Log
class LoggingSocketDelegate extends Socket {

    @Delegate(excludes = Closeable.class)
    private final Socket delegate;

    LoggingSocketDelegate(Socket delegate) {
        this.delegate = delegate;
        log.log(Level.INFO, Strings.opnd_tcp);
        if(delegate.isConnected())
            log.log(Level.INFO, String.format(bond_tcp, delegate.getLocalPort(), delegate.getRemoteSocketAddress()));
    }

    @Override
    public void close() {
        if(!delegate.isClosed()) {
            int port = delegate.getLocalPort();
            try {
                delegate.close();
                log.log(Level.INFO, Strings.clsd_tcp, port);
            } catch (IOException e) {
                log.log(Level.WARNING, "Exception occurred while " +
                        "attempting to close TCP connection socket on port " + port, e);
            }
        }
    }
}

