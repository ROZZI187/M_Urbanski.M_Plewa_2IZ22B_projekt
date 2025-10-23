package com.example.hammingnet.gui;

import com.example.hammingnet.core.ErrorType;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class HammingPane extends BorderPane {

    // Pola UI
    private final TextField input16 = new TextField();    // hex lub dec
    private final Label encoded21 = new Label("-");
    private final Label syndrome  = new Label("-");
    private final Label corrected = new Label("-");
    private final Label output16  = new Label("-");
    private final ComboBox<ErrorType> errorBox = new ComboBox<>();
    private final Button btnEncode = new Button("Encode");
    private final Button btnInject = new Button("Inject error");
    private final Button btnDecode = new Button("Decode");

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
    }
}
