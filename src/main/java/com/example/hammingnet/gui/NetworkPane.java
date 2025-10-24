package com.example.hammingnet.gui;

import com.example.hammingnet.core.ErrorType;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class NetworkPane extends BorderPane {

    // Górny pasek: Start/Stop
    private final Button btnStart = new Button("Start 8 nodes");
    private final Button btnStop  = new Button("Stop");

    // Lewy panel: ustawienia wysyłki
    private final ComboBox<Integer> cbEntry = new ComboBox<>();
    private final ComboBox<Integer> cbDst   = new ComboBox<>();
    private final TextField tfValue16 = new TextField();
    private final ComboBox<ErrorType> cbError = new ComboBox<>();
    private final TextField tfProb = new TextField("0.10");

    private final Button btnSendNoErr = new Button("Send (no errors)");
    private final Button btnSendErr   = new Button("Send (with error)");
    private final Button btnSendProb  = new Button("Send (p error)");
    private final Button btnSendRand  = new Button("Send random");

    // Prawy panel: log
    private final TextArea log = new TextArea();
    private final Button btnClearLog = new Button("Clear log");

    public NetworkPane() {
        setPadding(new Insets(12));


        var top = new HBox(8, btnStart, btnStop);
        top.setPadding(new Insets(0, 0, 12, 0));
        setTop(top);


        var grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);

        int r = 0;
        grid.add(new Label("Entry node:"), 0, r); grid.add(cbEntry, 1, r++);
        grid.add(new Label("Destination:"), 0, r); grid.add(cbDst,   1, r++);
        grid.add(new Label("Value 16-bit:"), 0, r); grid.add(tfValue16, 1, r++);
        grid.add(new Label("Error type:"), 0, r); grid.add(cbError,  1, r++);
        grid.add(new Label("p (0..1):"),   0, r); grid.add(tfProb,   1, r++);

        var sendBox = new VBox(8, btnSendNoErr, btnSendErr, btnSendProb, btnSendRand);
        var left = new VBox(12, grid, sendBox);
        left.setPadding(new Insets(0, 12, 0, 0));
        setLeft(left);

        // Log
        log.setEditable(false);
        log.setPrefColumnCount(60);
        log.setPrefRowCount(24);
        var right = new VBox(8, new Label("Event log:"), log, btnClearLog);
        setCenter(right);

        //  listy wyboru
        for (int i = 0; i < 8; i++) { cbEntry.getItems().add(i); cbDst.getItems().add(i); }
        cbEntry.getSelectionModel().select(0);
        cbDst.getSelectionModel().select(4);
        cbError.getItems().addAll(ErrorType.values());
        cbError.getSelectionModel().selectFirst();

        // Stan początkowy
        btnStop.setDisable(true);
        setSendControlsDisabled(true);
    }

    private void setSendControlsDisabled(boolean b) {
        cbEntry.setDisable(b);
        cbDst.setDisable(b);
        tfValue16.setDisable(b);
        cbError.setDisable(b);
        tfProb.setDisable(b);
        btnSendNoErr.setDisable(b);
        btnSendErr.setDisable(b);
        btnSendProb.setDisable(b);
        btnSendRand.setDisable(b);
    }
}
