import java.net.InetAddress;
import java.util.Objects;

public class Location {
    private InetAddress address;
    private int port;
    Location(InetAddress address, int port){
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location location = (Location) o;
        return port == location.port &&
                Objects.equals(address, location.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }

    @Override
    public String toString() {
        return "Location{" +
                "address=" + address +
                ", port=" + port +
                '}';
    }
}
