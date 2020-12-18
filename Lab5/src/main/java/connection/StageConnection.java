package connection;

import java.nio.channels.SocketChannel;

public abstract class StageConnection extends Connection {
    protected SocketChannel channel;

    public StageConnection(ConnectionSelector connectionSelector, SocketChannel channel) {
        super(connectionSelector);
        this.channel = channel;
    }
}
