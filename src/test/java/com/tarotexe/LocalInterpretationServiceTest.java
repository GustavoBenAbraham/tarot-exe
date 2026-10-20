package com.tarotexe;

import com.tarotexe.domain.*;
import com.tarotexe.service.LocalInterpretationService;
import com.tarotexe.service.LocalCardCatalog;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LocalInterpretationServiceTest {
    @Test void themeChangesApplicationWithoutChangingCardsOrGuessingQuestion() {
        var card = TarotDeck.standard().get(36);
        var cards = List.of(new DrawnCard(card, true, "Orientação"));
        var time = LocalDateTime.now();
        var outputs = new java.util.HashSet<String>();
        for (var theme : ReadingTheme.values()) {
            var reading = new Reading("Trabalho ou amor?", SpreadType.ONE, cards, time, theme);
            String text = new LocalInterpretationService().interpret(reading);
            assertTrue(text.contains("FOCO: " + theme));
            assertTrue(text.contains("Ás de Copas (Invertida)"));
            assertTrue(text.contains("APLICAÇÃO AO TEMA"));
            assertTrue(outputs.add(text));
        }
        var general = new Reading("Trabalho", SpreadType.ONE, cards, time);
        assertEquals(ReadingTheme.GENERAL, general.theme());
        assertTrue(new LocalInterpretationService().interpret(general).contains("FOCO: Geral"));
    }

    @Test void repeatedGuidanceIsPrintedOnlyOnce() {
        var card = TarotDeck.standard().get(0);
        var reading = new Reading("", SpreadType.ONE,
                List.of(new DrawnCard(card, true, "Orientação")), LocalDateTime.now());
        String text = new LocalInterpretationService().interpret(reading);
        String action = new LocalCardCatalog().detail(0).action();
        assertEquals(text.indexOf(action), text.lastIndexOf(action));
        assertEquals(text.indexOf("Sobre as invertidas:"), text.lastIndexOf("Sobre as invertidas:"));
    }

    @Test void allThemesCoverEveryCardGroup() {
        var catalog = new com.tarotexe.service.LocalContextCatalog();
        for (var theme : ReadingTheme.values()) for (int group = 0; group < 5; group++) {
            var context = catalog.context(theme, group);
            assertNotNull(context);
            assertFalse(context.application().isBlank());
            assertTrue(context.question().endsWith("?"));
        }
    }

    @Test void all78CardsHaveDistinctCompleteLocalContentInBothOrientations() {
        var catalog = new LocalCardCatalog();
        var meanings = new java.util.HashSet<String>();
        for (var card : TarotDeck.standard()) {
            var detail = catalog.detail(card.id());
            assertTrue(meanings.add(detail.upright()), "Texto normal repetido: " + card.name());
            assertNotEquals(detail.upright(), detail.reversed());
            for (boolean reversed : new boolean[]{false, true}) {
                var reading = new Reading("", SpreadType.ONE,
                        List.of(new DrawnCard(card, reversed, "Orientação")), LocalDateTime.now());
                String text = new LocalInterpretationService().interpret(reading);
                assertTrue(text.contains(reversed ? detail.reversed() : detail.upright()));
                assertFalse(text.contains(reversed ? detail.upright() : detail.reversed()));
                assertTrue(text.contains(detail.action()));
                assertTrue(text.contains("PRÓXIMO PASSO"));
                assertFalse(text.contains("null"));
            }
        }
    }

    @Test void synthesisLinksActualCardsAndDetectsRepeatedSuitAndReversals() {
        var deck = TarotDeck.standard();
        var reading = new Reading("", SpreadType.THREE, List.of(
                new DrawnCard(deck.get(36), false, "Situação"),
                new DrawnCard(deck.get(37), true, "Desafio"),
                new DrawnCard(deck.get(50), true, "Conselho")), LocalDateTime.now());
        String text = new LocalInterpretationService().interpret(reading);
        String synthesis = text.substring(text.indexOf("SÍNTESE"));
        assertTrue(synthesis.contains("Situação → Desafio: Ás de Copas"));
        assertTrue(synthesis.contains("Desafio → Conselho: Dois de Copas"));
        assertTrue(synthesis.contains("Naipe recorrente: Copas"));
        assertTrue(synthesis.contains("As inversões se repetem"));
        assertFalse(synthesis.contains("predominância de Arcanos Maiores"));
        assertTrue(synthesis.contains(new LocalCardCatalog().detail(50).action()));
    }

    @Test void majorityOfMajorsHasItsOwnSynthesis() {
        var deck = TarotDeck.standard();
        var reading = new Reading("", SpreadType.THREE, List.of(
                new DrawnCard(deck.get(0), false, "Situação"),
                new DrawnCard(deck.get(8), false, "Desafio"),
                new DrawnCard(deck.get(77), false, "Conselho")), LocalDateTime.now());
        String text = new LocalInterpretationService().interpret(reading);
        assertTrue(text.contains("predominância de Arcanos Maiores"));
        assertFalse(text.contains("Naipe recorrente:"));
        assertFalse(text.contains("As inversões se repetem"));
    }

    @Test void reversedCardUsesStudiedMeaningThroughoutReading() {
        var card = new TarotCard(0, "O Louco", "Arcano Maior", "começos e liberdade", "impulsividade");
        var reading = new Reading("", SpreadType.ONE,
                List.of(new DrawnCard(card, true, "Orientação")), LocalDateTime.now());
        String text = new LocalInterpretationService().interpret(reading);
        assertTrue(text.contains("O Louco (Invertida)"));
        assertTrue(text.contains(new LocalCardCatalog().detail(0).reversed()));
        assertFalse(text.contains("Tema: impulsividade."));
        assertFalse(text.contains("começos e liberdade"));
    }

    @Test void releasingReversalsDoNotReceiveContradictoryGenericShadow() {
        var deck = TarotDeck.standard();
        for (int id : new int[]{15, 39, 42, 57}) {
            var reading = new Reading("", SpreadType.ONE,
                    List.of(new DrawnCard(deck.get(id), true, "Orientação")), LocalDateTime.now());
            String text = new LocalInterpretationService().interpret(reading);
            assertTrue(text.contains(new LocalCardCatalog().detail(id).reversed()));
            assertFalse(text.contains(deck.get(id).shadow()));
            assertTrue(text.contains("Rachel Pollack"));
            assertTrue(text.contains(com.tarotexe.service.StudyReference.pages(id)));
        }
    }

    @Test void courtCardsAreNotAssignedToGenderOrSpecificPerson() {
        var deck = TarotDeck.standard();
        var reading = new Reading("", SpreadType.THREE, List.of(
                new DrawnCard(deck.get(48), false, "Situação"),
                new DrawnCard(deck.get(60), true, "Desafio"),
                new DrawnCard(deck.get(77), false, "Conselho")), LocalDateTime.now());
        assertTrue(new LocalInterpretationService().interpret(reading).contains("não definem gênero, idade ou identidade"));
    }

    @Test void referenceCoversAllCardsAndRejectsUnknownIds() {
        for (int id = 0; id < 78; id++)
            assertTrue(com.tarotexe.service.StudyReference.pages(id).matches("\\d+–\\d+"));
        assertEquals("20–25", com.tarotexe.service.StudyReference.pages(0));
        assertEquals("107–110", com.tarotexe.service.StudyReference.pages(21));
        assertEquals("126–140", com.tarotexe.service.StudyReference.pages(42));
        assertThrows(IllegalArgumentException.class, () -> com.tarotexe.service.StudyReference.pages(78));
    }
}
