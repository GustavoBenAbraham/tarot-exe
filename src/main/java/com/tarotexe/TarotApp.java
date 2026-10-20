package com.tarotexe;

import com.tarotexe.domain.*;
import com.tarotexe.engine.RandomEngine;
import com.tarotexe.service.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Screen;

public final class TarotApp extends Application {
    private final HistoryStore history;
    private final AiInterpreter ai;
    private AiSettings aiSettings = AiSettings.defaults();
    private java.util.concurrent.CompletableFuture<String> pendingAi;
    private long aiGeneration;
    private String responseModel = "";
    private final Button deepen = new Button("Aprofundar com IA");
    private final Button cancelAi = new Button("Cancelar");
    private final Label aiStatus = new Label("IA local opcional · qwen2.5:3b");
    private final TextArea aiText = new TextArea();
    private final TabPane readingTabs = new TabPane();
    private final Button export = new Button("Exportar .txt");
    private final Button copy = new Button("Copiar leitura");
    private final javafx.beans.property.IntegerProperty readingFontSize = new javafx.beans.property.SimpleIntegerProperty(15);
    private javafx.animation.Timeline aiClock;
    private Reading currentReading;
    private boolean currentAllowReversed;
    private final Button save = new Button("Salvar tiragem");
    public TarotApp() { this(HistoryStore.local()); }
    public TarotApp(HistoryStore history) { this(history, new OllamaService()); }
    public TarotApp(HistoryStore history, AiInterpreter ai) {
        this.history = history; this.ai = ai;
        try { aiSettings = AiSettings.load(AiSettings.localPath()); aiStatus.setText("IA local opcional · " + aiSettings.model()); }
        catch (java.io.IOException e) { aiStatus.setText("Configuração de IA inválida; usando padrão. Abra Configurar IA."); }
    }
    private final CardImages cardImages = new CardImages();
    private final TarotService tarot = new TarotService(new RandomEngine(), new LocalInterpretationService());
    private final ComboBox<SpreadType> spread = new ComboBox<>();
    private final ComboBox<ReadingTheme> readingTheme = new ComboBox<>();
    private final TextField question = new TextField();
    private final CheckBox reversed = new CheckBox("Permitir invertidas");
    private final TilePane cards = new TilePane(14, 14);
    private final TextArea interpretation = new TextArea();
    private final Label status = new Label("Pronto para iniciar uma tiragem.");

    @Override public void start(Stage stage) {
        MenuBar menu = new MenuBar();
        Menu file = new Menu("Arquivo"); MenuItem exit = new MenuItem("Sair"); exit.setOnAction(e -> stage.close()); file.getItems().add(exit);
        Menu help = new Menu("Ajuda"); MenuItem about = new MenuItem("Sobre Tarot.exe");
        about.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, "Tarot.exe " + AppInfo.VERSION + "\nLeitura por catálogo e aprofundamento opcional com Ollama local.\nIlustrações: Pamela Colman Smith, Rider–Waite–Smith (1909).\nHistórico e configurações: .tarot-exe, na sua pasta de usuário.").showAndWait());
        help.getItems().add(about); menu.getMenus().addAll(file, help);

        Label title = new Label("✦  TAROT.EXE  ·  Gabinete dos Arcanos"); title.getStyleClass().add("title-bar"); title.setMaxWidth(Double.MAX_VALUE);
        spread.getItems().addAll(SpreadType.values()); spread.setValue(SpreadType.THREE);
        readingTheme.getItems().addAll(ReadingTheme.values()); readingTheme.setValue(ReadingTheme.GENERAL);
        readingTheme.setId("readingTheme");
        reversed.setSelected(true); question.setPromptText("Sua pergunta (opcional, até 500 caracteres)");
        Button draw = new Button("☷ Embaralhar e tirar cartas"); draw.setDefaultButton(true);
        Button clear = new Button("Nova tiragem");
        save.setId("saveReading"); save.setDisable(true);
        export.setId("exportReading"); export.setDisable(true); copy.setId("copyReading"); copy.setDisable(true);
        export.setOnAction(e -> {
            if (currentReading == null) return;
            var chooser = new javafx.stage.FileChooser(); chooser.setTitle("Exportar leitura");
            chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Arquivo de texto", "*.txt"));
            chooser.setInitialFileName("Tarot-" + currentReading.createdAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm")) + ".txt");
            var selected = chooser.showSaveDialog(stage);
            if (selected != null) try { ReadingExport.write(selected.toPath(), exportText()); status.setText("Leitura exportada com sucesso."); }
            catch (java.io.IOException ex) { AppLog.record("Falha ao exportar", ex); status.setText("Não foi possível exportar. Escolha outra pasta e tente novamente."); }
        });
        copy.setOnAction(e -> {
            if (currentReading == null) return;
            var content = new javafx.scene.input.ClipboardContent(); content.putString(exportText());
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(content); status.setText("Leitura copiada.");
        });
        Button historyButton = new Button("Histórico"); historyButton.setId("history");
        historyButton.setOnAction(e -> HistoryWindow.show(stage, history, entry -> {
            currentAllowReversed = entry.allowReversed(); reversed.setSelected(currentAllowReversed);
            spread.setValue(entry.reading().type()); readingTheme.setValue(entry.reading().theme());
            question.setText(entry.reading().question()); display(entry.reading(), entry.interpretation());
            aiText.setText(entry.aiText()); responseModel = entry.aiModel();
            if (!entry.aiText().isBlank()) aiStatus.setText("Resposta salva · " + entry.aiModel());
            save.setDisable(true); status.setText("Tiragem reaberta do histórico.");
        }));
        save.setOnAction(e -> {
            if (currentReading == null) return;
            try {
                history.save(currentReading, interpretation.getText(), currentAllowReversed, aiText.getText(), responseModel);
                save.setDisable(true); status.setText("Tiragem salva no histórico local.");
            } catch (java.io.IOException ex) {
                AppLog.record("Falha ao salvar tiragem", ex);
                status.setText("Não foi possível salvar. Verifique espaço e permissões da pasta; a leitura continua disponível.");
            }
        });
        question.setId("question"); spread.setId("spread"); reversed.setId("reversed");
        cards.setId("cards"); interpretation.setId("interpretation"); status.setId("status");
        draw.setId("draw"); clear.setId("clear");
        Label questionCount = new Label(); questionCount.setId("questionCount");
        questionCount.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(
                () -> question.getText().length() + "/500 caracteres", question.textProperty()));
        Label questionError = new Label("Pergunta deve ter até 500 caracteres.");
        questionError.setId("questionError");
        questionError.setStyle("-fx-text-fill: #a00000;");
        var questionTooLong = question.lengthProperty().greaterThan(500);
        questionError.visibleProperty().bind(questionTooLong);
        questionError.managedProperty().bind(questionTooLong);
        draw.disableProperty().bind(questionTooLong.or(spread.valueProperty().isNull()).or(readingTheme.valueProperty().isNull()));
        FlowPane settings = new FlowPane(10, 6, new Label("Tipo:"), spread, reversed, new Label("Tema:"), readingTheme);
        settings.setAlignment(Pos.CENTER_LEFT);
        settings.getChildren().addAll(draw, clear, save, historyButton);
        Region questionSpacer = new Region(); HBox.setHgrow(questionSpacer, Priority.ALWAYS);
        HBox questionHeader = new HBox(new Label("PERGUNTA"), questionSpacer, questionCount);
        VBox controls = new VBox(8, questionHeader, question, questionError, settings); controls.getStyleClass().add("panel");

        draw.getStyleClass().add("primary-button");
        cards.setAlignment(Pos.TOP_CENTER); cards.setTileAlignment(Pos.TOP_CENTER); cards.setPrefColumns(3); cards.setMinWidth(0); cards.setPadding(new Insets(12));
        ScrollPane scroll = new ScrollPane(cards); scroll.setId("cardsScroll"); scroll.setFitToWidth(true); scroll.setPrefHeight(350); scroll.setMinHeight(80); scroll.getStyleClass().add("card-table");
        scroll.setFitToHeight(false);
        interpretation.setEditable(false); interpretation.setWrapText(true); interpretation.setPrefRowCount(12); interpretation.setMinHeight(80);
        interpretation.getStyleClass().add("reading-paper");
        interpretation.styleProperty().bind(readingFontSize.asString("-fx-font-size: %dpx;"));
        interpretation.setPromptText("Embaralhe as cartas e reserve um momento para sua leitura.");
        Label cardsHeading = new Label("✧  As cartas"); cardsHeading.getStyleClass().add("section-heading");
        Label readingHeading = new Label("✧  Caderno de leitura"); readingHeading.getStyleClass().add("section-heading");
        aiText.setId("aiResponse"); aiText.setEditable(false); aiText.setWrapText(true); aiText.getStyleClass().add("reading-paper");
        aiText.styleProperty().bind(readingFontSize.asString("-fx-font-size: %dpx;"));
        aiText.setPromptText("Sorteie as cartas e clique em Aprofundar com IA.\nA leitura local permanece disponível enquanto o Ollama responde.");
        aiText.setMinHeight(80); readingTabs.setId("readingTabs"); readingTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        readingTabs.getTabs().addAll(new Tab("Leitura local", interpretation), new Tab("IA local", aiText));
        deepen.setId("aiDeepen"); deepen.setDisable(true); cancelAi.setId("aiCancel"); cancelAi.setDisable(true);
        deepen.setOnAction(e -> startAi()); cancelAi.setOnAction(e -> { cancelPendingAi(); aiStatus.setText("Geração cancelada. A leitura local está disponível."); });
        var configureAi = new Button("Configurar IA"); configureAi.setId("aiConfigure");
        configureAi.setOnAction(e -> AiSettingsDialog.show(stage, aiSettings, settingsValue -> {
            cancelPendingAi(); aiSettings = settingsValue;
            try { aiSettings.save(AiSettings.localPath()); aiStatus.setText("Configuração salva · " + aiSettings.model()); }
            catch (java.io.IOException ex) { aiStatus.setText("Configuração aplicada nesta sessão; não foi possível salvar no disco."); }
        }));
        aiStatus.setId("aiStatus"); aiStatus.setWrapText(true); aiStatus.getStyleClass().add("ai-status");
        var aiToolbar = new FlowPane(6, 6, deepen, cancelAi, configureAi); aiToolbar.getStyleClass().add("ai-toolbar");
        var smallerText = new Button("A−"); smallerText.setId("smallerText"); smallerText.setTooltip(new Tooltip("Diminuir texto"));
        var largerText = new Button("A+"); largerText.setId("largerText"); largerText.setTooltip(new Tooltip("Aumentar texto"));
        smallerText.setOnAction(e -> readingFontSize.set(Math.max(12, readingFontSize.get() - 1)));
        largerText.setOnAction(e -> readingFontSize.set(Math.min(22, readingFontSize.get() + 1)));
        var readingActions = new FlowPane(6, 6, copy, export, smallerText, largerText);
        VBox cardSection = new VBox(6, cardsHeading, scroll); cardSection.setId("cardColumn"); VBox.setVgrow(scroll, Priority.ALWAYS); cardSection.setMinSize(240, 110);
        VBox readingSection = new VBox(6, readingHeading, aiToolbar, aiStatus, readingActions, readingTabs); readingSection.setId("readingColumn");
        VBox.setVgrow(readingTabs, Priority.ALWAYS); readingSection.setMinSize(280, 180); readingTabs.setMinHeight(110);
        SplitPane readingSplit = new SplitPane(cardSection, readingSection); readingSplit.setId("workspaceColumns");
        readingSplit.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        readingSplit.setDividerPositions(0.58); readingSplit.getStyleClass().add("reading-split");
        VBox body = new VBox(10, controls, readingSplit); VBox.setVgrow(readingSplit, Priority.ALWAYS);
        VBox container = new VBox(menu, body, status); VBox.setVgrow(body, Priority.ALWAYS);
        body.setPadding(new Insets(12)); status.getStyleClass().add("status"); status.setWrapText(true);
        draw.setOnAction(e -> {
            try {
                Reading reading = tarot.draw(question.getText(), spread.getValue(), reversed.isSelected(), readingTheme.getValue());
                currentAllowReversed = reversed.isSelected();
                display(reading, tarot.interpret(reading)); save.setDisable(false);
                status.setText("Tiragem concluída: " + reading.cards().size() + " carta(s). Interpretação offline.");
            } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait(); }
        });
        clear.setOnAction(e -> { cancelPendingAi(); currentReading = null; deepen.setDisable(true); aiText.clear(); responseModel = ""; save.setDisable(true); export.setDisable(true); copy.setDisable(true); cards.getChildren().clear(); interpretation.clear(); question.clear(); status.setText("Pronto para nova tiragem."); });
        Scene scene = new Scene(container, 960, 800);
        scene.getStylesheets().add(getClass().getResource("/retro.css").toExternalForm());
        stage.setTitle("Tarot.exe - Windows Retro Edition"); stage.setScene(scene); stage.setResizable(true);
        var windowControls = WindowControls.create(stage);
        windowControls.getStyleClass().add("window-toolbar");
        windowControls.setPadding(new Insets(4, 12, 4, 12));
        var titleRow = new HBox(title, windowControls); HBox.setHgrow(title, Priority.ALWAYS); titleRow.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(1, titleRow);
        var available = Screen.getPrimary().getVisualBounds();
        UiPreferences preferences = UiPreferences.defaults();
        boolean rememberWindow = getParameters() != null;
        if (rememberWindow) try { preferences = UiPreferences.load(UiPreferences.localPath()); }
        catch (java.io.IOException e) { AppLog.record("Preferências de janela inválidas; usando padrão", e); }
        readingFontSize.set(preferences.fontSize()); readingSplit.setDividerPositions(preferences.divider());
        stage.setMinWidth(Math.min(720, available.getWidth()));
        stage.setMinHeight(Math.min(600, available.getHeight()));
        stage.setWidth(Math.min(preferences.width(), available.getWidth()));
        stage.setHeight(Math.min(preferences.height(), available.getHeight()));
        var normalWidth = new javafx.beans.property.SimpleDoubleProperty(stage.getWidth());
        var normalHeight = new javafx.beans.property.SimpleDoubleProperty(stage.getHeight());
        stage.widthProperty().addListener((o, before, value) -> { if (!stage.isMaximized() && !stage.isFullScreen()) normalWidth.set(value.doubleValue()); });
        stage.heightProperty().addListener((o, before, value) -> { if (!stage.isMaximized() && !stage.isFullScreen()) normalHeight.set(value.doubleValue()); });
        stage.addEventHandler(javafx.stage.WindowEvent.WINDOW_HIDDEN, e -> cancelPendingAi());
        stage.addEventHandler(javafx.stage.WindowEvent.WINDOW_HIDDEN, e -> {
            if (rememberWindow) try {
                new UiPreferences(Math.max(720, normalWidth.get()), Math.max(600, normalHeight.get()), stage.isMaximized(),
                        Math.max(0.2, Math.min(0.8, readingSplit.getDividerPositions()[0])), readingFontSize.get()).save(UiPreferences.localPath());
            } catch (java.io.IOException | IllegalArgumentException ex) { AppLog.record("Não foi possível salvar preferências", ex); }
        });
        AppInfo.decorate(stage); stage.setMaximized(preferences.maximized());
        stage.show();
    }
    private void display(Reading reading, String text) {
        cancelPendingAi(); aiText.clear(); responseModel = ""; readingTabs.getSelectionModel().select(0);
        currentReading = reading; cards.getChildren().clear();
        for (DrawnCard selected : reading.cards()) cards.getChildren().add(cardPanel(selected));
        interpretation.setText(text); interpretation.positionCaret(0);
        deepen.setDisable(false); aiStatus.setText("IA local opcional · " + aiSettings.model());
        export.setDisable(false); copy.setDisable(false);
    }
    private void cancelPendingAi() {
        aiGeneration++;
        if (aiClock != null) { aiClock.stop(); aiClock = null; }
        if (pendingAi != null) pendingAi.cancel(true);
        pendingAi = null; cancelAi.setDisable(true); deepen.setDisable(currentReading == null);
    }
    private void startAi() {
        if (currentReading == null || pendingAi != null) return;
        long generation = ++aiGeneration; Reading snapshot = currentReading; AiSettings settings = aiSettings;
        deepen.setDisable(true); cancelAi.setDisable(false); aiStatus.setText("Gerando com " + settings.model() + "… Você pode continuar lendo ou cancelar.");
        long started = System.nanoTime();
        aiClock = new javafx.animation.Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e ->
                aiStatus.setText("Gerando com " + settings.model() + " · " + ((System.nanoTime() - started) / 1_000_000_000L)
                        + " s · limite " + settings.timeoutSeconds() + " s")));
        aiClock.setCycleCount(javafx.animation.Animation.INDEFINITE); aiClock.play();
        readingTabs.getSelectionModel().select(1);
        try {
            pendingAi = ai.deepen(snapshot, settings);
            pendingAi.whenComplete((text, error) -> javafx.application.Platform.runLater(() -> {
                if (generation != aiGeneration || snapshot != currentReading) return;
                if (aiClock != null) { aiClock.stop(); aiClock = null; }
                pendingAi = null; deepen.setDisable(false); cancelAi.setDisable(true);
                if (error == null) {
                    aiText.setText(text); aiText.positionCaret(0); responseModel = settings.model(); save.setDisable(false);
                    aiStatus.setText("Gerado por IA local · " + settings.model() + " · revise a interpretação.");
                } else {
                    AppLog.record("Falha ao gerar aprofundamento local", error);
                    Throwable cause = error;
                    while ((cause instanceof java.util.concurrent.CompletionException || cause instanceof java.util.concurrent.ExecutionException)
                            && cause.getCause() != null) cause = cause.getCause();
                    String message = cause instanceof java.net.http.HttpTimeoutException ? "Tempo limite excedido. Tente novamente ou aumente o tempo em Configurar IA."
                            : cause instanceof java.net.ConnectException ? "Ollama indisponível. Abra o Ollama e tente novamente."
                            : cause.getMessage();
                    aiStatus.setText((message == null ? "Falha ao gerar com IA." : message) + " A leitura local foi preservada.");
                }
            }));
        } catch (RuntimeException e) { cancelPendingAi(); aiStatus.setText("Não foi possível iniciar a IA. A leitura local foi preservada."); }
    }
    private String exportText() { return ReadingExport.text(currentReading, interpretation.getText(), aiText.getText(), responseModel); }
    private VBox cardPanel(DrawnCard drawn) {
        Label icon = new Label(drawn.reversed() ? "Ⅹ ✧ Ⅹ" : "✦ ✧ ✦"); icon.getStyleClass().add("card-icon");
        Label name = new Label(drawn.card().name()); name.setWrapText(true);
        name.getStyleClass().add("card-name");
        Label orientation = new Label(drawn.orientation()); Label position = new Label(drawn.position());
        orientation.getStyleClass().add("card-orientation"); position.getStyleClass().add("card-position");
        var image = cardImages.view(drawn.card().id(), drawn.reversed());
        image.setAccessibleText(drawn.card().name() + " — " + drawn.orientation());
        Label hint = new Label("Clique para ampliar"); hint.getStyleClass().add("card-orientation");
        VBox panel = new VBox(6, image.getImage() == null ? icon : image, name, orientation, position, hint); panel.setAlignment(Pos.CENTER);
        panel.getStyleClass().add("tarot-card"); panel.setPrefSize(220, 410); panel.setMinSize(220, 410);
        panel.setFocusTraversable(true); panel.setCursor(javafx.scene.Cursor.HAND);
        panel.setAccessibleText(drawn.card().name() + ": ampliar carta com Enter");
        panel.setOnMouseClicked(e -> CardWindow.show((Stage) panel.getScene().getWindow(), drawn));
        panel.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER || e.getCode() == javafx.scene.input.KeyCode.SPACE) {
                CardWindow.show((Stage) panel.getScene().getWindow(), drawn); e.consume();
            }
        });
        return panel;
    }
    public static void main(String[] args) { launch(args); }
}
