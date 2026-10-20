package com.tarotexe.domain;

public enum ReadingTheme {
    GENERAL("Geral"), RELATIONSHIPS("Relacionamentos"), WORK("Trabalho e projetos"),
    SELF_KNOWLEDGE("Autoconhecimento");

    private final String label;
    ReadingTheme(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
