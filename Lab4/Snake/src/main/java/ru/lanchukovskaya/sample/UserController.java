package ru.lanchukovskaya.sample;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class UserController {

    private View view;
    private Game game;
    private Player player;
    private EventHandler<KeyEvent> movementHandler;

    public UserController(View v, Game g, Player pl) {
        view = v;
        game = g;
        player = pl;
    }

    public void initEventHandlers() {
        movementHandler = keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.UP)) {
                game.makeMove(player, Movement.UP);
            }
            if (keyEvent.getCode().equals(KeyCode.DOWN)) {
                game.makeMove(player, Movement.DOWN);
            }
            if (keyEvent.getCode().equals(KeyCode.RIGHT)) {
                game.makeMove(player, Movement.RIGHT);
            }
            if (keyEvent.getCode().equals(KeyCode.LEFT)) {
                game.makeMove(player, Movement.LEFT);
            }
        };
        view.getStage().addEventHandler(KeyEvent.KEY_PRESSED, movementHandler);
        view.getExitButton().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> view.exit());
    }

    public void removeEventHandlers() {
        view.getStage().removeEventHandler(KeyEvent.KEY_PRESSED, movementHandler);
    }
}
