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
            DatagramSocket socket = new DatagramSocket(port);
            System.out.println("Opened UDP socket on port " + port);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if(!socket.isClosed()) {
                    socket.close();
                    System.out.println("Closed UDP socket on port " + port);
                }
            }));
            return socket;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static Socket createTCPConnectionSocket() {
        Socket socket = new Socket();
        System.out.println("Opened TCP connection socket");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(!socket.isClosed()) {
                try {
                    socket.close();
                    System.out.println("Closed TCP connection socket on port " + socket.getLocalPort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        return socket;
    }

    public static ServerSocket createTCPServerSocket() {
        try {
            int port = findAvailableTCPPort();
            ServerSocket socket = new ServerSocket(port);
            System.out.println("Opened TCP server socket on port " + port);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    socket.close();
                } catch(IOException e){
                    e.printStackTrace();
                } finally {
                    System.out.println("Closed TCP server socket on port " + port);
                }
            }));
            return socket;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int findAvailableUDPPort() {
        return (minPort == maxPort) ? minPort :
                SocketUtils.findAvailableUdpPort(minPort, maxPort);
    }

    private static int findAvailableTCPPort() {
        return (minPort == maxPort) ? minPort :
                SocketUtils.findAvailableTcpPort(minPort, maxPort);
    }

}
