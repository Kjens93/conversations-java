package io.github.kjens93.conversations.communications;

import lombok.SneakyThrows;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by kjensen on 11/6/16.
 */
public class TCPConnection implements AutoCloseable {

    /**
     * Read an object from the ObjectInputStream.  The class of the object, the
     * signature of the class, and the values of the non-transient and
     * non-static fields of the class and all of its supertypes are read.
     * Default deserializing for a class can be overriden using the writeObject
     * and readObject methods.  Objects referenced by this object are read
     * transitively so that a complete equivalent graph of objects is
     * reconstructed by readObject.
     *
     * <p>The root object is completely restored when all of its fields and the
     * objects it references are completely restored.  At this point the object
     * validation callbacks are executed in order based on their registered
     * priorities. The callbacks are registered by objects (in the readObject
     * special methods) as they are individually restored.
     *
     * <p>Exceptions are thrown for problems with the InputStream and for
     * classes that should not be deserialized.  All exceptions are fatal to
     * the InputStream and leave it in an indeterminate state; it is up to the
     * caller to ignore or recover the stream state.
     *
     * @throws ClassNotFoundException Class of a serialized object cannot be
     *          found.
     * @throws InvalidClassException Something is wrong with a class used by
     *          serialization.
     * @throws StreamCorruptedException Control information in the
     *          stream is inconsistent.
     * @throws OptionalDataException Primitive data was found in the
     *          stream instead of objects.
     * @throws IOException Any of the usual Input/Output related exceptions.
     */
    public Object readObject() throws IOException, ClassNotFoundException {
        return objectIn.readObject();
    }

    /**
     * Write the specified object to the ObjectOutputStream.  The class of the
     * object, the signature of the class, and the values of the non-transient
     * and non-static fields of the class and all of its supertypes are
     * written.  Default serialization for a class can be overridden using the
     * writeObject and the readObject methods.  Objects referenced by this
     * object are written transitively so that a complete equivalent graph of
     * objects can be reconstructed by an ObjectInputStream.
     *
     * <p>Exceptions are thrown for problems with the OutputStream and for
     * classes that should not be serialized.  All exceptions are fatal to the
     * OutputStream, which is left in an indeterminate state, and it is up to
     * the caller to ignore or recover the stream state.
     *
     * @throws InvalidClassException Something is wrong with a class used by
     *          serialization.
     * @throws NotSerializableException Some object to be serialized does not
     *          implement the java.io.Serializable interface.
     * @throws IOException Any exception thrown by the underlying
     *          OutputStream.
     * @param obj
     */
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

    /**
     * Reads from the
     * stream <code>in</code> a representation
     * of a Unicode  character string encoded in
     * <a href="DataInput.html#modified-utf-8">modified UTF-8</a> format;
     * this string of characters is then returned as a <code>String</code>.
     * The details of the modified UTF-8 representation
     * are  exactly the same as for the <code>readUTF</code>
     * method of <code>DataInput</code>.
     *
     * @param in a data input stream.
     * @return a Unicode string.
     * @throws EOFException           if the input stream reaches the end
     *                                before all the bytes.
     * @throws IOException            the stream has been closed and the contained
     *                                input stream does not support reading after close, or
     *                                another I/O error occurs.
     * @throws UTFDataFormatException if the bytes do not represent a
     *                                valid modified UTF-8 encoding of a Unicode string.
     * @see DataInputStream#readUnsignedShort()
     */
    @SneakyThrows
    public static String readUTF(DataInput in) {
        return DataInputStream.readUTF(in);
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or
     * skipped over) from this input stream without blocking by the next
     * caller of a method for this input stream. The next caller might be
     * the same thread or another thread.  A single read or skip of this
     * many bytes will not block, but may read or skip fewer bytes.
     * <p>
     * This method returns the result of {@link #in in}.available().
     *
     * @return an estimate of the number of bytes that can be read (or skipped
     * over) from this input stream without blocking.
     * @throws IOException if an I/O error occurs.
     */
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

    /**
     * Flushes this data output stream. This forces any buffered output
     * bytes to be written out to the stream.
     * <p>
     * The <code>flush</code> method of <code>DataOutputStream</code>
     * calls the <code>flush</code> method of its underlying output stream.
     *
     * @throws IOException if an I/O error occurs.
     * @see FilterOutputStream#out
     * @see OutputStream#flush()
     */
    @SneakyThrows
    public TCPConnection flush() {
        out.flush();
        return this;
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    /**
     * Reads some number of bytes from the contained input stream and
     * stores them into the buffer array <code>b</code>. The number of
     * bytes actually read is returned as an integer. This method blocks
     * until input data is available, end of file is detected, or an
     * exception is thrown.
     * <p>
     * <p>If <code>b</code> is null, a <code>NullPointerException</code> is
     * thrown. If the length of <code>b</code> is zero, then no bytes are
     * read and <code>0</code> is returned; otherwise, there is an attempt
     * to read at least one byte. If no byte is available because the
     * stream is at end of file, the value <code>-1</code> is returned;
     * otherwise, at least one byte is read and stored into <code>b</code>.
     * <p>
     * <p>The first byte read is stored into element <code>b[0]</code>, the
     * next one into <code>b[1]</code>, and so on. The number of bytes read
     * is, at most, equal to the length of <code>b</code>. Let <code>k</code>
     * be the number of bytes actually read; these bytes will be stored in
     * elements <code>b[0]</code> through <code>b[k-1]</code>, leaving
     * elements <code>b[k]</code> through <code>b[b.length-1]</code>
     * unaffected.
     * <p>
     * <p>The <code>read(b)</code> method has the same effect as:
     * <blockquote><pre>
     * read(b, 0, b.length)
     * </pre></blockquote>
     *
     * @param b the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or
     * <code>-1</code> if there is no more data because the end
     * of the stream has been reached.
     * @throws IOException if the first byte cannot be read for any reason
     *                     other than end of file, the stream has been closed and the underlying
     *                     input stream does not support reading after close, or another I/O
     *                     error occurs.
     * @see FilterInputStream#in
     * @see InputStream#read(byte[], int, int)
     */
    @SneakyThrows
    public int read(byte[] b) {
        return in.read(b);
    }

    /**
     * Reads up to <code>len</code> bytes of data from the contained
     * input stream into an array of bytes.  An attempt is made to read
     * as many as <code>len</code> bytes, but a smaller number may be read,
     * possibly zero. The number of bytes actually read is returned as an
     * integer.
     * <p>
     * <p> This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     * <p>
     * <p> If <code>len</code> is zero, then no bytes are read and
     * <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at end of
     * file, the value <code>-1</code> is returned; otherwise, at least one
     * byte is read and stored into <code>b</code>.
     * <p>
     * <p> The first byte read is stored into element <code>b[off]</code>, the
     * next one into <code>b[off+1]</code>, and so on. The number of bytes read
     * is, at most, equal to <code>len</code>. Let <i>k</i> be the number of
     * bytes actually read; these bytes will be stored in elements
     * <code>b[off]</code> through <code>b[off+</code><i>k</i><code>-1]</code>,
     * leaving elements <code>b[off+</code><i>k</i><code>]</code> through
     * <code>b[off+len-1]</code> unaffected.
     * <p>
     * <p> In every case, elements <code>b[0]</code> through
     * <code>b[off]</code> and elements <code>b[off+len]</code> through
     * <code>b[b.length-1]</code> are unaffected.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset in the destination array <code>b</code>
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or
     * <code>-1</code> if there is no more data because the end
     * of the stream has been reached.
     * @throws NullPointerException      If <code>b</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException If <code>off</code> is negative,
     *                                   <code>len</code> is negative, or <code>len</code> is greater than
     *                                   <code>b.length - off</code>
     * @throws IOException               if the first byte cannot be read for any reason
     *                                   other than end of file, the stream has been closed and the underlying
     *                                   input stream does not support reading after close, or another I/O
     *                                   error occurs.
     * @see FilterInputStream#in
     * @see InputStream#read(byte[], int, int)
     */
    @SneakyThrows
    public int read(byte[] b, int off, int len) {
        return in.read(b, off, len);
    }

    /**
     * Reads the next byte of data from this input stream. The value
     * byte is returned as an <code>int</code> in the range
     * <code>0</code> to <code>255</code>. If no byte is available
     * because the end of the stream has been reached, the value
     * <code>-1</code> is returned. This method blocks until input data
     * is available, the end of the stream is detected, or an exception
     * is thrown.
     * <p>
     * This method
     * simply performs <code>in.read()</code> and returns the result.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * stream is reached.
     * @throws IOException if an I/O error occurs.
     * @see FilterInputStream#in
     */
    @SneakyThrows
    public int read() {
        return in.read();
    }

    /**
     * See the general contract of the <code>readBoolean</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return the <code>boolean</code> value read.
     * @throws EOFException if this input stream has reached the end.
     * @throws IOException  the stream has been closed and the contained
     *                      input stream does not support reading after close, or
     *                      another I/O error occurs.
     * @see FilterInputStream#in
     */
    @SneakyThrows
    public boolean readBoolean() {
        return in.readBoolean();
    }

    /**
     * See the general contract of the <code>readByte</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return the next byte of this input stream as a signed 8-bit
     * <code>byte</code>.
     * @throws EOFException if this input stream has reached the end.
     * @throws IOException  the stream has been closed and the contained
     *                      input stream does not support reading after close, or
     *                      another I/O error occurs.
     * @see FilterInputStream#in
     */
    @SneakyThrows
    public byte readByte() {
        return in.readByte();
    }

    /**
     * See the general contract of the <code>readChar</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return the next two bytes of this input stream, interpreted as a
     * <code>char</code>.
     * @throws EOFException if this input stream reaches the end before
     *                      reading two bytes.
     * @throws IOException  the stream has been closed and the contained
     *                      input stream does not support reading after close, or
     *                      another I/O error occurs.
     * @see FilterInputStream#in
     */
    @SneakyThrows
    public char readChar() {
        return in.readChar();
    }

    /**
     * See the general contract of the <code>readDouble</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return the next eight bytes of this input stream, interpreted as a
     * <code>double</code>.
     * @throws EOFException if this input stream reaches the end before
     *                      reading eight bytes.
     * @throws IOException  the stream has been closed and the contained
     *                      input stream does not support reading after close, or
     *                      another I/O error occurs.
     * @see DataInputStream#readLong()
     * @see Double#longBitsToDouble(long)
     */
    @SneakyThrows
    public double readDouble() {
        return in.readDouble();
    }

    /**
     * See the general contract of the <code>readFloat</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return the next four bytes of this input stream, interpreted as a
     * <code>float</code>.
     * @throws EOFException if this input stream reaches the end before
     *                      reading four bytes.
     * @throws IOException  the stream has been closed and the contained
     *                      input stream does not support reading after close, or
     *                      another I/O error occurs.
     * @see DataInputStream#readInt()
     * @see Float#intBitsToFloat(int)
     */
    @SneakyThrows
    public float readFloat() {
        return in.readFloat();
    }

    /**
     * See the general contract of the <code>readFully</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @param b the buffer into which the data is read.
     * @throws EOFException if this input stream reaches the end before
     *                      reading all the bytes.
     * @throws IOException  the stream has been closed and the contained
     *                      input stream does not support reading after close, or
     *                      another I/O error occurs.
     * @see FilterInputStream#in
     */
    @SneakyThrows
    public void readFully(byte[] b) {
        in.readFully(b);
    }

    /**
     * See the general contract of the <code>readFully</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the number of bytes to read.
     * @throws EOFException if this input stream reaches the end before
     *                      reading all the bytes.
     * @throws IOException  the stream has been closed and the contained
     *                      input stream does not support reading after close, or
     *                      another I/O error occurs.
     * @see FilterInputStream#in
     */
    @SneakyThrows
    public void readFully(byte[] b, int off, int len) {
        in.readFully(b, off, len);
    }

    /**
     * See the general contract of the <code>readInt</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return the next four bytes of this input stream, interpreted as an
     * <code>int</code>.
     * @throws EOFException if this input stream reaches the end before
     *                      reading four bytes.
     * @throws IOException  the stream has been closed and the contained
     *                      input stream does not support reading after close, or
     *                      another I/O error occurs.
     * @see FilterInputStream#in
     */
    @SneakyThrows
    public int readInt() {
        return in.readInt();
    }

    /**
     * See the general contract of the <code>readLong</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return the next eight bytes of this input stream, interpreted as a
     * <code>long</code>.
     * @throws EOFException if this input stream reaches the end before
     *                      reading eight bytes.
     * @throws IOException  the stream has been closed and the contained
     *                      input stream does not support reading after close, or
     *                      another I/O error occurs.
     * @see FilterInputStream#in
     */
    @SneakyThrows
    public long readLong() {
        return in.readLong();
    }

    /**
     * See the general contract of the <code>readShort</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return the next two bytes of this input stream, interpreted as a
     * signed 16-bit number.
     * @throws EOFException if this input stream reaches the end before
     *                      reading two bytes.
     * @throws IOException  the stream has been closed and the contained
     *                      input stream does not support reading after close, or
     *                      another I/O error occurs.
     * @see FilterInputStream#in
     */
    @SneakyThrows
    public short readShort() {
        return in.readShort();
    }

    /**
     * See the general contract of the <code>readUTF</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return a Unicode string.
     * @throws EOFException           if this input stream reaches the end before
     *                                reading all the bytes.
     * @throws IOException            the stream has been closed and the contained
     *                                input stream does not support reading after close, or
     *                                another I/O error occurs.
     * @throws UTFDataFormatException if the bytes do not represent a valid
     *                                modified UTF-8 encoding of a string.
     * @see DataInputStream#readUTF(DataInput)
     */
    @SneakyThrows
    public String readUTF() {
        return in.readUTF();
    }

    /**
     * See the general contract of the <code>readUnsignedByte</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return the next byte of this input stream, interpreted as an
     * unsigned 8-bit number.
     * @throws EOFException if this input stream has reached the end.
     * @throws IOException  the stream has been closed and the contained
     *                      input stream does not support reading after close, or
     *                      another I/O error occurs.
     * @see FilterInputStream#in
     */
    @SneakyThrows
    public int readUnsignedByte() {
        return in.readUnsignedByte();
    }

    /**
     * See the general contract of the <code>readUnsignedShort</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return the next two bytes of this input stream, interpreted as an
     * unsigned 16-bit integer.
     * @throws EOFException if this input stream reaches the end before
     *                      reading two bytes.
     * @throws IOException  the stream has been closed and the contained
     *                      input stream does not support reading after close, or
     *                      another I/O error occurs.
     * @see FilterInputStream#in
     */
    @SneakyThrows
    public int readUnsignedShort() {
        return in.readUnsignedShort();
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from the
     * input stream. The <code>skip</code> method may, for a variety of
     * reasons, end up skipping over some smaller number of bytes,
     * possibly <code>0</code>. The actual number of bytes skipped is
     * returned.
     * <p>
     * This method simply performs <code>in.skip(n)</code>.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException if the stream does not support seek,
     *                     or if some other I/O error occurs.
     */
    @SneakyThrows
    public long skip(long n) {
        return in.skip(n);
    }

    /**
     * See the general contract of the <code>skipBytes</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException if the contained input stream does not support
     *                     seek, or the stream has been closed and
     *                     the contained input stream does not support
     *                     reading after close, or another I/O error occurs.
     */
    @SneakyThrows
    public int skipBytes(int n) {
        return in.skipBytes(n);
    }

    /**
     * Writes the specified byte (the low eight bits of the argument
     * <code>b</code>) to the underlying output stream. If no exception
     * is thrown, the counter <code>written</code> is incremented by
     * <code>1</code>.
     * <p>
     * Implements the <code>write</code> method of <code>OutputStream</code>.
     *
     * @param b the <code>byte</code> to be written.
     * @throws IOException if an I/O error occurs.
     * @see FilterOutputStream#out
     */
    @SneakyThrows
    public TCPConnection write(int b) {
        out.write(b);
        return this;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to the underlying output stream.
     * If no exception is thrown, the counter <code>written</code> is
     * incremented by <code>len</code>.
     *
     * @param b   the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if an I/O error occurs.
     * @see FilterOutputStream#out
     */
    @SneakyThrows
    public TCPConnection write(byte[] b, int off, int len) {
        out.write(b, off, len);
        return this;
    }

    /**
     * Writes <code>b.length</code> bytes to this output stream.
     * <p>
     * The <code>write</code> method of <code>FilterOutputStream</code>
     * calls its <code>write</code> method of three arguments with the
     * arguments <code>b</code>, <code>0</code>, and
     * <code>b.length</code>.
     * <p>
     * Note that this method does not call the one-argument
     * <code>write</code> method of its underlying stream with the single
     * argument <code>b</code>.
     *
     * @param b the data to be written.
     * @throws IOException if an I/O error occurs.
     * @see FilterOutputStream#write(byte[], int, int)
     */
    @SneakyThrows
    public TCPConnection write(byte[] b) {
        out.write(b);
        return this;
    }

    /**
     * Writes a <code>boolean</code> to the underlying output stream as
     * a 1-byte value. The value <code>true</code> is written out as the
     * value <code>(byte)1</code>; the value <code>false</code> is
     * written out as the value <code>(byte)0</code>. If no exception is
     * thrown, the counter <code>written</code> is incremented by
     * <code>1</code>.
     *
     * @param v a <code>boolean</code> value to be written.
     * @throws IOException if an I/O error occurs.
     * @see FilterOutputStream#out
     */
    @SneakyThrows
    public TCPConnection writeBoolean(boolean v) {
        out.writeBoolean(v);
        return this;
    }

    /**
     * Writes out a <code>byte</code> to the underlying output stream as
     * a 1-byte value. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>1</code>.
     *
     * @param v a <code>byte</code> value to be written.
     * @throws IOException if an I/O error occurs.
     * @see FilterOutputStream#out
     */
    @SneakyThrows
    public TCPConnection writeByte(int v) {
        out.writeByte(v);
        return this;
    }

    /**
     * Writes out the string to the underlying output stream as a
     * sequence of bytes. Each character in the string is written out, in
     * sequence, by discarding its high eight bits. If no exception is
     * thrown, the counter <code>written</code> is incremented by the
     * length of <code>s</code>.
     *
     * @param s a string of bytes to be written.
     * @throws IOException if an I/O error occurs.
     * @see FilterOutputStream#out
     */
    @SneakyThrows
    public TCPConnection writeBytes(String s) {
        out.writeBytes(s);
        return this;
    }

    /**
     * Writes a <code>char</code> to the underlying output stream as a
     * 2-byte value, high byte first. If no exception is thrown, the
     * counter <code>written</code> is incremented by <code>2</code>.
     *
     * @param v a <code>char</code> value to be written.
     * @throws IOException if an I/O error occurs.
     * @see FilterOutputStream#out
     */
    @SneakyThrows
    public TCPConnection writeChar(int v) {
        out.writeChar(v);
        return this;
    }

    /**
     * Writes a string to the underlying output stream as a sequence of
     * characters. Each character is written to the data output stream as
     * if by the <code>writeChar</code> method. If no exception is
     * thrown, the counter <code>written</code> is incremented by twice
     * the length of <code>s</code>.
     *
     * @param s a <code>String</code> value to be written.
     * @throws IOException if an I/O error occurs.
     * @see DataOutputStream#writeChar(int)
     * @see FilterOutputStream#out
     */
    @SneakyThrows
    public TCPConnection writeChars(String s) {
        out.writeChars(s);
        return this;
    }

    /**
     * Converts the double argument to a <code>long</code> using the
     * <code>doubleToLongBits</code> method in class <code>Double</code>,
     * and then writes that <code>long</code> value to the underlying
     * output stream as an 8-byte quantity, high byte first. If no
     * exception is thrown, the counter <code>written</code> is
     * incremented by <code>8</code>.
     *
     * @param v a <code>double</code> value to be written.
     * @throws IOException if an I/O error occurs.
     * @see FilterOutputStream#out
     * @see Double#doubleToLongBits(double)
     */
    @SneakyThrows
    public TCPConnection writeDouble(double v) {
        out.writeDouble(v);
        return this;
    }

    /**
     * Converts the float argument to an <code>int</code> using the
     * <code>floatToIntBits</code> method in class <code>Float</code>,
     * and then writes that <code>int</code> value to the underlying
     * output stream as a 4-byte quantity, high byte first. If no
     * exception is thrown, the counter <code>written</code> is
     * incremented by <code>4</code>.
     *
     * @param v a <code>float</code> value to be written.
     * @throws IOException if an I/O error occurs.
     * @see FilterOutputStream#out
     * @see Float#floatToIntBits(float)
     */
    @SneakyThrows
    public TCPConnection writeFloat(float v) {
        out.writeFloat(v);
        return this;
    }

    /**
     * Writes an <code>int</code> to the underlying output stream as four
     * bytes, high byte first. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>4</code>.
     *
     * @param v an <code>int</code> to be written.
     * @throws IOException if an I/O error occurs.
     * @see FilterOutputStream#out
     */
    @SneakyThrows
    public TCPConnection writeInt(int v) {
        out.writeInt(v);
        return this;
    }

    /**
     * Writes a <code>long</code> to the underlying output stream as eight
     * bytes, high byte first. In no exception is thrown, the counter
     * <code>written</code> is incremented by <code>8</code>.
     *
     * @param v a <code>long</code> to be written.
     * @throws IOException if an I/O error occurs.
     * @see FilterOutputStream#out
     */
    @SneakyThrows
    public TCPConnection writeLong(long v) {
        out.writeLong(v);
        return this;
    }

    /**
     * Writes a <code>short</code> to the underlying output stream as two
     * bytes, high byte first. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>2</code>.
     *
     * @param v a <code>short</code> to be written.
     * @throws IOException if an I/O error occurs.
     * @see FilterOutputStream#out
     */
    @SneakyThrows
    public TCPConnection writeShort(int v) {
        out.writeShort(v);
        return this;
    }

    /**
     * Writes a string to the underlying output stream using
     * <a href="DataInput.html#modified-utf-8">modified UTF-8</a>
     * encoding in a machine-independent manner.
     * <p>
     * First, two bytes are written to the output stream as if by the
     * <code>writeShort</code> method giving the number of bytes to
     * follow. This value is the number of bytes actually written out,
     * not the length of the string. Following the length, each character
     * of the string is output, in sequence, using the modified UTF-8 encoding
     * for the character. If no exception is thrown, the counter
     * <code>written</code> is incremented by the total number of
     * bytes written to the output stream. This will be at least two
     * plus the length of <code>str</code>, and at most two plus
     * thrice the length of <code>str</code>.
     *
     * @param str a string to be written.
     * @throws IOException if an I/O error occurs.
     */
    @SneakyThrows
    public TCPConnection writeUTF(String str) {
        out.writeUTF(str);
        return this;
    }

}
