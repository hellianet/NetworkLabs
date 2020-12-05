package ru.lanchukovskaya.sample;

import javafx.scene.input.MouseEvent;

import java.util.HashMap;

public class Controller implements Observer {

    private MenuView view;
    private Game game;
    private HashMap<Player, UserController> players;
    private View userView;

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
            userView = new View(game, pl, view);
            UserController userCr = new UserController(userView, game, pl);
            userCr.initEventHandlers();
            players.put(pl, userCr);
            userView.menu();
            newGame(userView);

        });

    }

    public void newGame(View userView) {
        userView.getPlay().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            userView.game();
            userView.initialization();
            game.run();
            exitButton();
        });
    }

    public void exitButton() {
        userView.getButton().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            game.exit();
            view.show();
            initInputImage();
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
