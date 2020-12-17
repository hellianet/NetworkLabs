package ru.lanchukovskaya.sample.network;

import ru.lanchukovskaya.sample.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class Receiver implements Runnable {
    private static final int PACKET_SIZE = 4094;
    private final Storage storage;

    private final DatagramSocket socket;

    public Receiver(Storage storage, DatagramSocket socket) {
        this.storage = storage;
        this.socket = socket;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
            try {
                socket.receive(packet);
                Node sender = new Node(packet.getAddress(), packet.getPort());
                SnakesProto.GameMessage message = SnakesProto.GameMessage.parseFrom(packet.getData());
                storage.addReceivedMessage(sender, message);
            } catch (IOException ignored) {
            }
        }
    }
}
