package ru.lanchukovskaya.sample.network;


import ru.lanchukovskaya.sample.SnakesProto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    private final Map<SnakesProto.GameMessage, Node> receivedMessages = new ConcurrentHashMap<>();
    private final Map<SnakesProto.GameMessage, Node> sentMessages = new ConcurrentHashMap<>();
    private final Map<SnakesProto.GameMessage, Node> messagesToSend = new ConcurrentHashMap<>();

    private void removeAllReceivedMessages() {
        receivedMessages.clear();
    }

    public void addMessageToSend(Node receiver, SnakesProto.GameMessage gameMessage) {
        messagesToSend.put(gameMessage, receiver);
    }

    public void addReceivedMessage(Node sender, SnakesProto.GameMessage gameMessage) {
        if (gameMessage.hasAck()) {
            removeConfirmedMessage(gameMessage);
            return;
        }
        receivedMessages.put(gameMessage, sender);
    }

    private void removeConfirmedMessage(SnakesProto.GameMessage ackMessage) {
        sentMessages.keySet().removeIf(message -> message.getMsgSeq() == ackMessage.getMsgSeq());
    }

    public Map<SnakesProto.GameMessage, Node> getMessagesToSend() {
        Map<SnakesProto.GameMessage, Node> messages = Map.copyOf(messagesToSend);
        messagesToSend.clear();
        return messages;
    }

    public Map<SnakesProto.GameMessage, Node> getReceivedMessages() {
        Map<SnakesProto.GameMessage, Node> messages = Map.copyOf(receivedMessages);
        removeAllReceivedMessages();
        return messages;
    }

    public void resendUnconfirmedMessages() {
        messagesToSend.putAll(sentMessages);
        sentMessages.clear();
    }
}
