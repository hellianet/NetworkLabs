package ru.lanchukovskaya.sample;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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


    private final MenuView menuView;
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
    private TextField widthGameField;
    private TextField heightGameField;
    private TextField timeMove;
    private TextField countFood;
    private TextField percentChange;
    private TextField delayMessage;
    private TextField nodeTimeout;
    private Button play;
    private Button button;

    public View(Game game, Player player, MenuView view) {
        this.player = player;
        this.game = game;
        this.stage = view.getStage();
        this.game.registerObserver(this);
        this.menuView = view;
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
        int sizeButton = 30;
        int height = sizeCell * game.heightField() + sizeLabel + sizeButton;
        stage.setWidth(width);
        stage.setHeight(height);
        GridPane gridpane = new GridPane();
        GridPane gp = new GridPane();
        labelsGrid = new GridPane();
        button = new Button("Exit");
        button.setOnAction(actionEvent -> {
            game.exit();
            menuView.show();
        });
        button.setStyle("-fx-font: 16 Algerian; -fx-base: #b6e7c9;");
        Button viewer = new Button("Become a viewer");
        viewer.setStyle("-fx-font: 16 Algerian; -fx-base: #b6e7c9;");

        for (int i = 0; i < game.widthField(); ++i) {
            for (int k = 0; k < game.heightField(); ++k) {
                Rectangle rectangle = new Rectangle(sizeCell, sizeCell, Color.WHITE);
                gridpane.add(rectangle, i, k);
                rects[i][k] = rectangle;
            }
        }
        gp.add(labelsGrid, 0, 0);
        gp.add(gridpane, 0, 1);
        gp.add(button, 0, 2);
        GridPane.setHalignment(button, HPos.RIGHT);
        gp.add(viewer, 0, 2);
        GridPane.setHalignment(viewer, HPos.LEFT);

        gridpane.setGridLinesVisible(true);
        Scene scene = new Scene(gp, width, height);
        stage.setScene(scene);
        stage.sizeToScene();
    }

    public void menu() {
        GridPane gridpane = new GridPane();
        GridPane gp = new GridPane();
        Label newGame = new Label("Create a new game:");
        newGame.setFont(new Font("Algerian", 20));
        Label existGame = new Label("Join an existing game:");
        existGame.setFont(new Font("Algerian", 20));

        makeColumnsAndRows(gridpane);

        gridpane.add(newGame, 0, 0);
        gridpane.add(existGame, 1, 0);

        makeMarginsForTheLeftSide(gridpane);

        makeTable(gp);

        addDataToTable(gp);

        gridpane.add(gp, 1, 1);
        Scene scene = new Scene(gridpane, 600, 600);
        stage.setScene(scene);
        stage.sizeToScene();

    }

    public Button getPlay() {
        return play;
    }

    public Button getButton() {
        return button;
    }

    public void makeMarginsForTheLeftSide(GridPane gridpane) {
        Label width = new Label("Playing field width");
        widthGameField = new TextField();
        widthGameField.setPrefColumnCount(1);

        Label height = new Label("Playing field height");
        heightGameField = new TextField();
        heightGameField.setPrefColumnCount(1);

        Label time = new Label("Time of one move");
        timeMove = new TextField();
        timeMove.setPrefColumnCount(1);

        Label count = new Label("The amount of food per player");
        countFood = new TextField();
        countFood.setPrefColumnCount(1);

        Label change = new Label("The percentage of the snake turns into food(0-1)");
        percentChange = new TextField();
        percentChange.setPrefColumnCount(1);

        Label delay = new Label("Delay between sending ping messages, in milliseconds");
        delayMessage = new TextField();
        delayMessage.setPrefColumnCount(1);

        Label timeout = new Label("Time after which the neighboring node disappeared");
        nodeTimeout = new TextField();
        nodeTimeout.setPrefColumnCount(1);

        play = new Button("Play");
        play.setOnAction(actionEvent -> {
            try {
                SnakesProto.GameConfig protoConfig = SnakesProto.GameConfig.newBuilder()
                        .setDeadFoodProb(Float.parseFloat(percentChange.getText()))
                        .setHeight(Integer.parseInt(heightGameField.getText()))
                        .setWidth(Integer.parseInt(widthGameField.getText()))
                        .setPingDelayMs(Integer.parseInt(delayMessage.getText()))
                        .setFoodPerPlayer(Integer.parseInt(countFood.getText()))
                        .setNodeTimeoutMs(Integer.parseInt(nodeTimeout.getText()))
                        .setStateDelayMs(Integer.parseInt(timeMove.getText()))
                        .build();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            game.logIn(player);
            game();
            initialization();
        });
        play.setMaxWidth(Double.MAX_VALUE);
        play.setStyle("-fx-font: 18 Algerian; -fx-base: #b6e7c9;");

        FlowPane root = new FlowPane(Orientation.VERTICAL, 10, 10, width, widthGameField, height, heightGameField,
                time, timeMove, count, countFood, change, percentChange, delay, delayMessage, timeout, nodeTimeout, play);
        root.setAlignment(Pos.CENTER);
        gridpane.add(root, 0, 1);


    }

    public void makeColumnsAndRows(GridPane gridpane) {
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        gridpane.getColumnConstraints().add(column1);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        gridpane.getColumnConstraints().add(column2);

        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(5);
        gridpane.getRowConstraints().add(row1);

        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(95);
        gridpane.getRowConstraints().add(row2);

        gridpane.setGridLinesVisible(true);
    }

    public void makeTable(GridPane gp) {
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(45);
        gp.getColumnConstraints().add(col1);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(10);
        gp.getColumnConstraints().add(col2);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(25);
        gp.getColumnConstraints().add(col3);

        RowConstraints r1 = new RowConstraints();
        r1.setPercentHeight(5);
        gp.getRowConstraints().add(r1);

        gp.setAlignment(Pos.CENTER);
        gp.setGridLinesVisible(true);

        Label name = new Label("Name");
        gp.add(name, 0, 0);

        Label countPlayer = new Label("N");
        gp.add(countPlayer, 1, 0);

        Label entry = new Label("Entry");
        gp.add(entry, 2, 0);
    }

    public void addDataToTable(GridPane gp) {

        int size = 3;
        for (int i = 1; i <= size; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(5);
            gp.getRowConstraints().add(row);

            Button arrow = new Button("Entry");
            arrow.setMaxWidth(70);
            arrow.setMaxHeight(15);
            gp.add(arrow, 2, i);

            Label name = new Label("Kris");
            gp.add(name, 0, i);

            Label countPlayer = new Label("2");
            gp.add(countPlayer, 1, i);
        }

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
        rects[cl.getX()][cl.getY()].setFill(Color.WHITE);
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
        fruit = game.getFruits();
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
                snakeTail.remove(pl);
                colorSnake.remove(pl);
            }
        }
        playersInLabel.removeIf(p -> !players.contains(p));
        addFruitCoordinate();
        check();
    }

}
