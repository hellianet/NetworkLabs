package connection;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelectableChannel;

public abstract class Connection {
    protected ConnectionSelector connectionSelector;

    public Connection(ConnectionSelector connectionSelector) {
        this.connectionSelector = connectionSelector;
    }

    public abstract void make(SelectionKey key) throws IOException;

    public void finish(SelectionKey key) {
        connectionSelector.deleteConnection((AbstractSelectableChannel) key.channel());
    }

    public abstract void finish();
}
