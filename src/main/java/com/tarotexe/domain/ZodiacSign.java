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
    @Override public String toString() { return label; }
}
