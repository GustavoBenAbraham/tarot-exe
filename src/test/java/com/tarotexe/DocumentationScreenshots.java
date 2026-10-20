package com.tarotexe;

import com.tarotexe.domain.*;
import com.tarotexe.service.*;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

/** Capturas reais da interface, executadas explicitamente; sem dados pessoais ou chamadas à IA. */
class DocumentationScreenshots {
    @TempDir Path history;

    @Test void captureFeaturesForReadme() throws Exception {
        var toolkit = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); toolkit.countDown(); });
        if (!toolkit.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("JavaFX não iniciou");
        var capture = new FutureTask<Void>(() -> {
            var stage = new Stage();
            try {
                var deck = TarotDeck.standard();
                var reading = new Reading("Como posso organizar melhor meu projeto pessoal?", SpreadType.THREE,
                        List.of(new DrawnCard(deck.get(1), false, "Situação"), new DrawnCard(deck.get(31), true, "Desafio"),
                                new DrawnCard(deck.get(71), false, "Conselho")), LocalDateTime.of(2026, 10, 20, 10, 30), ReadingTheme.WORK);
                var store = new HistoryStore(history);
                store.save(reading, new LocalInterpretationService().interpret(reading), true);
                new TarotApp(store).start(stage);
                var root = stage.getScene().getRoot();
                ((Button) root.lookup("#history")).fire();
                var archive = findStage("Histórico de tiragens");
                ((ListView<?>) archive.getScene().lookup("#historyList")).getSelectionModel().selectFirst();
                ((Button) archive.getScene().lookup("#historyOpen")).fire();
                capture(root, 1400, 900, "01-leitura-local.png");
                var tabs = (TabPane) root.lookup("#readingTabs"); tabs.getSelectionModel().select(1);
                capture(root, 1400, 900, "02-probabilidades.png");
                tabs.getSelectionModel().select(0);
                var card = ((Pane) root.lookup("#cards")).getChildren().get(1);
                card.getOnKeyPressed().handle(new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED,
                        "", "", javafx.scene.input.KeyCode.ENTER, false, false, false, false));
                var zoom = findStage("Dez de Paus — Carta ampliada");
                capture(zoom.getScene().getRoot(), 520, 780, "03-carta-ampliada.png"); zoom.close();
                ((Button) root.lookup("#history")).fire(); archive = findStage("Histórico de tiragens");
                ((ListView<?>) archive.getScene().lookup("#historyList")).getSelectionModel().selectFirst();
                capture(archive.getScene().getRoot(), 1120, 700, "04-historico.png"); archive.close();
                return null;
            } finally { stage.close(); }
        });
        Platform.runLater(capture);
        try { capture.get(40, TimeUnit.SECONDS); }
        finally { Platform.exit(); }
    }

    private static Stage findStage(String title) {
        return (Stage) javafx.stage.Window.getWindows().stream()
                .filter(w -> w instanceof Stage s && s.getTitle().equals(title)).findFirst().orElseThrow();
    }
    private static void capture(Parent root, int width, int height, String filename) throws Exception {
        root.resize(width, height); root.applyCss(); root.layout();
        var shot = root.snapshot(null, null);
        var output = new java.awt.image.BufferedImage((int) shot.getWidth(), (int) shot.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < output.getHeight(); y++) for (int x = 0; x < output.getWidth(); x++)
            output.setRGB(x, y, shot.getPixelReader().getArgb(x, y));
        Path path = Path.of("docs/screenshots", filename); Files.createDirectories(path.getParent());
        javax.imageio.ImageIO.write(output, "png", path.toFile());
    }
}
