package connection;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class WaitingConnection extends Connection {

    private SocketChannel channel;
    private SecondConnection secondPhaseConnection;

    public WaitingConnection(ConnectionSelector connectionSelector, SocketChannel channel, SecondConnection secondPhaseConnection, SocketAddress address) throws IOException {
        super(connectionSelector);
        this.channel = channel;
        this.secondPhaseConnection = secondPhaseConnection;
        boolean connected;

        try {
            connected = this.channel.connect(address);
        } catch (AlreadyConnectedException e) {
            this.secondPhaseConnection.setIsConnected(true, this);
            return;
        }

        if (connected) {
            this.secondPhaseConnection.setIsConnected(true, this);
        } else {
            this.connectionSelector.registerConnection(this.channel, this, SelectionKey.OP_CONNECT);
        }

    }

    @Override
    public void make(SelectionKey key) throws IOException {
        if (key.isValid() && key.isConnectable()) {
            try {
                boolean result = channel.finishConnect();
                secondPhaseConnection.setIsConnected(result, this);
            } catch (SocketException e) {
                secondPhaseConnection.setIsConnected(false, this);
            }
            key.cancel();
        }
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

    public SocketChannel getChannel() {
        return channel;
    }
}
