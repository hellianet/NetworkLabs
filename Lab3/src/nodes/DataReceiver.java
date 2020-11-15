package nodes;

import messages.AlternateMessage;
import messages.Message;
import messages.TextMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

class DataReceiver extends Thread {
    private static final long MASSAGE_SAVE_TIME = 10000;
    private final ChatNode chatNode;
    private final Map<String, Long> receivedMessages = new HashMap<>();

    public DataReceiver(ChatNode chatNode) {
        this.chatNode = chatNode;
    }

    @Override
    public void run() {
        try {
            Random random = new Random(System.currentTimeMillis());
            while (!isInterrupted()) {
                int size = 1024;
                DatagramPacket packet = new DatagramPacket(new byte[size], size);
                chatNode.nodeSocket.receive(packet);
                if (random.nextInt(100) > chatNode.lossPercent) {
                    Message message = SerializationUtils.deserialize(packet.getData());
                    switch (message.getType()) {
                        case CONNECT:
                            checkingConnectionDelivery(message, packet.getAddress(), packet.getPort());
                            shareAltNeighbour(packet.getAddress(), packet.getPort());
                            chatNode.dataSender.sendConfirmMessage(message.getMessageID(), packet.getAddress(), packet.getPort());
                            break;

                        case ALTERNATE:
                            AlternateMessage alternateMessage = (AlternateMessage) message;
                            setAlternate(packet.getAddress(), packet.getPort(), alternateMessage.getIp(), alternateMessage.getPort());
                            chatNode.dataSender.sendConfirmMessage(message.getMessageID(), packet.getAddress(), packet.getPort());
                            break;

                        case ALIVE:
                            updateNeighbourAliveTime(packet.getAddress(), packet.getPort());
                            chatNode.dataSender.sendConfirmMessage(message.getMessageID(), packet.getAddress(), packet.getPort());
                            break;

                        case CONFIRM:
                            checkingConfirmDelivery(message);
                            break;

                        case TEXT:
                            checkingTextDelivery(message, packet.getAddress(), packet.getPort());
                            chatNode.dataSender.sendConfirmMessage(message.getMessageID(), packet.getAddress(), packet.getPort());
                            break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void shareAltNeighbour(InetAddress inetAddress, int port) throws IOException {
        if (null == chatNode.alternate) {
            if (chatNode.neighboursList.size() > 0) {
                chatNode.alternate = chatNode.neighboursList.get(0);
                chatNode.dataSender.sendAlternateToAllNeighbours(chatNode.alternate.getIp(), chatNode.alternate.getPort());
            }
        } else {
            if (!chatNode.alternate.equals(new Neighbour(inetAddress, port))) {
                chatNode.dataSender.sendAlternate(UUID.randomUUID().toString(), chatNode.alternate.getIp(), chatNode.alternate.getPort(), inetAddress, port);
            }
        }
    }

    private void checkingConfirmDelivery(Message message) {
        synchronized (chatNode.confirmedMessages) {
            if (!chatNode.confirmedMessages.contains(message.getMessageID())) {
                chatNode.confirmedMessages.add(message.getMessageID());
            }
        }
    }

    private void checkingTextDelivery(Message message, InetAddress inetAddress, int port) throws IOException {
        receivedMessages.entrySet().removeIf(msg -> System.currentTimeMillis() - msg.getValue() > MASSAGE_SAVE_TIME);
        if (!receivedMessages.containsKey(message.getMessageID())) {
            System.out.println(((TextMessage) message).getName() + ":" + ((TextMessage) message).getContent());
            receivedMessages.put(message.getMessageID(), System.currentTimeMillis());
            chatNode.dataSender.sendTextNext((TextMessage) message, inetAddress, port);
        }
    }


    private void checkingConnectionDelivery(Message message, InetAddress inetAddress, int port) {
        receivedMessages.entrySet().removeIf(msg -> System.currentTimeMillis() - msg.getValue() > MASSAGE_SAVE_TIME);
        if (!receivedMessages.containsKey(message.getMessageID())) {
            Neighbour neighbour = new Neighbour(inetAddress, port, System.currentTimeMillis());
            chatNode.neighboursList.add(neighbour);
            receivedMessages.put(message.getMessageID(), System.currentTimeMillis());
        }
    }

    private void setAlternate(InetAddress senderIp, int senderPort, InetAddress alternateIp, int alternatePort) {
        for (Neighbour neighbour : chatNode.neighboursList) {
            if (neighbour.getPort() == senderPort && neighbour.getIp().equals(senderIp)) {
                neighbour.setAlternate(new Neighbour(alternateIp, alternatePort, System.currentTimeMillis()));
            }
        }

    }

    private void updateNeighbourAliveTime(InetAddress inetAddress, int port) {
        for (Neighbour neighbour : chatNode.neighboursList) {
            if (neighbour.getIp().equals(inetAddress) && port == neighbour.getPort()) {
                neighbour.setLastTime(System.currentTimeMillis());
                break;
            }
        }
    }
}
