package com.tarotexe;

import com.tarotexe.service.AiSettings;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import java.util.function.Consumer;

final class AiSettingsDialog {
    static void show(Stage owner, AiSettings current, Consumer<AiSettings> apply) {
        var dialog = new Dialog<AiSettings>(); dialog.initOwner(owner); dialog.setTitle("Configurar IA local");
        var model = new TextField(current.model()); model.setId("aiModelSetting");
        var timeout = new Spinner<Integer>(5, 300, current.timeoutSeconds(), 15);
        var tokens = new Spinner<Integer>(128, 1024, current.responseTokens(), 64);
        var context = new Spinner<Integer>(2048, 8192, current.contextTokens(), 1024);
        var temperature = new Spinner<Double>(0, 1, current.temperature(), 0.1);
        var grid = new GridPane(); grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(12));
        grid.addRow(0, new Label("Modelo baixado"), model);
        grid.addRow(1, new Label("Tempo limite (segundos)"), timeout);
        grid.addRow(2, new Label("Limite da resposta (tokens)"), tokens);
        grid.addRow(3, new Label("Contexto (tokens)"), context);
        grid.addRow(4, new Label("Criatividade (0 a 1)"), temperature);
        var note = new Label("Padrão para esta máquina: qwen2.5:3b.\nOllama local • 127.0.0.1:11434\nA IA só é acionada quando você pede o aprofundamento.");
        note.setWrapText(true); grid.add(note, 0, 5, 2, 1);
        var error = new Label(); error.setWrapText(true); grid.add(error, 0, 6, 2, 1);
        var save = new ButtonType("Aplicar e salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().add(AiSettingsDialog.class.getResource("/retro.css").toExternalForm());
        dialog.getDialogPane().lookupButton(save).addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            try { new AiSettings(model.getText().strip(), timeout.getValue(), tokens.getValue(), context.getValue(), temperature.getValue()); }
            catch (IllegalArgumentException ex) { error.setText(ex.getMessage()); e.consume(); }
        });
        dialog.setResultConverter(button -> button == save
                ? new AiSettings(model.getText().strip(), timeout.getValue(), tokens.getValue(), context.getValue(), temperature.getValue()) : null);
        dialog.showAndWait().ifPresent(apply);
    }
}
