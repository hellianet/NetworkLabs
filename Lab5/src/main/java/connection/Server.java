package connection;

import java.io.IOException;

public class Server implements Runnable {

    private ConnectionSelector connectionSelector;

    public Server(int port) {
        try {
            connectionSelector = new ConnectionSelector();
            new ServerConnection(connectionSelector, port);
            DNSConnection.createInstance(connectionSelector);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                connectionSelector.iterateOverConnections();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            connectionSelector.exit();
        }
    }

}
