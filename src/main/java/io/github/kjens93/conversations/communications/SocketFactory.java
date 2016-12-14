package io.github.kjens93.conversations.communications;

import lombok.extern.java.Log;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by kjensen on 11/27/16.
 */
@Log
public final class SocketFactory {

    private static final int maxPort = 13999;
    private static final int minPort = 13000;

    private SocketFactory() {}

    public static DatagramSocket createUDPSocket() {
        try {
            int port = findAvailableUDPPort();
            DatagramSocket socket = new LoggingDatagramSocket(port);
            Runtime.getRuntime().addShutdownHook(new Thread(socket::close));
            return socket;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static Socket createTCPConnectionSocket() {
        LoggingSocket socket = new LoggingSocket();
        Runtime.getRuntime().addShutdownHook(new Thread(socket::close));
        return socket;
    }

    public static ServerSocket createTCPServerSocket() {
        try {
            int port = findAvailableTCPPort();
            LoggingServerSocket socket = new LoggingServerSocket(port);
            Runtime.getRuntime().addShutdownHook(new Thread(socket::close));
            return socket;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int findAvailableUDPPort() {
        return SocketUtils.findAvailableUdpPort(minPort, maxPort);
    }

    private static int findAvailableTCPPort() {
        return SocketUtils.findAvailableTcpPort(minPort, maxPort);
    }

}
