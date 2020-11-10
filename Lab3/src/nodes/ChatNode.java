package nodes;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
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
    final Map<String, Long> receivedMessages = new HashMap<>();

    private final int DELIVERY_DELAY = 10000;
    private final int MASSAGE_SAVE_TIME = DELIVERY_DELAY * 5;

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

    public List<String> getConfirmedMessages() {
        return confirmedMessages;
    }

    public List<Neighbour> getNeighboursList() {
        return neighboursList;
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
            startNotifyNeighboursAboutLiveStatus();
            checkingReceivedMessages();
            checkingNeighbours();
            dataSender.start();
            dataReceiver.start();
            Scanner scanner = new Scanner(System.in);
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

    private void startNotifyNeighboursAboutLiveStatus() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    dataSender.shareAliveStatusToAllNeighbours();
                    Thread.sleep(100);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void checkingReceivedMessages() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(DELIVERY_DELAY);
                    synchronized (receivedMessages) {
                        receivedMessages.entrySet().removeIf(msg -> System.currentTimeMillis() - msg.getValue() > MASSAGE_SAVE_TIME);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void checkingNeighbours() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    helpForCheckingNeighbours();
                    Thread.sleep(1000);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }


    private void helpForCheckingNeighbours() throws IOException {
        boolean WasNeighbourChange = false;
        for (Neighbour neighbour : neighboursList) {
            int TIMEOUT = 5000;
            if (System.currentTimeMillis() - neighbour.getLastTime() > TIMEOUT) {

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
