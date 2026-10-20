package com.tarotexe.domain;

import java.time.LocalDateTime;
import java.util.List;

public record Reading(String question, SpreadType type, List<DrawnCard> cards, LocalDateTime createdAt, ReadingTheme theme) {
    public Reading { cards = List.copyOf(cards); java.util.Objects.requireNonNull(theme, "Tema obrigatório"); }
    public Reading(String question, SpreadType type, List<DrawnCard> cards, LocalDateTime createdAt) {
        this(question, type, cards, createdAt, ReadingTheme.GENERAL);
    }
}
