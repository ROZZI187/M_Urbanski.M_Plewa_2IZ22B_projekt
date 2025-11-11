package com.example.hammingnet.gui;

import com.example.hammingnet.core.BitVector;
import com.example.hammingnet.core.HammingModel;
import com.example.hammingnet.net.Graph;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/* Panel dekodera - stałe szerokości, kopiowanie wiersza do schowka */
public class DecoderPane extends BorderPane {

    private static final class Row {
        final Label lastSrc = mkLabel();
        final Label raw21   = mkLabel();
        final Label syndrome = mkLabel();
        final Label corrected = mkLabel();
        final Label data16  = mkLabel();
        final Button copyBtn = new Button("Kopiuj");
    }

    private final Row[] rows = new Row[Graph.NODES];
    private final Button btnClear = new Button("Wyczyść tabelę");
    private final HammingModel model = new HammingModel();

    public DecoderPane() {
        setPadding(new Insets(12));
        var grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);

        int r = 0;
        grid.add(labelBold("Węzeł (docelowy)"), 0, r);
        grid.add(labelBold("Źródło"),           1, r);
        grid.add(labelBold("Ramka 21-bit (surowa)"), 2, r);
        grid.add(labelBold("Syndrom"),          3, r);
        grid.add(labelBold("Skorygowana 21-bit"),4, r);
        grid.add(labelBold("Dane 16-bit"),      5, r);
        grid.add(labelBold("Akcje"),            6, r);
        r++;

        for (int i = 0; i < Graph.NODES; i++, r++) {
            rows[i] = new Row();
            grid.add(new Label("Węzeł " + i), 0, r);

            setMono(rows[i].lastSrc, 70);
            setMono(rows[i].raw21,  210);
            setMono(rows[i].syndrome, 70);
            setMono(rows[i].corrected, 210);
            setMono(rows[i].data16, 160);

            grid.add(rows[i].lastSrc,   1, r);
            grid.add(rows[i].raw21,     2, r);
            grid.add(rows[i].syndrome,  3, r);
            grid.add(rows[i].corrected, 4, r);
            grid.add(rows[i].data16,    5, r);
            grid.add(rows[i].copyBtn,   6, r);

            final int rowIdx = i;
            rows[i].copyBtn.setOnAction(e -> copyRow(rowIdx));
        }

        var bottom = new HBox(8, btnClear);
        bottom.setPadding(new Insets(8, 0, 0, 0));

        setCenter(new ScrollPane(grid));
        setBottom(bottom);

        btnClear.setOnAction(e -> clearAll());
    }

    private static Label mkLabel() {
        var l = new Label("-");
        l.setStyle("-fx-font-family: 'Consolas','Courier New',monospace; -fx-font-size: 12px;");
        return l;
    }

    private void setMono(Label l, double prefWidth) {
        l.setMinWidth(prefWidth);
        l.setPrefWidth(prefWidth);
        l.setMaxWidth(Double.MAX_VALUE);
    }

    private Label labelBold(String s) {
        var l = new Label(s);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private void copyRow(int idx) {
        if (idx < 0 || idx >= Graph.NODES) return;
        var r = rows[idx];
        String text = String.format(
                "Wezel=%d | Zrodlo=%s | Raw=%s | Syndrom=%s | Skorygowana=%s | Dane=%s",
                idx, r.lastSrc.getText(), r.raw21.getText(), r.syndrome.getText(),
                r.corrected.getText(), r.data16.getText()
        );
        var cb = javafx.scene.input.Clipboard.getSystemClipboard();
        var content = new javafx.scene.input.ClipboardContent();
        content.putString(text);
        cb.setContent(content);
    }

    private void clearAll() {
        for (int i = 0; i < Graph.NODES; i++) {
            rows[i].lastSrc.setText("-");
            rows[i].raw21.setText("-");
            rows[i].syndrome.setText("-");
            rows[i].corrected.setText("-");
            rows[i].data16.setText("-");
        }
    }

    public void acceptDelivery(int dstNodeId, int srcId, BitVector payload21) {
        if (dstNodeId < 0 || dstNodeId >= Graph.NODES) return;
        Platform.runLater(() -> {
            var row = rows[dstNodeId];
            row.lastSrc.setText(Integer.toString(srcId));
            row.raw21.setText(payload21.toString());

            int s = model.computeSyndrome(payload21);
            row.syndrome.setText(Integer.toString(s));

            BitVector fixed = model.correctSingleError(payload21);
            row.corrected.setText(fixed.toString());

            int data = model.extractData(fixed);
            row.data16.setText(String.format("dec=%d hex=0x%04X", data, data & 0xFFFF));
        });
    }
}
