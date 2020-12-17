package ru.lanchukovskaya.sample.network;

import ru.lanchukovskaya.sample.Observer;
import ru.lanchukovskaya.sample.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Master implements Observer, NodeWithRole {
    private final Game game;
    private final Timer timer;
    private SnakesProto.GameConfig gameConfig;
    private Map<Player, SnakesProto.GamePlayer> playerMap = new ConcurrentHashMap<>();
    private Map<Player, Movement> movementMap = new ConcurrentHashMap<>();
    private Map<Node, Player> nodePlayerMap = new ConcurrentHashMap<>();
    private Map<Node, Long> lastAliveTimeMap = new ConcurrentHashMap<>();
    private Node deputy;
    private GameNode gameNode;
    private Player masterPLayer;
    private String currentPlayerName;
    private long msgSeq = 0;


    public Master(SnakesProto.GameConfig gameConfig, GameNode gameNode) {
        this.gameConfig = gameConfig;
        this.gameNode = gameNode;
        game = new Game(gameConfig);
        game.registerObserver(this);
        registerYourSelf();
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                game.makeMove(Map.copyOf(movementMap));
                movementMap.clear();
            }
        };
        timer.schedule(timerTask, 0, gameConfig.getStateDelayMs());
        startSendAnnouncementMessages();
        checkingPlayersStart();
    }

    private void startSendAnnouncementMessages() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    gameNode.sendMessage(
                            new Node(InetAddress.getByName("239.192.0.4"), 9192),
                            SnakesProto.GameMessage.newBuilder()
                                    .setAnnouncement(SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                                            .setCanJoin(true)
                                            .setConfig(gameConfig)
                                            .setPlayers(SnakesProto.GamePlayers.newBuilder().addAllPlayers(playerMap.values()))
                                    )
                                    .setMsgSeq(new Random().nextInt())
                                    .build()
                    );
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    public Master(SnakesProto.GameConfig gameConfig, String playerName, GameNode gameNode) {
        this.gameNode = gameNode;
        this.currentPlayerName = playerName;
        this.gameConfig = gameConfig;
        game = new Game(gameConfig);
        game.registerObserver(this);
        registerYourSelf();
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                game.makeMove(Map.copyOf(movementMap));
                movementMap.clear();
            }
        };
        timer.schedule(timerTask, 0, gameConfig.getStateDelayMs());
        startSendAnnouncementMessages();
        checkingPlayersStart();
    }

    private void registerYourSelf() {
        masterPLayer = new Player(currentPlayerName);
        SnakesProto.GamePlayer gamePlayer = SnakesProto.GamePlayer.newBuilder()
                .setScore(0)
                .setId(masterPLayer.getId())
                .setPort(0)
                .setIpAddress("")
                .setName(masterPLayer.getName())
                .setRole(SnakesProto.NodeRole.MASTER)
                .build();
        game.logIn(masterPLayer);
        playerMap.put(masterPLayer, gamePlayer);
    }

    public Master(SnakesProto.GameConfig gameConfig, SnakesProto.GameState state, GameNode gameNode) {
        this.gameNode = gameNode;
        this.game = new Game(state);
        game.registerObserver(this);
        this.timer = new Timer();
        this.gameConfig = gameConfig;
        for (SnakesProto.GamePlayer gamePlayer : state.getPlayers().getPlayersList()) {
            Player player = new Player(gamePlayer.getName(), gamePlayer.getId());
            String playerIpAddress = gamePlayer.getIpAddress();
            if (playerIpAddress.isEmpty()) {
                continue;
            }
            try {
                Node node = new Node(InetAddress.getByName(playerIpAddress), gamePlayer.getPort());
                lastAliveTimeMap.put(node, System.currentTimeMillis());
                nodePlayerMap.put(node, player);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            playerMap.put(player, gamePlayer);
        }
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                game.makeMove(Map.copyOf(movementMap));
                movementMap.clear();
            }
        };
        timer.schedule(timerTask, 0, gameConfig.getStateDelayMs());
        startSendAnnouncementMessages();
        checkingPlayersStart();
    }

    private void checkingPlayersStart() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                List<Node> deadPlayers = lastAliveTimeMap.entrySet().stream()
                        .filter(nodeEntry ->
                                System.currentTimeMillis() - nodeEntry.getValue() > gameConfig.getNodeTimeoutMs()
                        )
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                long deputyLastTime = 0;
                if (deputy != null) {
                    deputyLastTime = lastAliveTimeMap.getOrDefault(deputy, (long) 0);
                }
                lastAliveTimeMap.keySet().removeAll(deadPlayers);
                nodePlayerMap.forEach((node, player) -> {
                    if (deadPlayers.contains(node)) {
                        playerMap.remove(player);
                        game.deletePlayer(player);
                    }
                });
                nodePlayerMap.keySet().removeAll(deadPlayers);
                if (System.currentTimeMillis() - deputyLastTime > gameConfig.getNodeTimeoutMs()) {
                    if (!nodePlayerMap.isEmpty()) {
                        Node node = nodePlayerMap.keySet().stream().findAny().get();
                        makeDeputy(node);
                    } else {
                        deputy = null;
                    }
                }
            }
        };
        timer.schedule(timerTask, 0, gameConfig.getNodeTimeoutMs());
    }

    @Override
    public void update() {
        List<SnakesProto.GameState.Coord> fruits = convertToListOfCoords(game.getFruits());
        Map<Player, Snake> userList = game.getUserList();
        List<SnakesProto.GameState.Snake> snakes = new ArrayList<>(userList.size());
        userList.forEach((player, snake) -> {
            List<Cell> cells = new ArrayList<>(snake.sizeSnake());
            snake.forEach(cells::add);
            List<SnakesProto.GameState.Coord> snakeCoords = convertToListOfCoords(cells);
            snakes.add(generateProtoSnake(snake, snakeCoords, getProtoPlayer(player).getId()));
        });
        SnakesProto.GameState gameState = SnakesProto.GameState.newBuilder()
                .setConfig(gameConfig)
                .setStateOrder(game.getStateId())
                .addAllFoods(fruits)
                .addAllSnakes(snakes)
                .setPlayers(
                        SnakesProto.GamePlayers.newBuilder()
                                .addAllPlayers(playerMap.values())
                                .build()
                )
                .build();
        sendGameState(gameState);
        gameNode.showState(gameState);
    }

    @Override
    public void update(SnakesProto.GameState state) {

    }

    private void sendGameState(SnakesProto.GameState gameState) {
        playerMap.values().forEach(gamePlayer -> {
            if (gamePlayer.getIpAddress().isEmpty()) {
                return;
            }
            try {
                gameNode.sendMessage(
                        new Node(InetAddress.getByName(gamePlayer.getIpAddress()), gamePlayer.getPort()),
                        generateStateMessage(gameState)
                );
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });


    }

    private SnakesProto.GameMessage generateStateMessage(SnakesProto.GameState gameState) {
        return SnakesProto.GameMessage.newBuilder()
                .setState(SnakesProto.GameMessage.StateMsg.newBuilder()
                        .setState(gameState)
                        .build()
                )
                .setMsgSeq(msgSeq++)
                .build();
    }


    private void putMove(Movement movement, Node sender) {
        for (Map.Entry<Player, SnakesProto.GamePlayer> entry : playerMap.entrySet()) {
            Player player = entry.getKey();
            SnakesProto.GamePlayer gamePlayer = entry.getValue();
            try {
                Node node = new Node(InetAddress.getByName(gamePlayer.getIpAddress()), gamePlayer.getPort());
                if (sender.equals(node)) {
                    movementMap.put(player, movement);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }


    private SnakesProto.GamePlayer getProtoPlayer(Player player) {
        SnakesProto.GamePlayer gamePlayer = playerMap.get(player);
        SnakesProto.GamePlayer updatedPlayer = gamePlayer.toBuilder().setScore(player.getScores()).build();
        playerMap.put(player, updatedPlayer);
        return updatedPlayer;
    }

    void registerPlayer(Node sender, SnakesProto.NodeRole role, String name) {
        if (playerMap.size() == 1) {
            makeDeputy(sender);
        }
        Player player = new Player(name);
        SnakesProto.GamePlayer gamePlayer = SnakesProto.GamePlayer.newBuilder()
                .setName(name)
                .setScore(0)
                .setRole(role)
                .setIpAddress(sender.getAddress().toString())
                .setPort(sender.getPort())
                .setId(player.getId())
                .build();
        game.logIn(player);
        playerMap.put(player, gamePlayer);
    }

    private void makeDeputy(Node sender) {
        deputy = sender;
        gameNode.sendMessage(deputy, SnakesProto.GameMessage.newBuilder()
                .setRoleChange(SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                        .setSenderRole(SnakesProto.NodeRole.MASTER)
                        .setReceiverRole(SnakesProto.NodeRole.DEPUTY)
                        .build())
                .setMsgSeq(msgSeq++)
                .build());
    }


    private SnakesProto.GameState.Snake generateProtoSnake(Snake snake, List<SnakesProto.GameState.Coord> snakeCoords, int ownerID) {
        return SnakesProto.GameState.Snake.newBuilder()
                .addAllPoints(snakeCoords)
                .setHeadDirection(getSnakeDirection(snake))
                .setState(SnakesProto.GameState.Snake.SnakeState.ALIVE)
                .setPlayerId(ownerID)
                .build();
    }

    private SnakesProto.Direction getSnakeDirection(Snake snake) {
        return ProtoUtils.getDirectionByMovement(snake.getPrevDir());
    }

    private List<SnakesProto.GameState.Coord> convertToListOfCoords(List<Cell> cells) {
        return cells.stream().map(this::convertToCoord).collect(Collectors.toList());
    }

    private SnakesProto.GameState.Coord convertToCoord(Cell cell) {
        return SnakesProto.GameState.Coord.newBuilder()
                .setX(cell.getX())
                .setY(cell.getY())
                .build();
    }

    @Override
    public void exit() {
        timer.cancel();
    }

    @Override
    public void processMessage(Node node, SnakesProto.GameMessage gameMessage) {
        lastAliveTimeMap.put(node, System.currentTimeMillis());
        if (gameMessage.hasJoin()) {
            registerPlayer(node, SnakesProto.NodeRole.NORMAL, gameMessage.getJoin().getName());
        } else if (gameMessage.hasRoleChange()) {
            SnakesProto.GameMessage.RoleChangeMsg msg = gameMessage.getRoleChange();
            if (msg.getSenderRole() == SnakesProto.NodeRole.VIEWER && msg.getReceiverRole() == SnakesProto.NodeRole.MASTER) {
                removePlayer(node);
            }
        } else if (gameMessage.hasSteer()) {
            putMove(ProtoUtils.getMovementByProto(gameMessage.getSteer().getDirection()), node);
        }

    }

    private void removePlayer(Node node) {
        Player player = nodePlayerMap.get(node);
        game.deletePlayer(player);
        playerMap.remove(player);
        lastAliveTimeMap.remove(node);
    }

    @Override
    public void makeMove(Movement movement) {
        movementMap.put(masterPLayer, movement);
    }
}
