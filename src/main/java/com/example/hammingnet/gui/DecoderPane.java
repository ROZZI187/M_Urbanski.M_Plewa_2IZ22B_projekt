package com.example.hammingnet.gui;

import com.example.hammingnet.core.BitVector;
import com.example.hammingnet.core.HammingModel;
import com.example.hammingnet.net.Graph;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/* Panel dekodera – oblicza syndrom, korekcję 1-bit i wyświetla dane 16-bit (dec/hex). */
public class DecoderPane extends BorderPane {

    private static final class Row {
        final Label lastSrc = new Label("-");
        final Label raw21   = new Label("-");
        final Label syndrome = new Label("-");
        final Label corrected = new Label("-");
        final Label data16  = new Label("-");
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
        r++;

        for (int i = 0; i < Graph.NODES; i++, r++) {
            rows[i] = new Row();
            grid.add(new Label("Węzeł " + i), 0, r);
            mono(rows[i].lastSrc); mono(rows[i].raw21);
            mono(rows[i].syndrome); mono(rows[i].corrected); mono(rows[i].data16);
            grid.add(rows[i].lastSrc,   1, r);
            grid.add(rows[i].raw21,     2, r);
            grid.add(rows[i].syndrome,  3, r);
            grid.add(rows[i].corrected, 4, r);
            grid.add(rows[i].data16,    5, r);
        }

        var bottom = new HBox(8, btnClear);
        bottom.setPadding(new Insets(8, 0, 0, 0));

        setCenter(new ScrollPane(grid));
        setBottom(bottom);

        btnClear.setOnAction(e -> clearAll());
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

    private Label labelBold(String s) {
        var l = new Label(s);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private void mono(Labeled l) {
        l.setStyle("-fx-font-family: 'Consolas','Courier New',monospace; -fx-font-size: 12px;");
    }

    /* aktualizuje wiersz dla węzła docelowego – liczymy syndrom, korygujemy,
       wyciągamy 16-bit i pokazujemy w formacie dec/hex. */
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
