package connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerConnection extends Connection {

    public ServerConnection(ConnectionSelector connectionSelector, int port) throws IOException {
        super(connectionSelector);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));
        connectionSelector.registerConnection(serverSocketChannel, this, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void make(SelectionKey key) throws IOException {
        if (key.isValid() && key.isAcceptable()) {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();

            if (socketChannel != null) {
                socketChannel.configureBlocking(false);

                connectionSelector.registerConnection(socketChannel,
                        new FirstConnection(connectionSelector, socketChannel), SelectionKey.OP_READ);
            }
        }
    }

    @Override
    public void finish() {
    }
}
