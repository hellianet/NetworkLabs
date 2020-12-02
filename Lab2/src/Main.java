import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args)  {
        try {
            InetAddress inetAddress = InetAddress.getByName(args[0]);
            int port = Integer.parseInt(args[1]);
            String path = args[2];
            Client cl = new Client(path, port, inetAddress);
            cl.startWorkWithClient();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
