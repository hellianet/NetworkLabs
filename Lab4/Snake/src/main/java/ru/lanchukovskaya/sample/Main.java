package ru.lanchukovskaya.sample;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ru.lanchukovskaya.sample.network.GameNode;

public class Main extends Application {

    public static void main(String[] args) {

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
        GameNode gameNode = new GameNode(build, SnakesProto.NodeRole.MASTER, 2000);
        MenuView v = new MenuView(stage);
        v.show();
        Controller con = new Controller(v);
        stage.show();
    }
}