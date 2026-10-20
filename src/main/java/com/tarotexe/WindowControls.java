package com.tarotexe;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

final class WindowControls {
    static HBox create(Stage stage) {
        var minimize = new Button("Minimizar"); minimize.setId("windowMinimize");
        var maximize = new Button(); maximize.setId("windowMaximize");
        var fullscreen = new Button(); fullscreen.setId("windowFullscreen");
        maximize.textProperty().bind(Bindings.when(stage.maximizedProperty()).then("Restaurar").otherwise("Maximizar"));
        fullscreen.textProperty().bind(Bindings.when(stage.fullScreenProperty()).then("Sair da tela cheia").otherwise("Tela cheia"));
        minimize.setOnAction(e -> { stage.setFullScreen(false); stage.setIconified(true); });
        maximize.setOnAction(e -> {
            stage.setFullScreen(false); stage.setIconified(false);
            stage.setMaximized(!stage.isMaximized());
        });
        fullscreen.setOnAction(e -> stage.setFullScreen(!stage.isFullScreen()));
        fullscreen.setTooltip(new Tooltip("F11 alterna tela cheia. Esc retorna à janela."));
        stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.ESCAPE));
        stage.setFullScreenExitHint("Pressione Esc ou F11 para sair da tela cheia.");
        stage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F11), fullscreen::fire);
        var controls = new HBox(6, minimize, maximize, fullscreen);
        controls.setAlignment(Pos.CENTER_RIGHT);
        return controls;
    }
}
