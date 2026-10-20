package com.tarotexe;

import javafx.scene.image.Image;
import javafx.stage.Stage;

final class AppInfo {
    static final String VERSION = "0.2.0";
    private static Image icon;
    static void decorate(Stage stage) {
        if (icon == null) {
            var source = AppInfo.class.getResource("/app-icon.png");
            if (source != null) icon = new Image(source.toExternalForm());
        }
        if (icon != null && !icon.isError()) stage.getIcons().setAll(icon);
    }
}
