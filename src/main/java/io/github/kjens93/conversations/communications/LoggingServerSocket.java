package io.github.kjens93.conversations.communications;

import lombok.extern.java.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

/**
 * Created by kjensen on 12/13/16.
 */
@Log
class LoggingServerSocket extends ServerSocket {

    public LoggingServerSocket(int port) throws IOException {
        super(port);
        if(isBound())
            log.log(Level.INFO, String.format(Strings.opnd_tcs, port));
    }

    @Override
    public Socket accept() throws IOException {
        Socket result = super.accept();
        LoggingSocketDelegate del = new LoggingSocketDelegate(result);
        Runtime.getRuntime().addShutdownHook(new Thread(del::close));
        return del;
    }

    @Override
    public void close() {
        if(!isClosed()) {
            int port = getLocalPort();
            try {
                super.close();
                log.log(Level.INFO, String.format(Strings.clsd_tcs, port));
            } catch (IOException e) {
                log.log(Level.WARNING, "Exception occurred while " +
                        "attempting to close TCP server socket on port " + port, e);
            }
        }
    }
}
