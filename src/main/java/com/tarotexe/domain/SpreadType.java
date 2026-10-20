package com.tarotexe.domain;

public enum SpreadType {
    ONE("Uma carta", new String[]{"Orientação"}),
    THREE("Três cartas", new String[]{"Situação", "Desafio", "Conselho"});

    private final String label;
    private final String[] positions;
    SpreadType(String label, String[] positions) { this.label = label; this.positions = positions; }
    public String label() { return label; }
    public int size() { return positions.length; }
    public String position(int index) { return positions[index]; }
    @Override public String toString() { return label; }
}
