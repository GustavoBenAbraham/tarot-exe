package com.tarotexe;

import com.tarotexe.domain.SpreadType;
import com.tarotexe.engine.RandomEngine;
import com.tarotexe.service.LocalInterpretationService;
import com.tarotexe.service.TarotService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TarotServiceTest {
    private final TarotService service = new TarotService(new RandomEngine(), new LocalInterpretationService());

    @Test void defaultDrawAlwaysAllowsReversedCardsAndUsesGeneralTheme() {
        var random = new java.security.SecureRandom() {
            @Override public boolean nextBoolean() { return true; }
        };
        var tarot = new TarotService(new RandomEngine(random), new LocalInterpretationService());
        for (var type : SpreadType.values()) {
            var reading = tarot.draw("", type);
            assertEquals(type.size(), reading.cards().size());
            assertTrue(reading.cards().stream().allMatch(com.tarotexe.domain.DrawnCard::reversed));
            assertEquals(com.tarotexe.domain.ReadingTheme.GENERAL, reading.theme());
        }
    }

    @Test void nullQuestionBecomesEmpty() {
        assertEquals("", service.draw(null, SpreadType.ONE, false).question());
    }

    @Test void questionIsTrimmed() {
        assertEquals("Minha pergunta", service.draw("  Minha pergunta  ", SpreadType.ONE, false).question());
    }

    @Test void acceptsQuestionAtLimit() {
        assertEquals(500, service.draw("a".repeat(500), SpreadType.ONE, false).question().length());
    }

    @Test void rejectsQuestionOverLimit() {
        assertThrows(IllegalArgumentException.class,
                () -> service.draw("a".repeat(501), SpreadType.ONE, false));
    }

    @Test void rejectsMissingSpread() {
        assertThrows(IllegalArgumentException.class, () -> service.draw("", null, false));
    }

    @Test void singleCardReadingContainsOrientationAndInterpretation() {
        var reading = service.draw("Como seguir?", SpreadType.ONE, false);
        assertEquals(1, reading.cards().size());
        assertEquals("Orientação", reading.cards().getFirst().position());
        assertFalse(reading.cards().getFirst().reversed());
        String text = service.interpret(reading);
        assertTrue(text.contains("Pergunta: Como seguir?"));
        assertTrue(text.contains(reading.cards().getFirst().card().name()));
        assertTrue(text.contains(new com.tarotexe.service.LocalCardCatalog().detail(reading.cards().getFirst().card().id()).upright()));
        assertTrue(text.contains("SÍNTESE"));
    }

    @Test void threeCardInterpretationContainsEachPositionAndCard() {
        var reading = service.draw("", SpreadType.THREE, true);
        String text = service.interpret(reading);
        assertFalse(text.contains("Pergunta:"));
        for (var drawn : reading.cards()) {
            assertTrue(text.contains(drawn.position()));
            assertTrue(text.contains(drawn.card().name()));
            var detail = new com.tarotexe.service.LocalCardCatalog().detail(drawn.card().id());
            assertTrue(text.contains(drawn.reversed() ? detail.reversed() : detail.upright()));
        }
    }

    @Test void readingCardsCannotBeModified() {
        var reading = service.draw("", SpreadType.THREE, false);
        assertThrows(UnsupportedOperationException.class, () -> reading.cards().clear());
    }
}
