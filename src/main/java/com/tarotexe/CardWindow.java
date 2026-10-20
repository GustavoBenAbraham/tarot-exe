package com.tarotexe;

import com.tarotexe.domain.DrawnCard;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;

final class CardWindow {
    static void show(Stage owner, DrawnCard card) {
        var stage = new Stage(); stage.initOwner(owner);
        AppInfo.decorate(stage);
        stage.setTitle(card.card().name() + " — Carta ampliada");
        var title = new Label(card.card().name() + " · " + card.orientation());
        title.getStyleClass().add("section-heading");
        var image = new ImageView(); image.setId("enlargedCard"); image.setPreserveRatio(true);
        var resource = CardWindow.class.getResource("/cards/%02d.jpg".formatted(card.card().id()));
        if (resource != null) {
            var loaded = new Image(resource.toExternalForm());
            if (!loaded.isError()) image.setImage(loaded);
        }
        image.setRotate(card.reversed() ? 180 : 0);
        image.setAccessibleText(card.card().name() + " — " + card.orientation());
        var display = new StackPane(image); VBox.setVgrow(display, Priority.ALWAYS); display.setMinSize(0, 0);
        if (image.getImage() == null) display.getChildren().add(new Label("Imagem indisponível. O nome e a leitura da carta continuam disponíveis."));
        image.fitWidthProperty().bind(javafx.beans.binding.Bindings.max(1, display.widthProperty().subtract(12)));
        image.fitHeightProperty().bind(javafx.beans.binding.Bindings.max(1, display.heightProperty().subtract(12)));
        var close = new Button("Fechar · Esc"); close.setOnAction(e -> stage.close());
        var root = new VBox(10, title, display, close); root.setAlignment(Pos.CENTER); root.setPadding(new Insets(14));
        var scene = new Scene(root, 500, 760);
        scene.getStylesheets().add(CardWindow.class.getResource("/retro.css").toExternalForm());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), stage::close);
        stage.setScene(scene); stage.setMinWidth(300); stage.setMinHeight(420);
        var bounds = Screen.getPrimary().getVisualBounds();
        stage.setHeight(Math.min(800, bounds.getHeight())); stage.setWidth(Math.min(520, bounds.getWidth()));
        stage.show();
    }
}
