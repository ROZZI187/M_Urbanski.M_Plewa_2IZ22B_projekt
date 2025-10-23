package com.example.hammingnet;

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
        var scene = new Scene(root, 960, 640);
        stage.setTitle("Hamming Net");
        stage.setScene(scene);
        stage.show();
        var centerInfo = new Label("• 8 nodes (TCP)\n• Supervisor sending 16-bit values (Hamming)\n• Fault injection via GUI");
        root.setCenter(centerInfo);

        var status = new Label("Status: ready");
        root.setBottom(status);

    }
    public static void main(String[] args) {
        launch(args);
    }
}
