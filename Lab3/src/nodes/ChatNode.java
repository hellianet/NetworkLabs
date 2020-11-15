package nodes;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatNode {

    String nodeName;
    int lossPercent;
    private int port;
    private int neighbourPort;
    private InetAddress inetAddress;
    DatagramSocket nodeSocket;
    Neighbour alternate;
    final List<Neighbour> neighboursList = new CopyOnWriteArrayList<>();
    final List<String> confirmedMessages = new ArrayList<>();

    DataSender dataSender = new DataSender(this);
    private DataReceiver dataReceiver = new DataReceiver(this);

    public ChatNode(String nodeName, int lossPercent, int port) {
        this.nodeName = nodeName;
        this.lossPercent = lossPercent;
        this.port = port;
        this.neighbourPort = -1;
        this.inetAddress = null;
    }

    public ChatNode(String nodeName, int lossPercent, int port, InetAddress inetAddress, int neighbourPort) {
        this.nodeName = nodeName;
        this.lossPercent = lossPercent;
        this.port = port;
        this.neighbourPort = neighbourPort;
        this.inetAddress = inetAddress;
    }

    public void startChat() {
        try {
            nodeSocket = new DatagramSocket(this.port);
            if (neighbourPort != -1 && inetAddress != null) {
                neighboursList.add(new Neighbour(inetAddress, neighbourPort, System.currentTimeMillis()));
                dataSender.sendConnectMessage(UUID.randomUUID().toString(), inetAddress, neighbourPort);
                dataSender.sendAlternate(UUID.randomUUID().toString(), inetAddress, neighbourPort, inetAddress, neighbourPort);
                alternate = new Neighbour(inetAddress, neighbourPort, System.currentTimeMillis());
            }
            startNotifyAndCheckNeighbours();
            dataSender.start();
            dataReceiver.start();
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            while (scanner.hasNextLine()) {
                String text = scanner.nextLine();
                dataSender.sendTextToAllNeighbours(text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (nodeSocket != null) {
                nodeSocket.close();
            }
        }

    }

    private void startNotifyAndCheckNeighbours() {
        new Thread(() -> {
            long lastCheckingNeighboursTime = System.currentTimeMillis();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - lastCheckingNeighboursTime > 1000) {
                        helpForCheckingNeighbours();
                        lastCheckingNeighboursTime = nowTime;
                    }
                    dataSender.shareAliveStatusToAllNeighbours();
                    Thread.sleep(100);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void helpForCheckingNeighbours() throws IOException {
        boolean WasNeighbourChange = false;
        for (Neighbour neighbour : neighboursList) {
            int timeout = 5000;
            if (System.currentTimeMillis() - neighbour.getLastTime() > timeout) {

                if (neighbour.equals(alternate)) {
                    WasNeighbourChange = true;
                }

                Neighbour newNeighbour = neighbour.getAlternate();

                if (newNeighbour != null) {
                    newNeighbour.setLastTime(System.currentTimeMillis());
                    neighboursList.add(newNeighbour);
                    dataSender.sendConnectMessage(UUID.randomUUID().toString(), newNeighbour.getIp(), newNeighbour.getPort());
                }
                neighboursList.remove(neighbour);
            }
        }

        if (WasNeighbourChange) {
            if (neighboursList.size() > 0) {
                alternate = neighboursList.get(0);
                dataSender.sendAlternateToAllNeighbours(alternate.getIp(), alternate.getPort());
            } else {
                alternate = null;
            }
        }

    }

}
