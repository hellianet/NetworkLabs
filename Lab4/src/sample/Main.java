package sample;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

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
        Game game = new Game(25);
        MenuView v = new MenuView(stage);
        v.show();
        Controller con = new Controller(v, game);
        con.initInputImage();
        stage.show();
    }
}