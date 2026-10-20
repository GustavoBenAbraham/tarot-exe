package com.tarotexe;

import com.tarotexe.domain.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import java.time.*;
import java.time.format.*;
import java.util.Optional;

final class ConsultationDialog {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

    static Optional<ConsultantProfile> show(Window owner, ConsultantProfile current) {
        var dialog = new Dialog<ConsultantProfile>(); dialog.initOwner(owner); dialog.setTitle("Sua apresentação");
        var name = new TextField(current.name()); name.setId("consultantName"); name.setPromptText("Como prefere ser chamado?");
        var date = new TextField(current.birthDate() == null ? "" : current.birthDate().format(DATE)); date.setId("birthDate"); date.setPromptText("dd/mm/aaaa");
        var time = new TextField(current.birthTime() == null ? "" : current.birthTime().format(TIME)); time.setId("birthTime"); time.setPromptText("hh:mm — se souber");
        var city = new TextField(current.birthCity()); city.setId("birthCity"); city.setPromptText("Cidade, estado e país");
        var sign = new ComboBox<ZodiacSign>(); sign.getItems().setAll(ZodiacSign.values()); sign.setValue(current.sign());
        sign.setId("consultantSign"); sign.setPromptText("Não informar"); sign.setMaxWidth(Double.MAX_VALUE);
        var form = new GridPane(); form.setHgap(12); form.setVgap(10);
        form.addRow(0, new Label("Nome ou apelido"), name);
        form.addRow(1, new Label("Data de nascimento"), date);
        form.addRow(2, new Label("Horário de nascimento"), time);
        form.addRow(3, new Label("Local de nascimento"), city);
        form.addRow(4, new Label("Seu signo, se souber"), sign);
        var column = new ColumnConstraints(); column.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(new ColumnConstraints(), column);
        var introduction = new Label("Todas as informações são opcionais. Você também pode continuar sem se identificar."); introduction.setWrapText(true);
        var note = new Label("O signo acrescenta um contexto à leitura. Data, horário e cidade ainda não calculam signo ou ascendente. Os dados só entram no histórico quando você salva a consulta."); note.setWrapText(true);
        var error = new Label(); error.setId("profileError"); error.setWrapText(true); error.setStyle("-fx-text-fill: #a00000;");
        var content = new VBox(12, introduction, form, note, error); content.setPadding(new Insets(8)); content.setPrefWidth(510);
        dialog.getDialogPane().setContent(content);
        var next = new ButtonType("Preparar consulta", ButtonBar.ButtonData.OK_DONE);
        var anonymous = new ButtonType("Continuar sem dados", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().setAll(next, anonymous, ButtonType.CANCEL);
        var result = new java.util.concurrent.atomic.AtomicReference<ConsultantProfile>();
        dialog.getDialogPane().lookupButton(next).setId("profileContinue");
        dialog.getDialogPane().lookupButton(next).addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            try {
                LocalDate birth = date.getText().isBlank() ? null : LocalDate.parse(date.getText().strip(), DATE);
                if (birth != null && birth.isAfter(LocalDate.now(ZoneId.of("America/Sao_Paulo"))))
                    throw new IllegalArgumentException("A data de nascimento não pode estar no futuro.");
                LocalTime hour = time.getText().isBlank() ? null : LocalTime.parse(time.getText().strip(), TIME);
                result.set(new ConsultantProfile(name.getText(), birth, hour, city.getText(), sign.getValue()));
            } catch (DateTimeParseException ex) {
                error.setText("Confira a data (dd/mm/aaaa) e o horário (hh:mm). Deixe em branco o que não souber."); e.consume();
            } catch (IllegalArgumentException ex) { error.setText(ex.getMessage()); e.consume(); }
        });
        dialog.setResultConverter(button -> button == next ? result.get() : button == anonymous ? ConsultantProfile.empty() : null);
        dialog.getDialogPane().getStylesheets().add(ConsultationDialog.class.getResource("/retro.css").toExternalForm());
        return dialog.showAndWait();
    }
}
