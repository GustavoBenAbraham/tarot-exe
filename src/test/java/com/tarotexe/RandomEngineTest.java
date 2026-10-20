package com.tarotexe;

import com.tarotexe.domain.*;
import com.tarotexe.engine.RandomEngine;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RandomEngineTest {
    private final RandomEngine engine = new RandomEngine();
    @Test void rejectsNullDeck() {
        assertThrows(IllegalArgumentException.class, () -> engine.draw(null, SpreadType.ONE, false));
    }
    @Test void rejectsInsufficientDeck() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.draw(TarotDeck.standard().subList(0, 2), SpreadType.THREE, false));
    }
    @Test void rejectsDuplicateCards() {
        var card = TarotDeck.standard().getFirst();
        assertThrows(IllegalArgumentException.class,
                () -> engine.draw(java.util.List.of(card, card, card), SpreadType.THREE, false));
    }
    @Test void standardDeckHas78UniqueCards() {
        var cards = TarotDeck.standard();
        assertEquals(78, cards.size());
        assertEquals(78, cards.stream().map(TarotCard::id).distinct().count());
    }
    @Test void threeCardReadingHasUniqueCardsAndCorrectPositions() {
        for (int i = 0; i < 100; i++) {
            var drawn = engine.draw(TarotDeck.standard(), SpreadType.THREE, true);
            assertEquals(3, drawn.size());
            assertEquals(3, drawn.stream().map(d -> d.card().id()).distinct().count());
            assertEquals("Situação", drawn.get(0).position());
            assertEquals("Desafio", drawn.get(1).position());
            assertEquals("Conselho", drawn.get(2).position());
        }
    }
    @Test void reversedCanBeDisabled() {
        for (int i = 0; i < 30; i++)
            assertTrue(engine.draw(TarotDeck.standard(), SpreadType.THREE, false).stream().noneMatch(DrawnCard::reversed));
    }
    @Test void originalDeckIsNotModified() {
        var deck = TarotDeck.standard();
        var first = deck.get(0);
        engine.draw(deck, SpreadType.THREE, true);
        assertEquals(first, deck.get(0));
    }
}
