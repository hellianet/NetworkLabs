package ru.lanchukovskaya.sample.network;


import ru.lanchukovskaya.sample.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;

public class Sender implements Runnable {
    private final Storage storage;

    private final DatagramSocket socket;
    private final int nodeTimeoutMs;

    public Sender(Storage storage, DatagramSocket socket, int nodeTimeoutMs) {
        this.storage = storage;
        this.socket = socket;
        this.nodeTimeoutMs = nodeTimeoutMs;
    }


    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            storage.resendUnconfirmedMessages();
            sendMessages();
            try {
                Thread.sleep(nodeTimeoutMs);
            } catch (InterruptedException e) {
                sendMessages();
                return;
            }
        }
    }

    public void addMessageToSend(Node receiver, SnakesProto.GameMessage message) {
        storage.addMessageToSend(receiver, message);
    }

    private void sendMessages() {
        Map<SnakesProto.GameMessage, Node> messagesToSend = storage.getMessagesToSend();
        for (Map.Entry<SnakesProto.GameMessage, Node> entry : messagesToSend.entrySet()) {
            SnakesProto.GameMessage gameMessage = entry.getKey();
            Node node = entry.getValue();
            sendMessage(node, gameMessage);
        }
    }

    private void sendMessage(Node receiver, SnakesProto.GameMessage message) {
        byte[] messageBytes = message.toByteArray();
        DatagramPacket packet = new DatagramPacket(
                messageBytes,
                messageBytes.length,
                receiver.getAddress(),
                receiver.getPort()
        );
        try {
            socket.send(packet);
        } catch (IOException ignored) {
        }
    }
}
