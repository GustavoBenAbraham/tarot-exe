package com.tarotexe;

import com.tarotexe.domain.SpreadType;
import com.tarotexe.engine.RandomEngine;
import com.tarotexe.service.LocalInterpretationService;
import com.tarotexe.service.TarotService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TarotServiceTest {
    @Test void zodiacDateRangesCoverAllTransitionsAndLeapDay() {
        var signs = com.tarotexe.domain.ZodiacSign.values();
        int[][] starts = {{3,21}, {4,20}, {5,21}, {6,21}, {7,23}, {8,23},
                {9,23}, {10,23}, {11,22}, {12,22}, {1,20}, {2,19}};
        for (int i = 0; i < signs.length; i++) {
            var date = java.time.LocalDate.of(2000, starts[i][0], starts[i][1]);
            assertEquals(signs[i], com.tarotexe.domain.ZodiacSign.fromBirthDate(date));
            assertEquals(signs[(i + 11) % 12], com.tarotexe.domain.ZodiacSign.fromBirthDate(date.minusDays(1)));
        }
        assertEquals(com.tarotexe.domain.ZodiacSign.PISCES,
                com.tarotexe.domain.ZodiacSign.fromBirthDate(java.time.LocalDate.of(2000, 2, 29)));
        assertEquals(com.tarotexe.domain.ZodiacSign.CAPRICORN,
                com.tarotexe.domain.ZodiacSign.fromBirthDate(java.time.LocalDate.of(2000, 12, 31)));
    }
    @Test void profilePersonalizesReadingWithoutChangingRandomDraw() {
        var profile = new com.tarotexe.domain.ConsultantProfile("Ana", java.time.LocalDate.of(1994, 7, 12),
                java.time.LocalTime.of(9, 30), "São Paulo, Brasil", com.tarotexe.domain.ZodiacSign.CANCER);
        java.util.function.Supplier<TarotService> deterministic = () -> new TarotService(new RandomEngine(new java.security.SecureRandom() {
            @Override public int nextInt(int bound) { return 0; }
            @Override public boolean nextBoolean() { return true; }
        }), new LocalInterpretationService());
        var anonymous = deterministic.get().draw("Como seguir?", SpreadType.THREE);
        var personal = deterministic.get().draw("Como seguir?", SpreadType.THREE, profile);
        assertEquals(anonymous.cards(), personal.cards());
        String text = deterministic.get().interpret(personal);
        assertTrue(text.contains("Consulta de Ana"));
        assertTrue(text.contains("Câncer"));
        assertTrue(text.contains(profile.sign().question()));
        assertFalse(deterministic.get().interpret(anonymous).contains("CONTEXTO ASTROLÓGICO"));
    }

    @Test void optionalProfileValidatesBoundsAndTimeRequiresDate() {
        assertThrows(IllegalArgumentException.class, () -> new com.tarotexe.domain.ConsultantProfile("a".repeat(81), null, null, "", null));
        assertThrows(IllegalArgumentException.class, () -> new com.tarotexe.domain.ConsultantProfile("", null, java.time.LocalTime.NOON, "", null));
        assertEquals("Ana", new com.tarotexe.domain.ConsultantProfile(" Ana ", null, null, "", null).name());
    }
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
