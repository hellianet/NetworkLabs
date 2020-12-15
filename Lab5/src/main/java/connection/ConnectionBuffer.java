package connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ConnectionBuffer {
    private final int capacity = 16384;
    private byte[] buf = new byte[capacity];
    private DirectConnection reader;
    private DirectConnection writer;
    private int writeDisplacement = 0;
    private int readDisplacement = 0;
    private int filled = 0;
    private boolean shutdown = false;

    public void setReader(DirectConnection reader) {
        this.reader = reader;
    }

    public void setWriter(DirectConnection writer)
    {
        this.writer = writer;
    }

    private ByteBuffer[] getByteBuffersArrayToRead() {
        if (writeDisplacement < readDisplacement) {
            ByteBuffer[] result = new ByteBuffer[1];
            result[0] = ByteBuffer.wrap(buf, writeDisplacement, readDisplacement - writeDisplacement);
            return result;
        } else {
            if (readDisplacement == 0) {
                ByteBuffer[] result = new ByteBuffer[1];
                result[0] = ByteBuffer.wrap(buf, writeDisplacement, capacity - writeDisplacement);
                return result;
            } else {
                ByteBuffer[] result = new ByteBuffer[2];
                result[0] = ByteBuffer.wrap(buf, writeDisplacement, capacity - writeDisplacement);
                result[1] = ByteBuffer.wrap(buf, 0, readDisplacement);
                return result;
            }
        }
    }

    private ByteBuffer[] getByteBuffersArrayToWrite() {
        if (writeDisplacement < readDisplacement) {
            ByteBuffer[] result = new ByteBuffer[2];
            result[0] = ByteBuffer.wrap(buf, readDisplacement, capacity - readDisplacement);
            result[1] = ByteBuffer.wrap(buf, 0, writeDisplacement);
            return result;
        } else {
            ByteBuffer[] result = new ByteBuffer[1];
            result[0] = ByteBuffer.wrap(buf, readDisplacement, writeDisplacement - readDisplacement);
            return result;
        }
    }

    private void enableOption(DirectConnection directConnection, int opt) {
        if (directConnection == null) return;
        directConnection.connectionSelector.enableOpt(reader.getChannel(), opt);
    }

    private void disableOption(DirectConnection directConnection, int opt) {
        if (directConnection == null) return;
        directConnection.connectionSelector.disableOpt(reader.getChannel(), opt);
    }


    public boolean readFromChannelToBuffer(SocketChannel channel) throws IOException {
        long read = channel.read(getByteBuffersArrayToRead());
        if (read == -1) {
            shutdown = true;
            disableOption(reader, SelectionKey.OP_READ);
            return false;
        }
        if (read == 0) { return true;}
        filled += read;
        if (filled == capacity) {
            disableOption(writer, SelectionKey.OP_READ);
        }
        enableOption(reader, SelectionKey.OP_WRITE);
        writeDisplacement = (writeDisplacement + (int) read) % capacity;
        return true;
    }

    public void writeToChannelFromBuffer(SocketChannel channel) throws IOException {
        long written = channel.write(getByteBuffersArrayToWrite());
        if (shutdown && (written == 0)) {
            channel.shutdownOutput();
            disableOption(writer, SelectionKey.OP_WRITE);
            return;
        }
        if (written > 0) {
            enableOption(reader, SelectionKey.OP_READ);
        }
        filled -= written;
        if (filled == 0) {
            disableOption(reader, SelectionKey.OP_WRITE);
        }
        readDisplacement = (readDisplacement + (int) written) % capacity;
    }

}
