package com.tarotexe.domain;

public record DrawnCard(TarotCard card, boolean reversed, String position) {
    public String orientation() { return reversed ? "Invertida" : "Normal"; }
}
