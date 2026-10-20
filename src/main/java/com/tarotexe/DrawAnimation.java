package com.tarotexe;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

/** Presentation only: never draws cards or consumes the random generator. */
final class DrawAnimation {
    private SequentialTransition sequence;
    private Runnable completion;

    void play(TilePane table, List<Node> faces, Runnable onFinished) {
        cancel(); completion = onFinished;
        var deck = new StackPane(); deck.setPrefSize(220, 410); deck.setMinSize(220, 410);
        var layers = new ArrayList<Node>();
        for (int i = 0; i < 6; i++) {
            var back = back(); back.setTranslateX(i * 3); back.setTranslateY(-i * 3);
            layers.add(back); deck.getChildren().add(back);
        }
        table.getChildren().setAll(deck);
        var shuffle = new Timeline();
        for (int step = 1; step <= 8; step++) {
            var values = new ArrayList<KeyValue>();
            for (int i = 0; i < layers.size(); i++) {
                double offset = step == 8 ? i * 3 : ((i + step) % 2 == 0 ? -32 : 32) + i * 2;
                values.add(new KeyValue(layers.get(i).translateXProperty(), offset, Interpolator.LINEAR));
                values.add(new KeyValue(layers.get(i).translateYProperty(), -i * 3 + (step % 2) * 5, Interpolator.LINEAR));
            }
            shuffle.getKeyFrames().add(new KeyFrame(Duration.millis(step * 115), values.toArray(KeyValue[]::new)));
        }
        var holders = new ArrayList<StackPane>();
        for (int i = 0; i < faces.size(); i++) {
            var holder = new StackPane(back()); holder.setPrefSize(220, 410); holder.setMinSize(220, 410);
            holder.setTranslateY(-18); holder.setOpacity(0); holder.setMouseTransparent(true);
            holders.add(holder);
        }
        shuffle.setOnFinished(e -> table.getChildren().setAll(holders));
        sequence = new SequentialTransition(shuffle);
        for (int i = 0; i < holders.size(); i++) {
            var holder = holders.get(i); Node face = faces.get(i);
            var deal = new Timeline(new KeyFrame(Duration.millis(180),
                    new KeyValue(holder.translateYProperty(), 0, Interpolator.LINEAR),
                    new KeyValue(holder.opacityProperty(), 1, Interpolator.LINEAR)));
            var close = new ScaleTransition(Duration.millis(140), holder); close.setFromX(1); close.setToX(0);
            close.setOnFinished(e -> holder.getChildren().setAll(face));
            var open = new ScaleTransition(Duration.millis(140), holder); open.setFromX(0); open.setToX(1);
            sequence.getChildren().addAll(deal, new PauseTransition(Duration.millis(100)), close, open);
        }
        sequence.setOnFinished(e -> finish()); sequence.play();
    }

    void finish() {
        Runnable done = completion; cancel(); if (done != null) done.run();
    }
    void cancel() {
        if (sequence != null) sequence.stop(); sequence = null; completion = null;
    }
    private static StackPane back() {
        var canvas = new Canvas(180, 290); var g = canvas.getGraphicsContext2D();
        g.setFill(Color.web("#eeeeee")); g.fillRect(0, 0, 180, 290);
        g.setFill(Color.web("#000080")); g.fillRect(5, 5, 170, 280);
        g.setFill(Color.web("#b0b0da"));
        for (int y = 10; y < 280; y += 8) for (int x = 10; x < 170; x += 8)
            if ((x / 8 + y / 8) % 2 == 0) g.fillRect(x, y, 3, 3);
        g.setFill(Color.web("#eeeeee")); g.fillRect(29, 126, 122, 38);
        g.setStroke(Color.web("#000080")); g.strokeRect(32.5, 129.5, 115, 31);
        g.setFill(Color.web("#000080")); g.setFont(Font.font("Monospaced", 18)); g.fillText("TAROT.EXE", 41, 151);
        var result = new StackPane(canvas); result.setAlignment(Pos.CENTER);
        result.setMaxSize(180, 290); result.setMinSize(180, 290); result.setAccessibleText("Verso da carta");
        return result;
    }
}
