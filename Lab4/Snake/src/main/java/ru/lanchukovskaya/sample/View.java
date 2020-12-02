package ru.lanchukovskaya.sample;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class View implements Observer {


    private ArrayList<Cell> fruit;
    private Player player;
    private Game game;
    private Stage stage;
    private Map<Player, Snake> userList;
    private Rectangle[][] rects;
    private HashMap<Player, Label> labels;
    private HashMap<Player, Color> colorSnake;
    private HashMap<Player, Cell> snakeTail;
    private GridPane labelsGrid;
    private ImageView exitButton;

    public View(Game game, Player player, Stage st) {
        this.player = player;
        this.game = game;
        this.stage = st;
        this.game.registerObserver(this);
        userList = new HashMap<>();
        fruit = new ArrayList<>();
        labels = new HashMap<>();
        rects = new Rectangle[game.widthField()][game.heightField()];
        exitButton = new ImageView(new Image("exitButton.png", 180, 45, false, false));
    }

    private void check() {
        if (game.getIsLose().contains(player)) {
            lose();
        }

        if (game.getIsWin().contains(player)) {
            win();
        }

    }

    public ImageView getExitButton() {
        return exitButton;
    }


    public void game() {
        int sizeCell = 40;
        int width = sizeCell * game.widthField();
        int sizeLabel = 20;
        int height = sizeCell * game.heightField() + sizeLabel;
        stage.setWidth(width);
        stage.setHeight(height);
        GridPane gridpane = new GridPane();
        GridPane gp = new GridPane();
        labelsGrid = new GridPane();
        for (int i = 0; i < game.widthField(); ++i) {
            for (int k = 0; k < game.heightField(); ++k) {
                Rectangle rectangle = new Rectangle(sizeCell, sizeCell, Color.WHITE);
                gridpane.add(rectangle, i, k);
                rects[i][k] = rectangle;
            }
        }
        gp.add(labelsGrid, 0, 0);
        gp.add(gridpane, 0, 1);
        gridpane.setGridLinesVisible(true);
        Scene scene = new Scene(gp, width, height);
        stage.setScene(scene);
        stage.sizeToScene();
    }

    public void win() {
        Label end = new Label("Game over");
        end.setFont(new Font("Algerian", 50));
        Label youIs = new Label("You won!!!");
        youIs.setFont(new Font("Algerian", 45));
        FlowPane root = new FlowPane(Orientation.VERTICAL, 0, 10, end, youIs, exitButton);
        root.setAlignment(Pos.BOTTOM_CENTER);
        root.setBackground(new Background(new BackgroundImage(new Image("winBackground.jpg", 800, 400, false, false), null, null, null, null)));
        Scene scene = new Scene(root, 800, 400);
        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.sizeToScene();
        });

    }

    public void lose() {
        Label end = new Label("Game over");
        end.setFont(new Font("Algerian", 30));
        Label youIs = new Label("You lose...");
        youIs.setFont(new Font("Algerian", 25));
        Label wishPart1 = new Label("Do not give up,");
        Label wishPart2 = new Label("you will succeed!");
        wishPart1.setFont(new Font("Algerian", 25));
        wishPart2.setFont(new Font("Algerian", 25));
        FlowPane root = new FlowPane(Orientation.VERTICAL, 0, 8, end, youIs, wishPart1, wishPart2, exitButton);
        root.setAlignment(Pos.BOTTOM_CENTER);
        root.setBackground(new Background(new BackgroundImage(new Image("loseBackground.jpg", 800, 400, false, false), null, null, null, null)));
        Scene scene = new Scene(root, 800, 400);
        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.sizeToScene();
        });

    }

    public Stage getStage() {
        return stage;
    }

    public void exit() {
        stage.close();
    }

    public void addNameAndSizeSnake(Player pl) {
        Label l = new Label(pl.getName() + ": " + pl.getScores());
        labels.put(pl, l);
        l.setFont(new Font(14));
        labelsGrid.add(l, 0, 0);
        labelsGrid.setGridLinesVisible(true);
    }

    public Color newColor(Player pl) {
        Color cl = Color.RED;
        colorSnake.put(pl, cl);
        return cl;
    }

    public void removeTail(Player pl) {
        rects[snakeTail.get(pl).getX()][snakeTail.get(pl).getY()].setFill(Color.WHITE);
    }

    public void addFieldCoordinate(Cell cl) {
        rects[cl.getX()][cl.getY()].setFill(Color.YELLOW);
    }

    public void addFruitCoordinate() {
        for (int i = 0; i < game.getCountFruit(); ++i) {
            rects[fruit.get(i).getX()][fruit.get(i).getY()].setFill(Color.BLUE);
        }
    }


    public void addSnakeCoordinate(Player pl, Color cl) {
        if (game.getHeadSnake(pl).getX() < game.widthField() && game.getHeadSnake(pl).getX() >= 0 && game.getHeadSnake(pl).getY() < game.heightField() && game.getHeadSnake(pl).getY() >= 0) {
            rects[game.getHeadSnake(pl).getX()][game.getHeadSnake(pl).getY()].setFill(cl);
            rects[game.getTailSnake(pl).getX()][game.getTailSnake(pl).getY()].setFill(cl);
            snakeTail.put(pl, game.getTailSnake(pl));

        }

    }

    public void initialization() {
        colorSnake = new HashMap<>();
        snakeTail = new HashMap<>();
        userList = game.getUserList();
        fruit = game.getFruit();
        Set<Player> players = userList.keySet();
        for (Player pl : players) {
            labelsGrid.getColumnConstraints().add(new ColumnConstraints((double) 600 / userList.size()));
            addNameAndSizeSnake(pl);
            addSnakeCoordinate(pl, newColor(pl));
        }
        addFruitCoordinate();
    }


    @Override
    public void update() {
        display();

    }

    public void display() {
        Set<Player> players = userList.keySet();
        Set<Player> playersInLabel = labels.keySet();
        for (Player pl : players) {
            if (labels.containsKey(pl)) {
                if (game.snakeIsEat(pl)) {

                    Platform.runLater(() -> labels.get(pl).setText(pl.getName() + ": " + pl.getScores()));
                    addFruitCoordinate();
                } else {
                    removeTail(pl);

                }
                addSnakeCoordinate(pl, colorSnake.get(pl));
            } else {
                addNameAndSizeSnake(pl);
                addSnakeCoordinate(pl, newColor(pl));
            }
        }

        for (Player pl : playersInLabel) {
            if (!players.contains(pl)) {
                if (!game.snakeIsFruitNow(pl)) {
                    for (int i = 0; i < game.getSizeEmptySnake(); ++i) {
                        addFieldCoordinate(game.getEmptySnake(i));
                    }
                }
                labels.remove(pl);
                snakeTail.remove(pl);
                colorSnake.remove(pl);
            }
        }
        addFruitCoordinate();
        check();
    }

}
