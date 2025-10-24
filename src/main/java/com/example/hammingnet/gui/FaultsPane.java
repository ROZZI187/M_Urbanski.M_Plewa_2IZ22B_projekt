package com.example.hammingnet.gui;

import com.example.hammingnet.net.FaultConfig;
import com.example.hammingnet.net.Graph;
import com.example.hammingnet.net.NodeServer;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/* Panel do konfiguracji usterek dla 8 węzłów */
public class FaultsPane extends BorderPane {

    private final NodeServer[] nodes; // referencja od NetworkPane
    private final FaultConfig[] configs = new FaultConfig[Graph.NODES];

    private static final class Row {
        CheckBox flipOn = new CheckBox("BIT_FLIP");
        TextField flipP = new TextField("0.00");
        CheckBox burstOn = new CheckBox("BURST_2");
        TextField burstP = new TextField("0.00");
        CheckBox dropOn = new CheckBox("DROP_PACKET");
        TextField dropP = new TextField("0.00");
    }

    private final Row[] rows = new Row[Graph.NODES];
    private final Button btnApply = new Button("Zastosuj do węzłów");
    private final Button btnDisableAll = new Button("Wyłącz wszystkie usterki");

    public FaultsPane(NodeServer[] nodes) {
        this.nodes = nodes;
        setPadding(new Insets(12));
        for (int i = 0; i < Graph.NODES; i++) {
            configs[i] = FaultConfig.disabled();
            rows[i] = new Row();
        }
        buildUi();
        wireActions();
    }

    private void buildUi() {
        var grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);

        int r = 0;
        grid.add(labelBold("Węzeł"), 0, r);
        grid.add(labelBold("BIT_FLIP p"), 1, r);
        grid.add(labelBold("BURST_2 p"),  3, r);
        grid.add(labelBold("DROP p"),     5, r);
        r++;

        for (int i = 0; i < Graph.NODES; i++, r++) {
            grid.add(new Label("Węzeł " + i + ":"), 0, r);

            var row = rows[i];
            grid.add(row.flipOn, 1, r);
            grid.add(row.flipP,  2, r);
            grid.add(row.burstOn,3, r);
            grid.add(row.burstP, 4, r);
            grid.add(row.dropOn, 5, r);
            grid.add(row.dropP,  6, r);

            row.flipP.setPrefColumnCount(4);
            row.burstP.setPrefColumnCount(4);
            row.dropP.setPrefColumnCount(4);
        }

        var bottom = new HBox(8, btnApply, btnDisableAll);
        bottom.setPadding(new Insets(8, 0, 0, 0));

        setCenter(grid);
        setBottom(bottom);
    }

    private Label labelBold(String s) {
        var l = new Label(s);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private void wireActions() {
        btnApply.setOnAction(e -> applyToNodes());
        btnDisableAll.setOnAction(e -> {
            for (int i = 0; i < Graph.NODES; i++) {
                rows[i].flipOn.setSelected(false);
                rows[i].burstOn.setSelected(false);
                rows[i].dropOn.setSelected(false);
                rows[i].flipP.setText("0.00");
                rows[i].burstP.setText("0.00");
                rows[i].dropP.setText("0.00");
            }
            applyToNodes();
        });
    }

    private void applyToNodes() {
        for (int i = 0; i < Graph.NODES; i++) {
            FaultConfig cfg = new FaultConfig();
            var row = rows[i];

            cfg.flip().enabled = row.flipOn.isSelected();
            cfg.flip().p = parseP(row.flipP.getText());

            cfg.burst2().enabled = row.burstOn.isSelected();
            cfg.burst2().p = parseP(row.burstP.getText());

            cfg.drop().enabled = row.dropOn.isSelected();
            cfg.drop().p = parseP(row.dropP.getText());

            configs[i] = cfg;

            if (nodes[i] != null) {
                nodes[i].setFaultConfig(cfg);
            }
        }
    }

    private double parseP(String t) {
        try {
            double p = Double.parseDouble(t.trim());
            if (p < 0) return 0.0;
            if (p > 1) return 1.0;
            return p;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
