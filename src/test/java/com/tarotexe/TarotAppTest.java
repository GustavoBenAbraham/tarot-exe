package com.tarotexe;

import com.tarotexe.domain.SpreadType;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** Requires a graphical desktop; actions run on the real JavaFX application thread. */
class TarotAppTest {
    @Test void missingOrCorruptCardImageUsesCachedFallback() throws Exception {
        var badImage = historyFolder.resolve("broken.png"); java.nio.file.Files.writeString(badImage, "invalid image");
        onFx(() -> {
            var requests = new java.util.concurrent.atomic.AtomicInteger();
            var missing = new com.tarotexe.service.CardImages(id -> { requests.incrementAndGet(); return null; });
            assertNull(missing.view(0, false).getImage()); assertNull(missing.view(0, true).getImage());
            assertEquals(1, requests.get());
            try {
                var broken = new com.tarotexe.service.CardImages(id -> {
                    try { return badImage.toUri().toURL(); } catch (java.net.MalformedURLException e) { throw new AssertionError(e); }
                });
                assertNull(broken.view(1, false).getImage());
            } catch (RuntimeException e) { throw new AssertionError("Fallback não pode encerrar o aplicativo", e); }
        });
    }

    @Test void fontControlsAndExportAvailabilityFollowReadingState() throws Exception {
        onFx(() -> {
            var stage = new Stage();
            try {
                new TarotApp().start(stage); var root = stage.getScene().getRoot();
                assertTrue(((Button) root.lookup("#exportReading")).isDisabled());
                ((Button) root.lookup("#draw")).fire();
                assertFalse(((Button) root.lookup("#exportReading")).isDisabled());
                assertFalse(((Button) root.lookup("#copyReading")).isDisabled());
                ((Button) root.lookup("#largerText")).fire();
                assertTrue(((TextArea) root.lookup("#interpretation")).getStyle().contains("16px"));
                ((Button) root.lookup("#smallerText")).fire();
                assertTrue(((TextArea) root.lookup("#interpretation")).getStyle().contains("15px"));
                ((Button) root.lookup("#clear")).fire();
                assertTrue(((Button) root.lookup("#exportReading")).isDisabled());
                assertTrue(((Button) root.lookup("#copyReading")).isDisabled());
            } finally { stage.close(); }
        });
    }
    @Test void aiCompletesAsynchronouslyAndPersistsWithReading() throws Exception {
        var pending = new java.util.concurrent.CompletableFuture<String>();
        var stage = new java.util.concurrent.atomic.AtomicReference<Stage>();
        var original = new java.util.concurrent.atomic.AtomicReference<String>();
        var store = new com.tarotexe.service.HistoryStore(historyFolder);
        try {
            onFx(() -> {
                var window = new Stage(); stage.set(window);
                new TarotApp(store, (reading, settings) -> pending).start(window);
                var root = window.getScene().getRoot();
                assertTrue(((Button) root.lookup("#aiDeepen")).isDisabled());
                ((Button) root.lookup("#draw")).fire();
                original.set(((TextArea) root.lookup("#interpretation")).getText());
                ((Button) root.lookup("#aiDeepen")).fire();
                assertTrue(((Button) root.lookup("#aiDeepen")).isDisabled());
                assertFalse(((Button) root.lookup("#aiCancel")).isDisabled());
                assertFalse(((Button) root.lookup("#draw")).isDisabled());
                assertEquals(original.get(), ((TextArea) root.lookup("#interpretation")).getText());
            });
            pending.complete("Uma reflexão local mais detalhada.");
            onFx(() -> {
                var root = stage.get().getScene().getRoot();
                assertEquals("Uma reflexão local mais detalhada.", ((TextArea) root.lookup("#aiResponse")).getText());
                assertEquals(original.get(), ((TextArea) root.lookup("#interpretation")).getText());
                assertTrue(((Button) root.lookup("#aiCancel")).isDisabled());
                ((Button) root.lookup("#saveReading")).fire();
            });
            var entry = store.list().entries().getFirst();
            assertEquals("Uma reflexão local mais detalhada.", entry.aiText());
            assertEquals(original.get(), entry.interpretation());
        } finally { onFx(() -> { if (stage.get() != null) stage.get().close(); }); }
    }

    @Test void obsoleteAiResponseDoesNotOverwriteNewDrawAndCancelAllowsRetry() throws Exception {
        // Deliberately non-cancellable fake simulates a provider returning after cancellation.
        var obsolete = new java.util.concurrent.CompletableFuture<String>() {
            @Override public boolean cancel(boolean interrupt) { return false; }
        };
        var second = new java.util.concurrent.CompletableFuture<String>();
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        var stage = new java.util.concurrent.atomic.AtomicReference<Stage>();
        try {
            onFx(() -> {
                var window = new Stage(); stage.set(window);
                new TarotApp(new com.tarotexe.service.HistoryStore(historyFolder),
                        (reading, settings) -> calls.getAndIncrement() == 0 ? obsolete : second).start(window);
                var root = window.getScene().getRoot();
                ((Button) root.lookup("#draw")).fire(); ((Button) root.lookup("#aiDeepen")).fire();
                ((Button) root.lookup("#draw")).fire();
            });
            obsolete.complete("Resposta antiga que deve ser ignorada");
            onFx(() -> {
                var root = stage.get().getScene().getRoot();
                assertEquals("", ((TextArea) root.lookup("#aiResponse")).getText());
                ((Button) root.lookup("#aiDeepen")).fire();
                ((Button) root.lookup("#aiCancel")).fire();
                assertTrue(second.isCancelled());
                assertFalse(((Button) root.lookup("#aiDeepen")).isDisabled());
                assertTrue(((Label) root.lookup("#aiStatus")).getText().contains("cancelada"));
            });
        } finally { onFx(() -> { if (stage.get() != null) stage.get().close(); }); }
    }

    @Test void aiFailureKeepsLocalReadingAndOffersRetry() throws Exception {
        var pending = new java.util.concurrent.CompletableFuture<String>();
        var stage = new java.util.concurrent.atomic.AtomicReference<Stage>();
        try {
            onFx(() -> {
                var window = new Stage(); stage.set(window);
                new TarotApp(new com.tarotexe.service.HistoryStore(historyFolder), (reading, settings) -> pending).start(window);
                var root = window.getScene().getRoot();
                ((Button) root.lookup("#draw")).fire(); ((Button) root.lookup("#aiDeepen")).fire();
            });
            pending.completeExceptionally(new java.net.http.HttpTimeoutException("timeout"));
            onFx(() -> {
                var root = stage.get().getScene().getRoot();
                assertTrue(((Label) root.lookup("#aiStatus")).getText().contains("Tempo limite"));
                assertFalse(((Button) root.lookup("#aiDeepen")).isDisabled());
                assertTrue(((TextArea) root.lookup("#interpretation")).getText().contains("PRÓXIMO PASSO"));
            });
        } finally { onFx(() -> { if (stage.get() != null) stage.get().close(); }); }
    }
    @Test void cardsStayLargeAndOpenOriginalImageInResizableWindow() throws Exception {
        onFx(() -> {
            var stage = new Stage();
            try {
                new TarotApp().start(stage);
                var root = stage.getScene().getRoot();
                ((Button) root.lookup("#draw")).fire();
                root.resize(720, 600); root.applyCss(); root.layout();
                var panel = (javafx.scene.layout.VBox) ((Pane) root.lookup("#cards")).getChildren().getFirst();
                var preview = (javafx.scene.image.ImageView) panel.getChildren().getFirst();
                assertTrue(preview.getBoundsInLocal().getHeight() >= 290, "Carta não pode encolher com a janela");
                assertTrue(preview.getBoundsInLocal().getWidth() >= 170);
                assertNotNull(panel.getOnMouseClicked());
                panel.getOnKeyPressed().handle(new javafx.scene.input.KeyEvent(
                        javafx.scene.input.KeyEvent.KEY_PRESSED, "", "", javafx.scene.input.KeyCode.ENTER, false, false, false, false));
                var zoom = (Stage) javafx.stage.Window.getWindows().stream().filter(w -> w instanceof Stage s
                        && s.getTitle().endsWith("Carta ampliada")).findFirst().orElseThrow();
                try {
                    zoom.getScene().getRoot().applyCss(); zoom.getScene().getRoot().layout();
                    var enlarged = (javafx.scene.image.ImageView) zoom.getScene().lookup("#enlargedCard");
                    assertNotNull(enlarged.getImage()); assertFalse(enlarged.getImage().isError());
                    assertTrue(enlarged.getImage().getHeight() > preview.getImage().getHeight());
                    assertTrue(enlarged.getBoundsInLocal().getHeight() > preview.getBoundsInLocal().getHeight());
                    assertEquals(preview.getRotate(), enlarged.getRotate());
                    assertTrue(zoom.isResizable());
                    zoom.getScene().getAccelerators().get(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.ESCAPE)).run();
                    assertFalse(zoom.isShowing());
                } finally { zoom.close(); }
            } finally { stage.close(); }
        });
    }

    @Test void windowControlsResizeMaximizeMinimizeAndFullscreenPreserveReading() throws Exception {
        // Separate FX turns allow Windows to apply native window transitions.
        var stage = new java.util.concurrent.atomic.AtomicReference<Stage>();
        var text = new java.util.concurrent.atomic.AtomicReference<String>();
        try {
            onFx(() -> {
                var window = new Stage(); stage.set(window);
                new TarotApp().start(window);
                assertTrue(window.isResizable());
                assertEquals(javafx.stage.StageStyle.DECORATED, window.getStyle());
                ((Button) window.getScene().lookup("#draw")).fire();
                text.set(((TextArea) window.getScene().lookup("#interpretation")).getText());
                ((Button) window.getScene().lookup("#windowMaximize")).fire();
            });
            onFx(() -> {
                assertTrue(stage.get().isMaximized());
                ((Button) stage.get().getScene().lookup("#windowMaximize")).fire();
            });
            onFx(() -> {
                assertFalse(stage.get().isMaximized());
                stage.get().setWidth(850); stage.get().setHeight(700);
                assertEquals(850, stage.get().getWidth(), 2);
                ((Button) stage.get().getScene().lookup("#windowMinimize")).fire();
            });
            onFx(() -> {
                assertTrue(stage.get().isIconified());
                stage.get().setIconified(false);
                ((Button) stage.get().getScene().lookup("#windowFullscreen")).fire();
            });
            onFx(() -> {
                assertTrue(stage.get().isFullScreen());
                assertEquals(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.ESCAPE), stage.get().getFullScreenExitKeyCombination());
                stage.get().getScene().getAccelerators().get(
                        new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.F11)).run();
            });
            onFx(() -> {
                assertFalse(stage.get().isFullScreen());
                assertEquals(text.get(), ((TextArea) stage.get().getScene().lookup("#interpretation")).getText());
            });
        } finally {
            onFx(() -> { if (stage.get() != null) stage.get().close(); });
        }
    }

    @org.junit.jupiter.api.io.TempDir java.nio.file.Path historyFolder;

    @Test void saveFailureKeepsReadingAndAllowsRetry() throws Exception {
        var blocked = historyFolder.resolve("not-directory");
        java.nio.file.Files.writeString(blocked, "preservar");
        onFx(() -> {
            var stage = new Stage();
            try {
                new TarotApp(new com.tarotexe.service.HistoryStore(blocked)).start(stage);
                var root = stage.getScene().getRoot();
                ((Button) root.lookup("#draw")).fire();
                var text = ((TextArea) root.lookup("#interpretation")).getText();
                var save = (Button) root.lookup("#saveReading"); save.fire();
                assertFalse(save.isDisabled());
                assertEquals(text, ((TextArea) root.lookup("#interpretation")).getText());
                assertTrue(((Label) root.lookup("#status")).getText().startsWith("Não foi possível salvar"));
                ((Button) root.lookup("#history")).fire();
                var window = historyStage();
                assertTrue(((Label) window.getScene().lookup("#historyMessage")).getText().startsWith("Não foi possível ler"));
                window.close();
            } finally { stage.close(); }
        });
    }

    @Test void saveReopenAndDeleteFromRealHistoryWindow() throws Exception {
        onFx(() -> {
            var store = new com.tarotexe.service.HistoryStore(historyFolder);
            var stage = new Stage();
            try {
                new TarotApp(store).start(stage);
                var root = stage.getScene().getRoot();
                var save = (Button) root.lookup("#saveReading");
                assertTrue(save.isDisabled());
                ((TextField) root.lookup("#question")).setText("Como cuidar desta relação?");
                ((Button) root.lookup("#draw")).fire();
                String text = ((TextArea) root.lookup("#interpretation")).getText();
                save.fire(); assertTrue(save.isDisabled());
                ((Button) root.lookup("#clear")).fire();
                stage.close();
                stage = new Stage();
                // Uma nova instância representa a reabertura do aplicativo.
                new TarotApp(new com.tarotexe.service.HistoryStore(historyFolder)).start(stage);
                root = stage.getScene().getRoot();
                ((Button) root.lookup("#history")).fire();
                Stage window = historyStage();
                @SuppressWarnings("unchecked")
                var list = (ListView<com.tarotexe.service.HistoryStore.Entry>) window.getScene().lookup("#historyList");
                assertEquals(1, list.getItems().size());
                list.getSelectionModel().selectFirst();
                assertEquals(text, ((TextArea) window.getScene().lookup("#historyPreview")).getText());
                ((Button) window.getScene().lookup("#historyOpen")).fire();
                assertEquals(text, ((TextArea) root.lookup("#interpretation")).getText());
                assertEquals("Como cuidar desta relação?", ((TextField) root.lookup("#question")).getText());
                assertEquals(3, ((Pane) root.lookup("#cards")).getChildren().size());
                ((Button) root.lookup("#history")).fire();
                window = historyStage();
                ((ListView<?>) window.getScene().lookup("#historyList")).getSelectionModel().selectFirst();
                ((Button) window.getScene().lookup("#historyDelete")).fire();
                assertTrue(((ListView<?>) window.getScene().lookup("#historyList")).getItems().isEmpty());
                window.close();
                try { assertTrue(store.list().entries().isEmpty()); }
                catch (java.io.IOException e) { throw new AssertionError(e); }
            } finally { stage.close(); }
        });
    }

    private Stage historyStage() {
        return (Stage) javafx.stage.Window.getWindows().stream()
                .filter(w -> w instanceof Stage s && s.getTitle().equals("Histórico de tiragens"))
                .findFirst().orElseThrow();
    }

    @Test void longReadingAndImagesRemainAccessibleAtMinimumWindowSize() throws Exception {
        onFx(() -> {
            var stage = new Stage();
            try {
                new TarotApp().start(stage);
                var root = stage.getScene().getRoot();
                ((TextField) root.lookup("#question")).setText("Uma reflexão longa: " + "situação atual ".repeat(30));
                ((Button) root.lookup("#draw")).fire();
                for (double[] size : new double[][]{{1360, 900}, {960, 800}, {720, 600}}) {
                    stage.setWidth(size[0]); stage.setHeight(size[1]);
                    root.applyCss(); root.resize(size[0], size[1]); root.layout();
                    assertEquals(javafx.geometry.Orientation.HORIZONTAL,
                            ((SplitPane) root.lookup("#workspaceColumns")).getOrientation());
                    var left = root.lookup("#cardColumn").localToScene(root.lookup("#cardColumn").getBoundsInLocal());
                    var right = root.lookup("#readingColumn").localToScene(root.lookup("#readingColumn").getBoundsInLocal());
                    assertTrue(right.getMinX() >= left.getMaxX(), "Colunas não devem se sobrepor");
                    var area = (TextArea) root.lookup("#interpretation");
                    assertTrue(area.isWrapText());
                    assertTrue(area.getHeight() >= 80, "Interpretação sem espaço de leitura");
                    assertTrue(area.getText().contains("PRÓXIMO PASSO"));
                    var cards = (Pane) root.lookup("#cards");
                    assertEquals(3, cards.getChildren().size());
                    for (var card : cards.getChildren()) {
                        var panel = (javafx.scene.layout.VBox) card;
                        assertInstanceOf(javafx.scene.image.ImageView.class, panel.getChildren().getFirst());
                    }
                    var shot = root.snapshot(null, null);
                    var buffered = new java.awt.image.BufferedImage((int) shot.getWidth(), (int) shot.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    for (int y = 0; y < buffered.getHeight(); y++)
                        for (int x = 0; x < buffered.getWidth(); x++) buffered.setRGB(x, y, shot.getPixelReader().getArgb(x, y));
                    try {
                        javax.imageio.ImageIO.write(buffered, "png", new java.io.File("target/interface-" + (int) size[0] + ".png"));
                    } catch (java.io.IOException e) { throw new AssertionError(e); }
                }
            } finally { stage.close(); }
        });
    }

    @Test void all78BundledImagesDecodeAndReversedImagesRotate() throws Exception {
        onFx(() -> {
            var images = new com.tarotexe.service.CardImages();
            var digests = new java.util.HashSet<String>();
            for (int id = 0; id < 78; id++) {
                var view = images.view(id, false);
                assertNotNull(view.getImage(), "Imagem ausente: " + id);
                assertFalse(view.getImage().isError(), "Imagem inválida: " + id);
                assertTrue(view.getImage().getWidth() > 0);
                assertEquals(0, view.getRotate());
                var inverted = images.view(id, true);
                assertEquals(180, inverted.getRotate());
                assertSame(view.getImage(), inverted.getImage());
                try (var stream = getClass().getResourceAsStream("/cards/%02d.jpg".formatted(id))) {
                    String digest = java.util.HexFormat.of().formatHex(
                            java.security.MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes()));
                    assertTrue(digests.add(digest), "Arquivo de imagem duplicado: " + id);
                } catch (Exception e) { throw new AssertionError(e); }
            }
        });
    }

    @BeforeAll static void startToolkit() throws Exception {
        var ready = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); ready.countDown(); });
        assertTrue(ready.await(15, TimeUnit.SECONDS), "JavaFX startup timed out");
    }

    @AfterAll static void stopToolkit() { Platform.exit(); }

    private void onFx(Runnable checks) throws Exception {
        var task = new FutureTask<Void>(() -> { checks.run(); return null; });
        Platform.runLater(task);
        task.get(15, TimeUnit.SECONDS);
    }

    @Test void drawSwitchSpreadAndClear() throws Exception {
        onFx(() -> {
            var stage = new Stage();
            try {
                new TarotApp().start(stage);
                var root = stage.getScene().getRoot();
                var question = (TextField) root.lookup("#question");
                var draw = (Button) root.lookup("#draw");
                var cards = (Pane) root.lookup("#cards");
                var interpretation = (TextArea) root.lookup("#interpretation");
                question.setText("Como seguir?");
                @SuppressWarnings("unchecked")
                var theme = (ComboBox<com.tarotexe.domain.ReadingTheme>) root.lookup("#readingTheme");
                theme.setValue(com.tarotexe.domain.ReadingTheme.WORK);
                draw.fire();
                assertEquals(3, cards.getChildren().size());
                assertTrue(interpretation.getText().contains("Como seguir?"));
                assertTrue(interpretation.getText().contains("FOCO: Trabalho e projetos"));
                @SuppressWarnings("unchecked")
                var spread = (ComboBox<SpreadType>) root.lookup("#spread");
                spread.setValue(SpreadType.ONE);
                ((CheckBox) root.lookup("#reversed")).setSelected(false);
                draw.fire();
                assertEquals(1, cards.getChildren().size());
                assertTrue(interpretation.getText().contains("(Normal)"));
                assertFalse(interpretation.getText().contains("(Invertida)"));
                ((Button) root.lookup("#clear")).fire();
                assertTrue(cards.getChildren().isEmpty());
                assertEquals("", question.getText());
                assertEquals("", interpretation.getText());
                assertEquals("0/500 caracteres", ((Label) root.lookup("#questionCount")).getText());
                assertEquals("Pronto para nova tiragem.", ((Label) root.lookup("#status")).getText());
            } finally { stage.close(); }
        });
    }

    @Test void questionLimitShowsErrorAndPreventsDrawUntilCorrected() throws Exception {
        onFx(() -> {
            var stage = new Stage();
            try {
                new TarotApp().start(stage);
                var root = stage.getScene().getRoot();
                var question = (TextField) root.lookup("#question");
                var draw = (Button) root.lookup("#draw");
                var error = (Label) root.lookup("#questionError");
                question.setText("a".repeat(500));
                assertFalse(draw.isDisabled());
                assertFalse(error.isVisible());
                question.appendText("a");
                assertTrue(draw.isDisabled());
                assertTrue(error.isVisible());
                assertEquals("Pergunta deve ter até 500 caracteres.", error.getText());
                assertEquals("501/500 caracteres", ((Label) root.lookup("#questionCount")).getText());
                draw.fire();
                assertTrue(((Pane) root.lookup("#cards")).getChildren().isEmpty());
                question.setText("Corrigida");
                assertFalse(draw.isDisabled());
                assertFalse(error.isVisible());
                draw.fire();
                assertEquals(3, ((Pane) root.lookup("#cards")).getChildren().size());
            } finally { stage.close(); }
        });
    }
}
