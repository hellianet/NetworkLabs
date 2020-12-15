package connection;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class DirectConnection extends Connection {
    private SocketChannel channel;
    private ConnectionBuffer bufRead;
    private ConnectionBuffer bufWrite;

    public DirectConnection(ConnectionSelector connectionSelector, SocketChannel socketChannel, ConnectionBuffer bufferRead, ConnectionBuffer bufferWrite) {
        super(connectionSelector);
        channel = socketChannel;
        bufRead = bufferRead;
        bufWrite = bufferWrite;
        bufRead.setReader(this);
        bufWrite.setWriter(this);
    }

    @Override
    public void make(SelectionKey key) {
        if (key.isValid() && key.isReadable()) {
            try {
                if (!bufWrite.readFromChannelToBuffer(channel)) {
                    finish(key);
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                finish(key);
                return;
            }
        }

        if (key.isValid() && key.isWritable()) {
            try {
                bufRead.writeToChannelFromBuffer(channel);
            } catch (AsynchronousCloseException eBoy) {
                eBoy.printStackTrace();
                connectionSelector.registerConnection(channel, this, SelectionKey.OP_READ);
            } catch (IOException e) {
                e.printStackTrace();
                finish(key);
            }
        }

    }

    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public void finish() {
        connectionSelector.deleteConnection(channel);
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
