package ru.lanchukovskaya.sample.network;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.lanchukovskaya.sample.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class AnnouncementMessagesReceiver {
    private final Map<Node, SnakesProto.GameMessage.AnnouncementMsg> announcementMsgSet = new HashMap<>();
    private final InetAddress multicastAddress;
    private final int port;
    private final Thread checkerThread;
    private final GameNode gameNode;

    public AnnouncementMessagesReceiver(String multicastHost, int port, GameNode gameNode) {
        this.gameNode = gameNode;
        try {
            multicastAddress = InetAddress.getByName(multicastHost);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Cant parse multicast address");
        }
        this.port = port;
        this.checkerThread = new Thread(getTaskForThread());
    }

    public void start() {
        checkerThread.start();
    }

    public void exit() {
        checkerThread.interrupt();
    }

    private Runnable getTaskForThread() {
        return () -> {
            try (MulticastSocket socket = new MulticastSocket(port)) {
                socket.joinGroup(multicastAddress);

                while (!Thread.currentThread().isInterrupted()) {
                    DatagramPacket datagramPacket = new DatagramPacket(new byte[4096], 4096);
                    socket.receive(datagramPacket);
                    Node sender = new Node(datagramPacket.getAddress(), datagramPacket.getPort());
                    SnakesProto.GameMessage message = SnakesProto.GameMessage.parseFrom(datagramPacket.getData());
                    announcementMsgSet.put(sender, message.getAnnouncement());
                    shareAnnouncementMessages();
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }


    private void shareAnnouncementMessages() {
        Map<Node, SnakesProto.GameMessage.AnnouncementMsg> copyOfSet = Map.copyOf(announcementMsgSet);
        announcementMsgSet.clear();
        gameNode.showAnnouncementMessages(copyOfSet);
    }
}
