package com.example.hammingnet;

import com.example.hammingnet.gui.HammingPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        var root = new BorderPane();

        var banner = new Label("Hamming Net — monitor (draft)");
        root.setTop(banner);

        root.setCenter(new HammingPane());

        var status = new Label("Status: ready");
        root.setBottom(status);

        var scene = new Scene(root, 960, 640);
        stage.setTitle("Hamming Net — Local Codec Simulator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
