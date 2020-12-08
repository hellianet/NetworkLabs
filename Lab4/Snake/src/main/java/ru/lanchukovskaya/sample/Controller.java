package ru.lanchukovskaya.sample;

import javafx.scene.input.MouseEvent;

import java.util.HashMap;

public class Controller implements Observer {

    private MenuView menuView;
    private Game game;
    private HashMap<Player, UserController> players;
    private View view;

    public Controller(MenuView v) {
        menuView = v;
        players = new HashMap<>();
        menuView.getInputImage().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            Player pl = new Player(menuView.getEnterName().getText());
            view = new View(game, pl, menuView);
            view.menu();
            UserController userCr = new UserController(view, game, pl);
            userCr.initEventHandlers();
            players.put(pl, userCr);
        });
    }

    @Override
    public void update() {
        removeUsers();
    }

    @Override
    public void update(SnakesProto.GameState state) {

    }

    private void removeUsers() {
        for (Player player : game.getIsLose()) {
            players.get(player).removeEventHandlers();
            players.remove(player);
        }
    }
}
