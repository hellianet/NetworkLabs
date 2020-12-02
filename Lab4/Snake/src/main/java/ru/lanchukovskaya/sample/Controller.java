package ru.lanchukovskaya.sample;

import javafx.scene.input.MouseEvent;

import java.util.HashMap;

public class Controller implements Observer {

    private MenuView view;
    private Game game;
    private HashMap<Player, UserController> players;

    public Controller(MenuView v, Game g) {
        view = v;
        game = g;
        players = new HashMap<>();
        game.registerObserver(this);
    }

    public void initInputImage() {
        view.getInputImage().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {

            String name = view.getEnterName().getText();
            Player pl = game.logIn(name);
            View userView = new View(game, pl, view.getStage());
            UserController userCr = new UserController(userView, game, pl);
            userCr.initEventHandlers();
            players.put(pl, userCr);
            userView.game();
            userView.initialization();
            game.run();
        });

    }

    @Override
    public void update() {
        removeUsers();
    }

    private void removeUsers() {
        for (Player player : game.getIsLose()) {
            players.get(player).removeEventHandlers();
            players.remove(player);
        }
    }
}
