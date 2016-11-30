package io.github.kjens93.conversations.communications;

import lombok.SneakyThrows;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by kjensen on 11/6/16.
 */
public class TCPConnection implements AutoCloseable {

    public Object readObject() throws IOException, ClassNotFoundException {
        return objectIn.readObject();
    }

    public TCPConnection writeObject(Object obj) throws IOException {
        objectOut.writeObject(obj);
        return this;
    }

    private final ObjectOutputStream objectOut;
    private final ObjectInputStream objectIn;
    private DataInputStream in;
    private DataOutputStream out;
    private final Socket socket;
    private ServerSocket serverSocket;

    @SneakyThrows
    public TCPConnection(Socket socket) {
        this.socket = socket;
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.objectOut = new ObjectOutputStream(socket.getOutputStream());
        this.objectIn = new ObjectInputStream(socket.getInputStream());
    }

    @SneakyThrows
    public TCPConnection(Socket socket, ServerSocket serverSocket) {
        this(socket);
        this.serverSocket = serverSocket;
    }

    @SneakyThrows
    public static String readUTF(DataInput in) {
        return DataInputStream.readUTF(in);
    }

    @SneakyThrows
    public int available() {
        return in.available();
    }

    @Override
    @SneakyThrows
    public void close() {
        try {
            in.close();
            out.close();
            objectIn.close();
            objectOut.close();
            socket.close();
            if (serverSocket != null)
                serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public TCPConnection flush() {
        out.flush();
        return this;
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    @SneakyThrows
    public int read(byte[] b) {
        return in.read(b);
    }

    @SneakyThrows
    public int read(byte[] b, int off, int len) {
        return in.read(b, off, len);
    }

    @SneakyThrows
    public int read() {
        return in.read();
    }

    @SneakyThrows
    public boolean readBoolean() {
        return in.readBoolean();
    }

    @SneakyThrows
    public byte readByte() {
        return in.readByte();
    }

    @SneakyThrows
    public char readChar() {
        return in.readChar();
    }

    @SneakyThrows
    public double readDouble() {
        return in.readDouble();
    }

    @SneakyThrows
    public float readFloat() {
        return in.readFloat();
    }

    @SneakyThrows
    public void readFully(byte[] b) {
        in.readFully(b);
    }

    @SneakyThrows
    public void readFully(byte[] b, int off, int len) {
        in.readFully(b, off, len);
    }

    @SneakyThrows
    public int readInt() {
        return in.readInt();
    }

    @SneakyThrows
    public long readLong() {
        return in.readLong();
    }

    @SneakyThrows
    public short readShort() {
        return in.readShort();
    }

    @SneakyThrows
    public String readUTF() {
        return in.readUTF();
    }

    @SneakyThrows
    public int readUnsignedByte() {
        return in.readUnsignedByte();
    }

    @SneakyThrows
    public int readUnsignedShort() {
        return in.readUnsignedShort();
    }

    @SneakyThrows
    public long skip(long n) {
        return in.skip(n);
    }

    @SneakyThrows
    public int skipBytes(int n) {
        return in.skipBytes(n);
    }

    @SneakyThrows
    public TCPConnection write(int b) {
        out.write(b);
        return this;
    }

    @SneakyThrows
    public TCPConnection write(byte[] b, int off, int len) {
        out.write(b, off, len);
        return this;
    }

    @SneakyThrows
    public TCPConnection write(byte[] b) {
        out.write(b);
        return this;
    }

    @SneakyThrows
    public TCPConnection writeBoolean(boolean v) {
        out.writeBoolean(v);
        return this;
    }

    @SneakyThrows
    public TCPConnection writeByte(int v) {
        out.writeByte(v);
        return this;
    }

    @SneakyThrows
    public TCPConnection writeBytes(String s) {
        out.writeBytes(s);
        return this;
    }

    @SneakyThrows
    public TCPConnection writeChar(int v) {
        out.writeChar(v);
        return this;
    }

    @SneakyThrows
    public TCPConnection writeChars(String s) {
        out.writeChars(s);
        return this;
    }

    @SneakyThrows
    public TCPConnection writeDouble(double v) {
        out.writeDouble(v);
        return this;
    }

    @SneakyThrows
    public TCPConnection writeFloat(float v) {
        out.writeFloat(v);
        return this;
    }

    @SneakyThrows
    public TCPConnection writeInt(int v) {
        out.writeInt(v);
        return this;
    }

    @SneakyThrows
    public TCPConnection writeLong(long v) {
        out.writeLong(v);
        return this;
    }

    @SneakyThrows
    public TCPConnection writeShort(int v) {
        out.writeShort(v);
        return this;
    }

    @SneakyThrows
    public TCPConnection writeUTF(String str) {
        out.writeUTF(str);
        return this;
    }

}
