package com.tarotexe.domain;

public record TarotCard(int id, String name, String category, String meaning, String shadow) {
    public TarotCard {
        if (id < 0 || id > 77 || name == null || name.isBlank()) throw new IllegalArgumentException("Carta inválida");
    }
}
