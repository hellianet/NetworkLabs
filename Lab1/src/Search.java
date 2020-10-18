import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Search {

    private InetAddress group;
    private MulticastSocket socket = null;
    private DatagramSocket datagramSocket = null;
    private byte[] buf = new byte[256];
    private byte[] message;
    private HashMap<Location, Long> localMap;

    Search(InetAddress inetAddress) throws IOException {
        group = inetAddress;
        socket = new MulticastSocket(4454);
        datagramSocket = new DatagramSocket();
        localMap = new HashMap<>();
    }

    public void dataTransfer(String multicastMessage) throws IOException {

        message = multicastMessage.getBytes();
        socket.setSoTimeout(100);
        socket.joinGroup(group);
        while(true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            }  catch (SocketTimeoutException e){
                DatagramPacket sendPacket = new DatagramPacket(message, message.length, group,4454);
                datagramSocket.send(sendPacket);
                continue;
            }
             catch (IOException e){
                socket.close();
                datagramSocket.close();
                return;
             }
            Location location = new Location(packet.getAddress(),packet.getPort());
            if(!localMap.containsKey(location))
            {
                System.out.println("New customer:" + location);
            }
            localMap.put(location, System.currentTimeMillis() );
            ArrayList<Location> delKey = new ArrayList<>();
            long time = System.currentTimeMillis();
            for(Map.Entry<Location, Long> pairs: localMap.entrySet()){
                if(time- pairs.getValue() > 1000){
                    delKey.add(pairs.getKey());
                    System.out.println("User deleted:" + location);
                }
            }
            delKey.forEach(localMap::remove);

        }
    }
}
