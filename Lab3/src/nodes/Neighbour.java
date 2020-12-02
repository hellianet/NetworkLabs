package nodes;

import java.net.InetAddress;
import java.util.Objects;

public class Neighbour {
    private InetAddress ip;
    private int port;
    private long lastTime;
    private Neighbour alternate;

    public Neighbour(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
        this.lastTime = 0;
    }

    public Neighbour(InetAddress ip, int port, long lastTime) {
        this.ip = ip;
        this.port = port;
        this.lastTime = lastTime;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public Neighbour getAlternate() {
        return alternate;
    }

    public void setAlternate(Neighbour alternate) {
        this.alternate = alternate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Neighbour neighbour = (Neighbour) obj;
        return port == neighbour.port &&
                Objects.equals(ip, neighbour.ip);
    }
}