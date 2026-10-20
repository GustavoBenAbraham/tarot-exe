package com.tarotexe.service;

import com.tarotexe.domain.*;
import com.tarotexe.engine.RandomEngine;
import java.time.LocalDateTime;

public final class TarotService {
    private final RandomEngine randomEngine;
    private final InterpretationService interpreter;
    public TarotService(RandomEngine randomEngine, InterpretationService interpreter) { this.randomEngine = randomEngine; this.interpreter = interpreter; }
    public Reading draw(String question, SpreadType type) {
        return draw(question, type, true, ReadingTheme.GENERAL);
    }
    public Reading draw(String question, SpreadType type, ConsultantProfile profile) {
        java.util.Objects.requireNonNull(profile, "Perfil obrigatório");
        var reading = draw(question, type);
        return new Reading(reading.question(), reading.type(), reading.cards(), reading.createdAt(), reading.theme(), profile);
    }
    public Reading draw(String question, SpreadType type, boolean allowReversed) {
        return draw(question, type, allowReversed, ReadingTheme.GENERAL);
    }
    public Reading draw(String question, SpreadType type, boolean allowReversed, ReadingTheme theme) {
        String cleanQuestion = question == null ? "" : question.strip();
        if (cleanQuestion.length() > 500) throw new IllegalArgumentException("Pergunta deve ter até 500 caracteres.");
        return new Reading(cleanQuestion, type, randomEngine.draw(TarotDeck.standard(), type, allowReversed), LocalDateTime.now(), theme);
    }
    public String interpret(Reading reading) { return interpreter.interpret(reading); }
}
