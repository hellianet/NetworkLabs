package ru.lanchukovskaya.sample.network;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.lanchukovskaya.sample.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Set;

public class AnnouncementMessagesReceiver {
    private final Set<SnakesProto.GameMessage.AnnouncementMsg> announcementMsgSet = new LinkedHashSet<>();
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
                byte[] buffer = new byte[1024];
                while (!Thread.currentThread().isInterrupted()) {
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, 1024);
                    socket.receive(datagramPacket);
                    Node sender = new Node(datagramPacket.getAddress(), datagramPacket.getPort());
                    SnakesProto.GameMessage.AnnouncementMsg announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.parseFrom(buffer);
                    announcementMsgSet.add(announcementMsg);
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }


    public void shareAnnouncementMessages() {
        Set<SnakesProto.GameMessage.AnnouncementMsg> copyOfSet = Set.copyOf(announcementMsgSet);
        announcementMsgSet.clear();
        gameNode.showAnnouncementMessages(copyOfSet);
    }
}
