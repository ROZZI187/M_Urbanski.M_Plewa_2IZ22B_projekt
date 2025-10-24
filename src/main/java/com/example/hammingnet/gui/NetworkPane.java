package com.example.hammingnet.gui;

import com.example.hammingnet.core.BitVector;
import com.example.hammingnet.core.ErrorType;
import com.example.hammingnet.net.Graph;
import com.example.hammingnet.net.NodeServer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/* Panel sieci — uruchamianie 8 węzłów, podgląd zdarzeń;  bez wysyłania ramek. */
public class NetworkPane extends BorderPane {

    // Górny pasek: Start/Stop
    private final Button btnStart = new Button("Uruchom 8 węzłów");
    private final Button btnStop  = new Button("Zatrzymaj");

    // Lewy panel: przygotowanie wysyłki
    private final ComboBox<Integer> cbEntry = new ComboBox<>();
    private final ComboBox<Integer> cbDst   = new ComboBox<>();
    private final TextField tfValue16 = new TextField();
    private final ComboBox<ErrorType> cbError = new ComboBox<>();
    private final TextField tfProb = new TextField("0.10");

    private final Button btnSendNoErr = new Button("Wyślij (bez usterek)");
    private final Button btnSendErr   = new Button("Wyślij (z usterką)");
    private final Button btnSendProb  = new Button("Wyślij (z prawdopodobieństwem)");
    private final Button btnSendRand  = new Button("Wyślij losową");

    // Prawy panel: dziennik
    private final TextArea log = new TextArea();
    private final Button btnClearLog = new Button("Wyczyść dziennik");

    // Backend sieci
    private final Graph graph = new Graph();
    private final NodeServer[] nodes = new NodeServer[Graph.NODES];
    private boolean running = false;

    public NetworkPane() {
        setPadding(new Insets(12));
        buildUi();

        // Handlery Start/Stop + dziennik
        btnStart.setOnAction(e -> startAll());
        btnStop.setOnAction(e -> stopAll());
        btnClearLog.setOnAction(e -> log.clear());

        // Wypełnij listy wyboru
        for (int i = 0; i < Graph.NODES; i++) { cbEntry.getItems().add(i); cbDst.getItems().add(i); }
        cbEntry.getSelectionModel().select(0);
        cbDst.getSelectionModel().select(4);
        cbError.getItems().addAll(ErrorType.values());
        cbError.getSelectionModel().selectFirst();

        btnStop.setDisable(true);
        setSendControlsDisabled(true);
    }

    private void buildUi() {
        var top = new HBox(8, btnStart, btnStop);
        top.setPadding(new Insets(0, 0, 12, 0));
        setTop(top);

        var grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        int r = 0;
        grid.add(new Label("Węzeł wejściowy:"), 0, r); grid.add(cbEntry, 1, r++);
        grid.add(new Label("Cel:"),              0, r); grid.add(cbDst,   1, r++);
        grid.add(new Label("Wartość 16-bit:"),   0, r); grid.add(tfValue16, 1, r++);
        grid.add(new Label("Rodzaj usterki:"),   0, r); grid.add(cbError,  1, r++);
        grid.add(new Label("p (0..1):"),         0, r); grid.add(tfProb,   1, r++);

        var sendBox = new VBox(8, btnSendNoErr, btnSendErr, btnSendProb, btnSendRand);
        var left = new VBox(12, grid, sendBox);
        left.setPadding(new Insets(0, 12, 0, 0));
        setLeft(left);

        log.setEditable(false);
        log.setPrefColumnCount(60);
        log.setPrefRowCount(26);
        var right = new VBox(8, new Label("Dziennik zdarzeń:"), log, btnClearLog);
        setCenter(right);
    }

    private void startAll() {
        if (running) return;
        for (int i = 0; i < Graph.NODES; i++) {
            final int id = i;
            nodes[i] = new NodeServer(id, graph);
            nodes[i].setListener(new NodeServer.NodeEventListener() {
                @Override public void onReceived(int nodeId, int srcId, int dstId, BitVector payload21) {
                    appendLog(String.format("Węzeł %d: odebrano src=%d dst=%d payload=%s", nodeId, srcId, dstId, payload21));
                }
                @Override public void onForwarded(int nodeId, int nextHopId, int srcId, int dstId, BitVector payload21) {
                    appendLog(String.format("Węzeł %d: przekazano → %d (src=%d dst=%d)", nodeId, nextHopId, srcId, dstId));
                }
                @Override public void onDelivered(int nodeId, int srcId, int dstId, BitVector payload21) {
                    appendLog(String.format("Węzeł %d: DOSTARCZONO src=%d dst=%d payload=%s", nodeId, srcId, dstId, payload21));
                }
                @Override public void onError(int nodeId, String message, Exception ex) {
                    appendLog(String.format("Węzeł %d — BŁĄD: %s (%s)", nodeId, message, ex != null ? ex.getMessage() : ""));
                }
            });
            nodes[i].start();
        }
        running = true;
        btnStart.setDisable(true);
        btnStop.setDisable(false);
        // wysyłka odblokowana dopiero w kolejnym commicie
        setSendControlsDisabled(true);
        appendLog("Uruchomiono wszystkie węzły.");
    }

    private void stopAll() {
        if (!running) return;
        for (int i = 0; i < Graph.NODES; i++) {
            try { if (nodes[i] != null) nodes[i].close(); } catch (Exception ignored) {}
            nodes[i] = null;
        }
        running = false;
        btnStart.setDisable(false);
        btnStop.setDisable(true);
        setSendControlsDisabled(true);
        appendLog("Zatrzymano wszystkie węzły.");
    }

    private void appendLog(String line) {
        Platform.runLater(() -> {
            log.appendText(line + System.lineSeparator());
            log.positionCaret(log.getText().length());
        });
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
