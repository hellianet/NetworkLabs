import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) throws IOException {
        InetAddress inetAddress = InetAddress.getByName(args[0]);
        if(!inetAddress.isMulticastAddress())
        {
            System.out.println("InetAddress isn't multicast");
            System.exit(1);
        }
        Search search = new Search(inetAddress);
        String mes = "Hello, I reached the port!";
        search.dataTransfer(mes);

    }


}
