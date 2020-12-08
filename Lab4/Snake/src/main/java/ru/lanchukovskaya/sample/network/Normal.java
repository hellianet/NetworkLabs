package ru.lanchukovskaya.sample.network;

import ru.lanchukovskaya.sample.Movement;
import ru.lanchukovskaya.sample.ProtoUtils;
import ru.lanchukovskaya.sample.SnakesProto;

import java.util.Timer;
import java.util.TimerTask;

public class Normal implements NodeWithRole {

    private final Timer timer;

    public Normal(SnakesProto.GameConfig gameConfig) {
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (gameNode.getLastMasterAliveTime() - System.currentTimeMillis() > gameConfig.getNodeTimeoutMs()) {
                    gameNode.becomeMaster();
                }
            }
        };
        timer.schedule(timerTask, 0, gameConfig.getNodeTimeoutMs());
    }

    private GameNode gameNode;
    private SnakesProto.GameState lastState;


    @Override
    public void processMessage(Node node, SnakesProto.GameMessage message) {
        if (message.hasRoleChange()) {
            changeRoleMessageProcess(node, message.getRoleChange());
        } else if (message.hasState()) {
            SnakesProto.GameState state = message.getState().getState();
            if (lastState == null) {
                lastState = state;
            } else if (lastState.getStateOrder() < state.getStateOrder()) {
                gameNode.showState(state);
            }
        } else if (message.hasError()) {
            System.out.println("Error: " + message.getError().getErrorMessage());
        } else if (message.hasPing()) {
            if (node == gameNode.getMaster()) {
                gameNode.markThatMasterAlive();
            }
        }
    }


    public void changeRoleMessageProcess(Node sender, SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg) {
        if (roleChangeMsg.getSenderRole() == SnakesProto.NodeRole.MASTER && roleChangeMsg.getReceiverRole() == SnakesProto.NodeRole.DEPUTY) {
            gameNode.changeRole(SnakesProto.NodeRole.DEPUTY);
        } else if (roleChangeMsg.getSenderRole() == SnakesProto.NodeRole.DEPUTY && roleChangeMsg.getReceiverRole() == SnakesProto.NodeRole.NORMAL) {
            gameNode.setMaster(sender);
        } else if (roleChangeMsg.getSenderRole() == SnakesProto.NodeRole.MASTER && roleChangeMsg.getReceiverRole() == SnakesProto.NodeRole.VIEWER) {
            System.out.println("LOSE");
        } else if (roleChangeMsg.getSenderRole() == SnakesProto.NodeRole.MASTER && roleChangeMsg.getReceiverRole() == SnakesProto.NodeRole.MASTER) {
            gameNode.becomeMaster();
        }
    }

    @Override
    public void makeMove(Movement movement) {
        gameNode.sendMessage(
                gameNode.getMaster(),
                generateSteerMessage(movement)
        );
    }

    private SnakesProto.GameMessage generateSteerMessage(Movement movement) {
        return SnakesProto.GameMessage.newBuilder()
                .setSteer(SnakesProto.GameMessage.SteerMsg.newBuilder()
                        .setDirection(ProtoUtils.getDirectionByMovement(movement))
                        .build()
                )
                .build();
    }

    @Override
    public void exit() {
        timer.cancel();
    }

    @Override
    public void setGameNode(GameNode gameNode) {
        this.gameNode = gameNode;
    }
}
