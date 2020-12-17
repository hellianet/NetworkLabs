package ru.lanchukovskaya.sample;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ru.lanchukovskaya.sample.network.GameNode;

public class Main extends Application {
    private static int port = 5666;

    public static void main(String[] args) {
        port = Integer.parseInt(args[0]);
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {

        stage.getIcons().add(new Image("Icon.jpg"));
        stage.setTitle("Snake");
        int width = 800;
        int height = 400;
        stage.setWidth(width);
        stage.setHeight(height);

        SnakesProto.GameConfig build = SnakesProto.GameConfig.newBuilder().setWidth(15).setHeight(15).build();
        GameNode gameNode = new GameNode(build, SnakesProto.NodeRole.NORMAL, port);
        View view = new View(stage, gameNode);
        view.mainMenu();
        stage.show();
    }
}