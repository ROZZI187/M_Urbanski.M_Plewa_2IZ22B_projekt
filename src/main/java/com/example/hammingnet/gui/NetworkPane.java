package com.example.hammingnet.gui;

import com.example.hammingnet.core.BitVector;
import com.example.hammingnet.core.ErrorType;
import com.example.hammingnet.net.Graph;
import com.example.hammingnet.net.NodeServer;
import com.example.hammingnet.net.Supervisor;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/* Panel sieci - zintegrowany z dekoderem: trzecia zakładka, przekazujemy onDelivered do DecoderPane. */
public class NetworkPane extends BorderPane {

    private final Button btnStart = new Button("Uruchom 8 węzłów");
    private final Button btnStop  = new Button("Zatrzymaj");

    private final ComboBox<Integer> cbEntry = new ComboBox<>();
    private final ComboBox<Integer> cbDst   = new ComboBox<>();
    private final TextField tfValue16 = new TextField();
    private final ComboBox<ErrorType> cbError = new ComboBox<>();
    private final TextField tfProb = new TextField("0.10");

    private final Button btnSendNoErr = new Button("Wyślij (bez usterek)");
    private final Button btnSendErr   = new Button("Wyślij (z usterką)");
    private final Button btnSendProb  = new Button("Wyślij (z prawdopodobieństwem)");
    private final Button btnSendRand  = new Button("Wyślij losową");

    private final TextArea log = new TextArea();
    private final Button btnClearLog = new Button("Wyczyść dziennik");

    private final Graph graph = new Graph();
    private final NodeServer[] nodes = new NodeServer[Graph.NODES];
    private boolean running = false;

    private final TabPane rightTabs = new TabPane();
    private final DecoderPane decoderPane = new DecoderPane();

    public NetworkPane() {
        setPadding(new Insets(12));

        var top = new HBox(8, btnStart, btnStop);
        top.setPadding(new Insets(0, 0, 12, 0));
        setTop(top);

        var left = buildSendControls();
        setLeft(left);

        log.setEditable(false);
        log.setPrefColumnCount(60);
        log.setPrefRowCount(22);
        log.setStyle("-fx-font-family: 'Consolas','Courier New',monospace; -fx-font-size: 12px;");

        var logBox = new VBox(8, new Label("Dziennik zdarzeń:"), log, btnClearLog);
        var tabLog = new Tab("Dziennik", logBox); tabLog.setClosable(false);

        var faultsPane = new FaultsPane(nodes);
        var tabFaults = new Tab("Usterki w węzłach", faultsPane); tabFaults.setClosable(false);

        var tabDecoder = new Tab("Dekoder (na węzłach docelowych)", decoderPane);
        tabDecoder.setClosable(false);

        rightTabs.getTabs().addAll(tabLog, tabFaults, tabDecoder);
        setCenter(rightTabs);

        btnStart.setOnAction(e -> startAll());
        btnStop.setOnAction(e -> stopAll());
        btnClearLog.setOnAction(e -> log.clear());

        for (int i = 0; i < Graph.NODES; i++) { cbEntry.getItems().add(i); cbDst.getItems().add(i); }
        cbEntry.getSelectionModel().select(0);
        cbDst.getSelectionModel().select(4);
        cbError.getItems().addAll(ErrorType.values());
        cbError.getSelectionModel().selectFirst();

        btnSendNoErr.setOnAction(e -> doSendNoErr());
        btnSendErr.setOnAction(e -> doSendErr());
        btnSendProb.setOnAction(e -> doSendProb());
        btnSendRand.setOnAction(e -> doSendRand());

        btnStop.setDisable(true);
        setSendControlsDisabled(true);
    }

    private VBox buildSendControls() {
        var grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        int r = 0;
        grid.add(new Label("Węzeł wejściowy:"), 0, r); grid.add(cbEntry, 1, r++);
        grid.add(new Label("Cel:"),              0, r); grid.add(cbDst,   1, r++);
        grid.add(new Label("Wartość 16-bit:"),   0, r); grid.add(tfValue16, 1, r++);
        grid.add(new Label("Rodzaj usterki:"),   0, r); grid.add(cbError,  1, r++);
        grid.add(new Label("p (0..1):"),         0, r); grid.add(tfProb,   1, r++);

        tfValue16.setPromptText("np. 0xBEEF lub 48879");
        tfProb.setPromptText("0..1");

        var sendBox = new VBox(8, btnSendNoErr, btnSendErr, btnSendProb, btnSendRand);
        var left = new VBox(12, grid, sendBox);
        left.setPadding(new Insets(0, 12, 0, 0));
        return left;
    }

    private void startAll() {
        if (running) { appendLog("Węzły już działają."); return; }
        appendLog("Porty węzłów: " + portMap());
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
                    decoderPane.acceptDelivery(nodeId, srcId, payload21);
                }
                @Override public void onError(int nodeId, String message, Exception ex) {
                    appendLog(String.format("Węzeł %d — BŁĄD: %s%s", nodeId, message, ex != null ? " (" + ex.getMessage() + ")" : ""));
                }
            });
            nodes[i].start();
        }
        running = true;
        btnStart.setDisable(true);
        btnStop.setDisable(false);
        setSendControlsDisabled(false);
        appendLog("Uruchomiono wszystkie węzły.");
    }

    private String portMap() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Graph.NODES; i++) {
            if (i > 0) sb.append(", ");
            sb.append(i).append("→").append(graph.port(i));
        }
        return sb.toString();
    }

    private void stopAll() {
        if (!running) { appendLog("Węzły już zatrzymane."); return; }
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

    private void appendLog(String line) {
        Platform.runLater(() -> {
            log.appendText(line + System.lineSeparator());
            log.positionCaret(log.getText().length());
        });
    }

    private Integer parse16(String txt) {
        if (txt == null) return null;
        txt = txt.trim();
        try {
            int v;
            if (txt.startsWith("0x") || txt.startsWith("0X")) v = Integer.parseUnsignedInt(txt.substring(2), 16);
            else v = Integer.parseInt(txt);
            return ((v & ~0xFFFF) == 0) ? v : null;
        } catch (Exception ex) { return null; }
    }

    private double parseProb(String txt) {
        try {
            double p = Double.parseDouble(txt.trim());
            if (p < 0 || p > 1) throw new IllegalArgumentException();
            return p;
        } catch (Exception ex) { return Double.NaN; }
    }

    private NodeServer entryNode() { return nodes[cbEntry.getValue()]; }
    private int dstId() { return cbDst.getValue(); }

    private void doSendNoErr() {
        var v = parse16(tfValue16.getText());
        if (v == null) { appendLog("Błędna wartość 16-bit (np. 0xBEEF lub 48879)."); return; }
        var entry = entryNode();
        if (entry == null) { appendLog("Węzeł wejściowy nie działa."); return; }
        var sup = new Supervisor(entry);
        boolean sent = sup.sendValueNoErrors(dstId(), v);
        if (sent) {
            appendLog(String.format("Wysłano (bez usterek): entry=%d dst=%d val=0x%04X", entry.id(), dstId(), v));
        }
    }

    private void doSendErr() {
        var v = parse16(tfValue16.getText());
        if (v == null) { appendLog("Błędna wartość 16-bit (np. 0xBEEF lub 48879)."); return; }
        var entry = entryNode();
        if (entry == null) { appendLog("Węzeł wejściowy nie działa."); return; }
        var sup = new Supervisor(entry);
        boolean sent = sup.sendValueWithError(dstId(), v, cbError.getValue());
        if (!sent && cbError.getValue() == ErrorType.DROP_PACKET) {
            appendLog(String.format("Pakiet porzucony u źródła (DROP_PACKET): entry=%d dst=%d val=0x%04X",
                    entry.id(), dstId(), v));
            return;
        }
        appendLog(String.format("Wysłano (z usterką %s): entry=%d dst=%d val=0x%04X",
                cbError.getValue(), entry.id(), dstId(), v));
    }

    private void doSendProb() {
        var v = parse16(tfValue16.getText());
        if (v == null) { appendLog("Błędna wartość 16-bit (np. 0xBEEF lub 48879)."); return; }
        double p = parseProb(tfProb.getText());
        if (Double.isNaN(p)) { appendLog("Błędne p — podaj liczbę z zakresu 0..1."); return; }
        var entry = entryNode();
        if (entry == null) { appendLog("Węzeł wejściowy nie działa."); return; }
        var sup = new Supervisor(entry);
        boolean sent = sup.sendValueMaybeError(dstId(), v, cbError.getValue(), p);
        if (!sent && cbError.getValue() == ErrorType.DROP_PACKET) {
            appendLog(String.format("Pakiet porzucony u źródła (DROP_PACKET, p=%.3f): entry=%d dst=%d val=0x%04X",
                    p, entry.id(), dstId(), v));
            return;
        }
        appendLog(String.format("Wysłano (p=%.3f, usterka %s): entry=%d dst=%d val=0x%04X",
                p, cbError.getValue(), entry.id(), dstId(), v));
    }

    private void doSendRand() {
        var entry = entryNode();
        if (entry == null) { appendLog("Węzeł wejściowy nie działa."); return; }
        var sup = new Supervisor(entry);
        boolean sent = sup.sendRandom(dstId());
        if (sent) {
            appendLog(String.format("Wysłano losową wartość: entry=%d dst=%d", entry.id(), dstId()));
        }
    }
}
