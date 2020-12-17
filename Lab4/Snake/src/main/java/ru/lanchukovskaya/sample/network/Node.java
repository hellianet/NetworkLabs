package ru.lanchukovskaya.sample.network;

import java.net.InetAddress;
import java.util.Objects;

public class Node {
    private final InetAddress address;
    private final int port;

    public Node(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    InetAddress getAddress() {
        return address;
    }

    int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return port == node.port &&
                address.equals(node.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }
}
