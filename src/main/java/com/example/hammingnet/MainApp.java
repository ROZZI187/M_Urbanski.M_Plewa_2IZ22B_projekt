package com.example.hammingnet;

import com.example.hammingnet.gui.HammingPane;
import com.example.hammingnet.gui.NetworkPane;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        var root = new BorderPane();

        var banner = new Label("Hamming Net — narzędzia symulacyjne");
        banner.setPadding(new Insets(8, 12, 8, 12));
        root.setTop(banner);

        var tabs = new TabPane();

        var tabCodec = new Tab("Symulator kodera Hamminga", new HammingPane());
        tabCodec.setClosable(false);

        var tabNetwork = new Tab("Sieć (8 węzłów)", new NetworkPane());
        tabNetwork.setClosable(false);

        tabs.getTabs().addAll(tabCodec, tabNetwork);
        root.setCenter(tabs);

        var status = new Label("Status: gotowe");
        status.setPadding(new Insets(4, 12, 4, 12));
        root.setBottom(status);

        var scene = new Scene(root, 1180, 760);
        stage.setTitle("Hamming Net — Symulacja");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
