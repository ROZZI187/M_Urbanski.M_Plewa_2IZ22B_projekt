package com.example.hammingnet.gui;

import com.example.hammingnet.core.BitVector;
import com.example.hammingnet.core.ErrorInjector;
import com.example.hammingnet.core.ErrorType;
import com.example.hammingnet.core.HammingModel;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class HammingPane extends BorderPane {

    private final TextField input16 = new TextField();
    private final Label encoded21 = new Label("-");
    private final Label syndrome  = new Label("-");
    private final Label corrected = new Label("-");
    private final Label output16  = new Label("-");
    private final ComboBox<ErrorType> errorBox = new ComboBox<>();
    private final Button btnEncode = new Button("Encode");
    private final Button btnInject = new Button("Inject error");
    private final Button btnDecode = new Button("Decode");

    // Backend
    private final HammingModel model = new HammingModel();
    private final ErrorInjector injector = new ErrorInjector();

    // Stan roboczy panelu
    private BitVector lastEncoded;   // 21 bitów (po encode)
    private BitVector lastPossiblyCorrupted; // po wstrzyknięciu błędu

    public HammingPane() {
        setPadding(new Insets(12));
        var form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);

        int r = 0;
        form.add(new Label("Input 16-bit (hex or dec):"), 0, r);
        input16.setPromptText("e.g. 0xBEEF or 48879");
        form.add(input16, 1, r++);

        form.add(new Label("Encoded 21-bit:"), 0, r);
        form.add(encoded21, 1, r++);

        form.add(new Label("Syndrome:"), 0, r);
        form.add(syndrome, 1, r++);

        form.add(new Label("Corrected 21-bit:"), 0, r);
        form.add(corrected, 1, r++);

        form.add(new Label("Output 16-bit:"), 0, r);
        form.add(output16, 1, r++);

        var actions = new HBox(8, btnEncode, btnInject, btnDecode);
        actions.setPadding(new Insets(8, 0, 0, 0));

        errorBox.getItems().addAll(ErrorType.values());
        errorBox.getSelectionModel().selectFirst();

        var right = new VBox(8, new Label("Fault type:"), errorBox);
        right.setPadding(new Insets(0, 0, 0, 12));

        setCenter(form);
        setBottom(actions);
        setRight(right);

        // Handlery przycisków (logika panelu)

        // parsuje 16-bitową wartość z pola tekstowego, akceptuje "0x" oraz dziesiętne.
        btnEncode.setOnAction(e -> {
            Integer v = parse16(input16.getText());
            if (v == null) {
                showError("Invalid 16-bit value. Use hex like 0xBEEF or decimal like 48879.");
                return;
            }
            lastEncoded = model.encode16to21(v);
            lastPossiblyCorrupted = new BitVector(21);
            for (int i = 0; i < 21; i++) lastPossiblyCorrupted.set(i, lastEncoded.get(i));
            encoded21.setText(lastEncoded.toString());
            syndrome.setText("-");
            corrected.setText("-");
            output16.setText("-");
        });

        // wstrzykuje wybrany błąd w aktualną ramkę (jeśli jest zakodowana).
        btnInject.setOnAction(e -> {
            if (lastPossiblyCorrupted == null) {
                showError("Encode first to get a 21-bit frame.");
                return;
            }
            ErrorType type = errorBox.getValue();
            injector.inject(lastPossiblyCorrupted, type);
            encoded21.setText(lastPossiblyCorrupted.toString());
        });

        // liczy syndrom, koryguje (1-bit), wyciąga 16 bitów.
        btnDecode.setOnAction(e -> {
            if (lastPossiblyCorrupted == null) {
                showError("Nothing to decode. Encode first.");
                return;
            }
            int s = model.computeSyndrome(lastPossiblyCorrupted);
            syndrome.setText(Integer.toString(s));
            BitVector fixed = model.correctSingleError(lastPossiblyCorrupted);
            corrected.setText(fixed.toString());
            int data = model.extractData(fixed);
            output16.setText(String.format("dec=%d hex=0x%04X", data, data & 0xFFFF));
        });
    }

    // zwraca null gdy format niepoprawny albo wychodzi poza 16 bitów.
    private Integer parse16(String txt) {
        if (txt == null) return null;
        txt = txt.trim();
        try {
            int v;
            if (txt.startsWith("0x") || txt.startsWith("0X")) {
                v = Integer.parseUnsignedInt(txt.substring(2), 16);
            } else {
                v = Integer.parseInt(txt);
            }
            if ((v & ~0xFFFF) != 0) return null;
            return v;
        } catch (Exception ex) {
            return null;
        }
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}
