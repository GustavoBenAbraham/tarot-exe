package com.tarotexe;

import com.tarotexe.service.HistoryStore;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

final class HistoryWindow {
    static void show(Stage owner, HistoryStore store, Consumer<HistoryStore.Entry> reopen) {
        var stage = new Stage(); stage.initOwner(owner); stage.setTitle("Histórico de tiragens");
        AppInfo.decorate(stage);
        var list = new ListView<HistoryStore.Entry>(); list.setId("historyList");
        list.setPlaceholder(new Label("Nenhuma tiragem salva."));
        list.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(HistoryStore.Entry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) { setText(null); return; }
                var reading = entry.reading();
                setText(reading.createdAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        + " • " + reading.theme() + " • " + reading.type() + "\n"
                        + (reading.question().isBlank() ? "Sem pergunta" : reading.question().replace('\n', ' ')));
            }
        });
        var preview = new TextArea(); preview.setId("historyPreview"); preview.setEditable(false); preview.setWrapText(true);
        preview.getStyleClass().add("reading-paper"); preview.setPromptText("Selecione uma tiragem para consultar suas anotações.");
        list.getSelectionModel().selectedItemProperty().addListener((o, old, entry) -> preview.setText(entry == null ? "" : entry.fullText()));
        var message = new Label(); message.setId("historyMessage"); message.setWrapText(true);
        Runnable refresh = () -> {
            try {
                var result = store.list(); list.getItems().setAll(result.entries());
                message.setText(result.problems().isEmpty() ? result.entries().size() + " tiragem(ns) salva(s)."
                        : result.problems().size() + " arquivo(s) não puderam ser lidos e foram preservados.");
            } catch (IOException e) { message.setText("Não foi possível ler o histórico. Verifique a pasta e as permissões."); }
        };
        var open = new Button("Reabrir"); open.setId("historyOpen");
        var delete = new Button("Excluir selecionada"); delete.setId("historyDelete");
        open.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        delete.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        open.setOnAction(e -> { reopen.accept(list.getSelectionModel().getSelectedItem()); stage.close(); });
        delete.setOnAction(e -> {
            try { store.delete(list.getSelectionModel().getSelectedItem().id()); refresh.run(); }
            catch (IOException ex) { message.setText("Não foi possível excluir a tiragem. O histórico não foi alterado."); }
        });
        var split = new SplitPane(list, preview); split.setDividerPositions(0.45); VBox.setVgrow(split, Priority.ALWAYS);
        var heading = new Label("✦  Arquivo de tiragens"); heading.getStyleClass().add("title-bar"); heading.setMaxWidth(Double.MAX_VALUE);
        var content = new VBox(10, split, new HBox(10, open, delete), message); content.setPadding(new Insets(12));
        VBox.setVgrow(content, Priority.ALWAYS); open.getStyleClass().add("primary-button");
        var root = new VBox(heading, content);
        var scene = new Scene(root, 860, 560);
        scene.getStylesheets().add(HistoryWindow.class.getResource("/retro.css").toExternalForm());
        stage.setScene(scene); stage.setMinWidth(620); stage.setMinHeight(400); refresh.run(); stage.show();
    }
}
