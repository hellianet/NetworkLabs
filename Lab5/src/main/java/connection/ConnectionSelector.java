package connection;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ConnectionSelector {
    private HashMap<AbstractSelectableChannel, Connection> connectionMap = new HashMap<>();
    private Selector selector;

    public ConnectionSelector() throws IOException {
        selector = Selector.open();
    }

    public void deleteConnection(AbstractSelectableChannel channel) {
        connectionMap.remove(channel);
    }

    public void enableOpt(AbstractSelectableChannel channel, int opt) {
        if (channel != null) {
            try {
                channel.register(selector, channel.validOps() | opt);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        }
    }

    public void disableOpt(AbstractSelectableChannel channel, int opt) {
        if (channel != null) {
            try {
                channel.register(selector, channel.validOps() & ~opt);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerConnection(AbstractSelectableChannel channel, Connection connection, int opts) {
        try {
            channel.register(selector, opts);
        } catch (IOException e) {
            e.printStackTrace();
            connection.finish();
            return;
        }
        connectionMap.put(channel, connection);
    }

    public void iterateOverConnections() throws IOException {
        selector.select();
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();

            Connection connection = connectionMap.get(key.channel());
            if (connection != null) {
                connection.make(key);
            }
            keyIterator.remove();
        }
    }

    public void exit() {
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Map.Entry<AbstractSelectableChannel, Connection> entry : connectionMap.entrySet()) {
            try {
                entry.getKey().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        connectionMap.clear();
    }
}
