package com.tarotexe.domain;

import java.time.LocalDateTime;
import java.util.List;

public record Reading(String question, SpreadType type, List<DrawnCard> cards, LocalDateTime createdAt, ReadingTheme theme, ConsultantProfile profile) {
    public Reading { cards = List.copyOf(cards); java.util.Objects.requireNonNull(theme, "Tema obrigatório"); java.util.Objects.requireNonNull(profile, "Perfil obrigatório"); }
    public Reading(String question, SpreadType type, List<DrawnCard> cards, LocalDateTime createdAt, ReadingTheme theme) {
        this(question, type, cards, createdAt, theme, ConsultantProfile.empty());
    }
    public Reading(String question, SpreadType type, List<DrawnCard> cards, LocalDateTime createdAt) {
        this(question, type, cards, createdAt, ReadingTheme.GENERAL);
    }
}
