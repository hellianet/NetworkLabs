import nodes.ChatNode;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws UnknownHostException {
        if (args.length != 3 && args.length != 5) {
            System.out.println("Error: wrong count argument");
        }
        String nodeName = args[0];
        try {
            int lossPercent = Integer.parseInt(args[1]);
            int port = Integer.parseInt(args[2]);
            if (args.length == 3) {
                ChatNode chatNodeSingle = new ChatNode(nodeName, lossPercent, port);
                chatNodeSingle.startChat();

            } else if (args.length == 5) {
                InetAddress inetAddress = InetAddress.getByName(args[3]);
                int neighbourPort = Integer.parseInt(args[4]);
                ChatNode chatNodeWithCouple = new ChatNode(nodeName, lossPercent, port, inetAddress, neighbourPort);
                chatNodeWithCouple.startChat();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
