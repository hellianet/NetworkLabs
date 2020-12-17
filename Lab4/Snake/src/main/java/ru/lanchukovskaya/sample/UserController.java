package ru.lanchukovskaya.sample;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import ru.lanchukovskaya.sample.network.GameNode;
import ru.lanchukovskaya.sample.network.Node;

public class UserController {

    private final GameNode gameNode;
    private Stage stage;
    private EventHandler<KeyEvent> movementHandler;
    private Player player;

    public UserController(Stage stage, GameNode node) {
        this.stage = stage;
        gameNode = node;
    }

    private void initEventHandlers() {
        movementHandler = keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.UP)) {
                gameNode.makeMove(Movement.UP);
            }
            if (keyEvent.getCode().equals(KeyCode.DOWN)) {
                gameNode.makeMove(Movement.DOWN);
            }
            if (keyEvent.getCode().equals(KeyCode.RIGHT)) {
                gameNode.makeMove(Movement.RIGHT);
            }
            if (keyEvent.getCode().equals(KeyCode.LEFT)) {
                gameNode.makeMove(Movement.LEFT);
            }
        };
        stage.addEventHandler(KeyEvent.KEY_RELEASED, movementHandler);
    }

    public void startGame(Node master) {
        initEventHandlers();
        gameNode.joinToGame(master, player.getName());
    }

    public void removeEventHandlers() {
        stage.removeEventHandler(KeyEvent.KEY_PRESSED, movementHandler);
    }

    public void registerPlayer(Player player) {
        this.player = player;
    }

    public void startGame(SnakesProto.GameConfig protoConfig) {
        initEventHandlers();
        gameNode.setConfig(protoConfig);
        gameNode.startGame(player.getName());
    }
}
