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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ru.lanchukovskaya.sample.network.GameNode;
import ru.lanchukovskaya.sample.network.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class View implements Observer {


    private Rectangle[][] rects;
    private HashMap<SnakesProto.GamePlayer, Label> labels;
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
    private Button gameExitButton;
    private GridPane mainWindowPane = new GridPane();
    private int sizeCell;
    private Stage stage;
    private TextField enterName;
    private ImageView input;
    private final UserController userCr;
    private GridPane gpTable;

    public View(Stage stage, GameNode gameNode) {
        this.stage = stage;
        labels = new HashMap<>();
        exitButton = new ImageView(new Image("exitButton.png", 180, 45, false, false));
        exitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> exit());
        gameNode.setView(this);
        userCr = new UserController(stage, gameNode);
    }


    public ImageView getExitButton() {
        return exitButton;
    }


    public void mainMenu() {
        Label label = new Label("Enter your name");
        label.setFont(new Font("Algerian", 18));
        enterName = new TextField();
        enterName.setPrefColumnCount(1);
        input = new ImageView(new Image("playButton.png", 180, 45, false, false));
        input.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            userCr.registerPlayer(new Player(enterName.getText()));
            menu();
        });
        FlowPane root = new FlowPane(Orientation.VERTICAL, 0, 6, label, enterName, input);
        root.setAlignment(Pos.BOTTOM_CENTER);
        root.setBackground(new Background(new BackgroundImage(new Image("menuBackground.jpg", 800, 400, false, false), null, null, null, null)));
        Scene scene = new Scene(root, 800, 400);
        stage.setScene(scene);
        stage.sizeToScene();
    }

    public TextField getEnterName() {
        return enterName;
    }

    public void menu() {
        GridPane gridpane = new GridPane();
        gpTable = new GridPane();
        Label newGame = new Label("Create a new game:");
        newGame.setFont(new Font("Algerian", 20));
        Label existGame = new Label("Join an existing game:");
        existGame.setFont(new Font("Algerian", 20));

        makeColumnsAndRows(gridpane);

        gridpane.add(newGame, 0, 0);
        gridpane.add(existGame, 1, 0);

        makeMarginsForTheLeftSide(gridpane);

        makeTable(gpTable);

        gridpane.add(gpTable, 1, 1);
        Scene scene = new Scene(gridpane, 600, 600);
        stage.setScene(scene);
        stage.sizeToScene();

    }


    public Button getPlay() {
        return play;
    }

    public Button getGameExitButton() {
        return gameExitButton;
    }

    private void makeMarginsForTheLeftSide(GridPane gridpane) {
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
                initGameWindow(protoConfig);
                userCr.startGame(protoConfig);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        });
        play.setMaxWidth(Double.MAX_VALUE);
        play.setStyle("-fx-font: 18 Algerian; -fx-base: #b6e7c9;");
        FlowPane root = new FlowPane(Orientation.VERTICAL, 10, 10, width, widthGameField, height, heightGameField,
                time, timeMove, count, countFood, change, percentChange, delay, delayMessage, timeout, nodeTimeout, play);
        root.setAlignment(Pos.CENTER);
        gridpane.add(root, 0, 1);


    }

    private void makeColumnsAndRows(GridPane gridpane) {
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

    private void makeTable(GridPane gp) {
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

    public void addDataToTable(Map<Node, SnakesProto.GameMessage.AnnouncementMsg> listOfGames) {

        int i = 1;
        for (Map.Entry<Node, SnakesProto.GameMessage.AnnouncementMsg> entry : listOfGames.entrySet()) {
            Node node = entry.getKey();
            SnakesProto.GameMessage.AnnouncementMsg announcementMsg = entry.getValue();
            List<SnakesProto.GamePlayer> playersList = announcementMsg.getPlayers().getPlayersList();
            SnakesProto.GamePlayer master = playersList.stream()
                    .filter(gamePlayer ->
                            gamePlayer.getRole() == SnakesProto.NodeRole.MASTER)
                    .findFirst()
                    .orElseThrow();
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(5);
            gpTable.getRowConstraints().add(row);

            Button arrow = new Button("Entry");
            arrow.setOnAction(actionEvent -> {
                userCr.startGame(node);
            });
            arrow.setMaxWidth(70);
            arrow.setMaxHeight(15);
            gpTable.add(arrow, 2, i);

            Label name = new Label(master.getName());
            gpTable.add(name, 0, i);

            Label countPlayer = new Label(String.valueOf(playersList.size()));
            gpTable.add(countPlayer, 1, i);
            i++;
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

    private void addNameAndSizeSnake(SnakesProto.GamePlayer pl) {
        Label l = new Label(pl.getName() + ": " + pl.getScore());
        labels.put(pl, l);
        l.setFont(new Font(14));
        labelsGrid.add(l, 0, 0);
        labelsGrid.setGridLinesVisible(true);
    }


    private void repaintField(SnakesProto.GameState gameState) {
        List<SnakesProto.GameState.Coord> snakesList = gameState.getSnakesList()
                .stream()
                .flatMap(snake -> snake.getPointsList().stream())
                .collect(Collectors.toList());
        List<SnakesProto.GameState.Coord> foodsList = gameState.getFoodsList();
        for (int y = 0; y < gameState.getConfig().getHeight(); y++) {
            for (int x = 0; x < gameState.getConfig().getWidth(); x++) {
                SnakesProto.GameState.Coord coord = SnakesProto.GameState.Coord.newBuilder().setY(y).setX(x).build();
                Color color = Color.WHITE;
                if (snakesList.contains(coord)) {
                    color = Color.RED;
                } else if (foodsList.contains(coord)) {
                    color = Color.BLUE;
                }
                rects[y][x].setFill(color);
            }
        }
    }

    private void initField(SnakesProto.GameConfig config) {
        sizeCell = 40;
        GridPane fieldPane = new GridPane();
        rects = new Rectangle[config.getHeight()][config.getWidth()];
        for (int y = 0; y < config.getHeight(); ++y) {
            for (int x = 0; x < config.getWidth(); ++x) {
                Rectangle rectangle = new Rectangle(sizeCell, sizeCell, Color.WHITE);
                fieldPane.add(rectangle, x, y);
                rects[y][x] = rectangle;
            }
        }
        fieldPane.setGridLinesVisible(true);
        mainWindowPane.add(fieldPane, 0, 1);
    }

    private void initGameWindow(SnakesProto.GameConfig config) {
        initField(config);
        labelsGrid = new GridPane();
        gameExitButton = new Button("Exit");
        gameExitButton.setOnAction(actionEvent -> {
            mainMenu();
        });
        gameExitButton.setStyle("-fx-font: 16 Algerian; -fx-base: #b6e7c9;");
        Button viewer = new Button("Become a viewer");
        viewer.setStyle("-fx-font: 16 Algerian; -fx-base: #b6e7c9;");

        mainWindowPane.add(labelsGrid, 0, 0);
        mainWindowPane.add(gameExitButton, 0, 2);
        GridPane.setHalignment(gameExitButton, HPos.RIGHT);
        mainWindowPane.add(viewer, 0, 2);
        GridPane.setHalignment(viewer, HPos.LEFT);

        int sizeLabel = 20;
        int sizeButton = 30;
        int width = sizeCell * config.getWidth();
        int height = sizeCell * config.getHeight() + sizeLabel + sizeButton;
        Scene scene = new Scene(mainWindowPane, width, height);
        stage.setScene(scene);
        stage.sizeToScene();
    }

    @Override
    public void update() {

    }

    @Override
    public void update(SnakesProto.GameState gameState) {
        if (rects == null) {
            initGameWindow(gameState.getConfig());
        }
        repaintField(gameState);
    }
}
