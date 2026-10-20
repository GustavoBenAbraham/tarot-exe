package com.tarotexe.domain;

/** Editorial prompts, not astronomical calculations or personality diagnoses. */
public enum ZodiacSign {
    ARIES("Áries", "iniciativa", "Como tomar a iniciativa sem atropelar o ritmo da situação?"),
    TAURUS("Touro", "continuidade", "O que merece ser preservado e onde a estabilidade virou apego?"),
    GEMINI("Gêmeos", "comunicação", "Que conversa ou informação pode ampliar sua perspectiva?"),
    CANCER("Câncer", "acolhimento", "Como cuidar do vínculo sem deixar suas necessidades de lado?"),
    LEO("Leão", "expressão", "Como expressar sua vontade sem depender apenas de reconhecimento?"),
    VIRGO("Virgem", "discernimento", "Que ajuste concreto ajuda mais do que exigir perfeição?"),
    LIBRA("Libra", "reciprocidade", "Qual acordo respeita as necessidades de todas as pessoas envolvidas?"),
    SCORPIO("Escorpião", "transformação", "O que pede profundidade e o que precisa deixar de ser controlado?"),
    SAGITTARIUS("Sagitário", "exploração", "Qual possibilidade amplia seu caminho sem perder contato com os fatos?"),
    CAPRICORN("Capricórnio", "responsabilidade", "Que compromisso vale sustentar e qual carga precisa ser dividida?"),
    AQUARIUS("Aquário", "autonomia", "Como experimentar outra perspectiva sem se afastar da relação com os outros?"),
    PISCES("Peixes", "sensibilidade", "Como acolher a imaginação e dar forma concreta ao que sente?");

    private final String label;
    private final String focus;
    private final String question;
    ZodiacSign(String label, String focus, String question) { this.label = label; this.focus = focus; this.question = question; }
    public String focus() { return focus; }
    public String question() { return question; }
    /** Conventional tropical date ranges; approximate near solar ingresses. */
    public static ZodiacSign fromBirthDate(java.time.LocalDate date) {
        java.util.Objects.requireNonNull(date, "Data obrigatória");
        int day = date.getDayOfMonth();
        return switch (date.getMonthValue()) {
            case 1 -> day < 20 ? CAPRICORN : AQUARIUS;
            case 2 -> day < 19 ? AQUARIUS : PISCES;
            case 3 -> day < 21 ? PISCES : ARIES;
            case 4 -> day < 20 ? ARIES : TAURUS;
            case 5 -> day < 21 ? TAURUS : GEMINI;
            case 6 -> day < 21 ? GEMINI : CANCER;
            case 7 -> day < 23 ? CANCER : LEO;
            case 8 -> day < 23 ? LEO : VIRGO;
            case 9 -> day < 23 ? VIRGO : LIBRA;
            case 10 -> day < 23 ? LIBRA : SCORPIO;
            case 11 -> day < 22 ? SCORPIO : SAGITTARIUS;
            case 12 -> day < 22 ? SAGITTARIUS : CAPRICORN;
            default -> throw new IllegalArgumentException("Mês inválido");
        };
    }
    @Override public String toString() { return label; }
}
