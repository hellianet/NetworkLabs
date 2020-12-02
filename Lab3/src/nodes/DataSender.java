package nodes;

import messages.AliveMessage;
import messages.AlternateMessage;
import messages.ConfirmMessage;
import messages.TextMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class DataSender extends Thread {

    private final ChatNode chatNode;
    private final Map<String, DatagramPacket> sentMessages = new ConcurrentHashMap<>();

    public DataSender(ChatNode chatNode) {
        this.chatNode = chatNode;
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {
            try {
                sentMessages.entrySet().removeIf(msg -> checkingDelivery(msg.getKey()) ||
                        !chatNode.neighboursList.contains(new Neighbour(msg.getValue().getAddress(), msg.getValue().getPort())));
                for (DatagramPacket datagramPacket : sentMessages.values()) {
                    try {
                        if (chatNode.nodeSocket == null || chatNode.nodeSocket.isClosed()) {
                            Thread.currentThread().interrupt();
                            System.err.println("Socket is closed");
                            return;
                        }
                        chatNode.nodeSocket.send(datagramPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean checkingDelivery(String messageID) {
        synchronized (chatNode.confirmedMessages) {
            boolean status = chatNode.confirmedMessages.contains(messageID);
            if (status) {
                chatNode.confirmedMessages.remove(messageID);
            }
            return status;
        }
    }

    private void sendMessage(String messageID, DatagramPacket datagramPacket) {
        sentMessages.put(messageID, datagramPacket);
    }


    void sendTextToAllNeighbours(String content) throws IOException {
        for (Neighbour neighbour : chatNode.neighboursList) {
            String messageID = UUID.randomUUID().toString();
            byte[] confirmAnswer = SerializationUtils.serialize(new TextMessage(content, messageID, chatNode.nodeName));
            sendMessage(messageID, new DatagramPacket(confirmAnswer, confirmAnswer.length, neighbour.getIp(), neighbour.getPort()));
        }
    }

    void shareAliveStatusToAllNeighbours() throws IOException {
        for (Neighbour neighbour : chatNode.neighboursList) {
            String messageID = UUID.randomUUID().toString();
            byte[] aliveMsg = SerializationUtils.serialize(new AliveMessage(messageID));
            sendMessage(messageID, new DatagramPacket(aliveMsg, aliveMsg.length, neighbour.getIp(), neighbour.getPort()));
        }
    }

    void sendAlternateToAllNeighbours(InetAddress ip, int port) throws IOException {
        for (Neighbour neighbour : chatNode.neighboursList) {
            if (!chatNode.alternate.equals(neighbour)) {
                sendAlternate(UUID.randomUUID().toString(), ip, port, neighbour.getIp(), neighbour.getPort());
            }
        }
    }

    void sendAlternate(String messageID, InetAddress alterIp, int alterPort, InetAddress distIp, int distPort) throws IOException {
        byte[] alternateMsg = SerializationUtils.serialize(new AlternateMessage(messageID, alterIp, alterPort));
        sendMessage(messageID, new DatagramPacket(alternateMsg, alternateMsg.length, distIp, distPort));
    }


    void sendConfirmMessage(String messageID, InetAddress ip, int port) throws IOException {
        byte[] confirmAnswer = SerializationUtils.serialize(new ConfirmMessage(messageID));
        chatNode.nodeSocket.send(new DatagramPacket(confirmAnswer, confirmAnswer.length, ip, port));
    }

    void sendTextNext(TextMessage oldTextMsg, InetAddress neighbourSenderIp, int neighbourSenderPort) throws IOException {
        for (Neighbour neighbour : chatNode.neighboursList) {
            if (neighbour.getPort() != neighbourSenderPort && neighbour.getIp() != neighbourSenderIp) {
                String messageID = UUID.randomUUID().toString();
                byte[] newTextMsg = SerializationUtils.serialize(new TextMessage(oldTextMsg.getContent(), messageID, oldTextMsg.getName()));

                DatagramPacket packet = new DatagramPacket(
                        newTextMsg, newTextMsg.length,
                        neighbour.getIp(), neighbour.getPort()
                );

                sendMessage(messageID, packet);
            }
        }
    }
}
