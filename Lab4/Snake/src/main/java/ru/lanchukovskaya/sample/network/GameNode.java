package ru.lanchukovskaya.sample.network;

import ru.lanchukovskaya.sample.Observable;
import ru.lanchukovskaya.sample.Observer;
import ru.lanchukovskaya.sample.*;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class GameNode implements Observable {

    private SnakesProto.GameConfig gameConfig;
    private final AnnouncementMessagesReceiver announcementMessagesReceiver;
    private DatagramSocket socket;
    private Sender sender;
    private Receiver receiver;
    private Storage storage;
    private Timer timer = new Timer();
    private Node master;
    private long lastMasterTime;
    private NodeWithRole currentNode;
    private SnakesProto.NodeRole currentNodeRole;
    private SnakesProto.GameState currentGameState;
    private List<Observer> observers = new ArrayList<>();
    private Thread threadForSend;
    private Thread threadForReceiving;
    private View view;


    public GameNode(SnakesProto.GameConfig gameConfig, SnakesProto.NodeRole role, int port) {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.gameConfig = gameConfig;
        storage = new Storage();
        sender = new Sender(storage, socket, gameConfig.getNodeTimeoutMs());
        receiver = new Receiver(storage, socket);
        announcementMessagesReceiver = new AnnouncementMessagesReceiver("239.192.0.4", 9192, this);
        changeRole(role);
        startAllThreads();
    }

    public void setView(View view) {
        this.view = view;
    }

    private void startAllThreads() {
        threadForSend = new Thread(sender);
        threadForReceiving = new Thread(receiver);
        threadForReceiving.setName("ReceiverThread");
        threadForSend.setName("SenderThread");
        threadForSend.start();
        threadForReceiving.start();
        timerProcessReceivingMessagesStart();
        timerSendMessagesStart();
        announcementMessagesReceiver.start();
    }

    private void timerProcessReceivingMessagesStart() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                for (Map.Entry<SnakesProto.GameMessage, Node> entry : storage.getReceivedMessages().entrySet()) {
                    Node node = entry.getValue();
                    SnakesProto.GameMessage message = entry.getKey();
                    if (message.hasState() || message.hasRoleChange() || message.hasSteer() || message.hasJoin()) {
                        sender.addMessageToSend(node, SnakesProto.GameMessage.newBuilder()
                                .setAck(SnakesProto.GameMessage.AckMsg.newBuilder().build())
                                .setMsgSeq(message.getMsgSeq())
                                .build());
                    }
                    currentNode.processMessage(node, message);
                }
            }
        };
        timer.schedule(task, 0, gameConfig.getPingDelayMs());
    }

    public void timerSendMessagesStart() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (master != null) {
                    storage.addMessageToSend(master, generatePingMessage());
                }
            }
        };
        timer.schedule(task, 0, gameConfig.getPingDelayMs());
    }

    private SnakesProto.GameMessage generatePingMessage() {
        return SnakesProto.GameMessage.newBuilder().setPing(SnakesProto.GameMessage.PingMsg.newBuilder().build())
                .setMsgSeq(new Random().nextInt())
                .build();
    }

    public void startGame(String playerName) {
        currentNode = new Master(gameConfig, playerName, this);
    }

    public void joinToGame(Node node, String playerName) {
        sender.addMessageToSend(node, generateJoinMessage(playerName));
        master = node;
        lastMasterTime = System.currentTimeMillis();
        currentNode = createNodeByRole(SnakesProto.NodeRole.NORMAL, gameConfig);
    }

    private SnakesProto.GameMessage generateJoinMessage(String playerName) {
        return SnakesProto.GameMessage.newBuilder()
                .setJoin(SnakesProto.GameMessage.JoinMsg.newBuilder().setName(playerName).build())
                .setMsgSeq(new Random().nextInt())
                .build();
    }

    public void changeRole(SnakesProto.NodeRole nodeRole) {
        if (currentNode != null) {
            currentNode.exit();
        }
        this.currentNodeRole = nodeRole;
        currentNode = createNodeByRole(nodeRole, gameConfig);
    }

    private NodeWithRole createNodeByRole(SnakesProto.NodeRole nodeRole, SnakesProto.GameConfig gameConfig) {
        switch (nodeRole) {
            case MASTER:
                return new Master(gameConfig, this);
            case NORMAL:
            case DEPUTY:
                return new Normal(gameConfig, this);
        }
        return new Normal(gameConfig, this);
    }

    public void becomeMaster() {
        if (currentNodeRole == SnakesProto.NodeRole.DEPUTY) {
            currentNode.exit();
            for (SnakesProto.GamePlayer gamePlayer : currentGameState.getPlayers().getPlayersList()) {
                String playerIpAddress = gamePlayer.getIpAddress();
                if (playerIpAddress.isEmpty()) {
                    continue;
                }
                try {
                    sendMessage(
                            new Node(InetAddress.getByName(playerIpAddress), gamePlayer.getPort()),
                            generateBecomeMasterMessage()
                    );
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }

            changeRole(SnakesProto.NodeRole.MASTER, currentGameState);
        }
    }

    private SnakesProto.GameMessage generateBecomeMasterMessage() {
        return SnakesProto.GameMessage.newBuilder()
                .setRoleChange(SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                        .setReceiverRole(SnakesProto.NodeRole.DEPUTY)
                        .setSenderRole(SnakesProto.NodeRole.NORMAL)
                        .build())
                .build();
    }

    public void changeRole(SnakesProto.NodeRole role, SnakesProto.GameState state) {
        currentNode = new Master(gameConfig, state, this);
        master = null;
    }


    public void sendMessage(Node receiver, SnakesProto.GameMessage message) {
        storage.addMessageToSend(receiver, message);
    }


    public void setMaster(Node node) {
        this.master = node;
    }

    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        SnakesProto.GameState state = this.currentGameState;
        observers.forEach(gameObserver -> gameObserver.update(state));
    }

    public Node getMaster() {
        return master;
    }

    public void makeMove(Movement movement) {
        currentNode.makeMove(movement);
    }

    public void exit() {
        socket.close();
        currentNode.exit();
        threadForReceiving.interrupt();
        threadForSend.interrupt();
        timer.cancel();
        announcementMessagesReceiver.exit();
    }

    public void showState(SnakesProto.GameState state) {
        this.currentGameState = state;
        view.update(state);
        notifyObservers();
    }

    public void markThatMasterAlive() {
        lastMasterTime = System.currentTimeMillis();
    }

    public long getLastMasterAliveTime() {
        return lastMasterTime;
    }

    public void showAnnouncementMessages(Map<Node, SnakesProto.GameMessage.AnnouncementMsg> announcementMsgs) {
        view.addDataToTable(announcementMsgs);
    }

    public void setConfig(SnakesProto.GameConfig config) {
        this.gameConfig = config;
    }
}
