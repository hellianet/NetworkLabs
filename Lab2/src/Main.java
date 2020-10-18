import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        Client cl = new Client("s1200.jpg", 1555, InetAddress.getLocalHost());
        cl.startWorkWithClient();
    }
}
