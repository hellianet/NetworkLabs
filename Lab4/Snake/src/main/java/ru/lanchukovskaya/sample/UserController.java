package ru.lanchukovskaya.sample;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import ru.lanchukovskaya.sample.network.GameNode;

public class UserController {

    private final GameNode gameNode;
    private View view;
    private EventHandler<KeyEvent> movementHandler;

    public UserController(View v, GameNode node) {
        view = v;
        gameNode = node;
    }

    public void initEventHandlers() {
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
        view.getStage().addEventHandler(KeyEvent.KEY_RELEASED, movementHandler);
        view.getExitButton().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> view.exit());
    }

    public void removeEventHandlers() {
        view.getStage().removeEventHandler(KeyEvent.KEY_PRESSED, movementHandler);
    }
}
