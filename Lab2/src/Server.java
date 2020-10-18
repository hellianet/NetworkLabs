import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    public static LinkedList<HelpForServer> serverList = new LinkedList<>();

    public void workServer(int port)  {
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                Socket socket = server.accept();
                serverList.add(new HelpForServer(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}