package ru.lanchukovskaya.sample;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MenuView {
    private TextField enterName;
    private ImageView input;
    private Stage stage;

    public MenuView(Stage stage) {
        this.stage = stage;

    }

    public Stage getStage() {
        return stage;
    }

    public void show() {
        Label label = new Label("Enter your name");
        label.setFont(new Font("Algerian", 18));
        input = new ImageView(new Image("playButton.png", 180, 45, false, false));
        enterName = new TextField();
        enterName.setPrefColumnCount(1);
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

    public ImageView getInputImage() {
        return input;
    }
}
