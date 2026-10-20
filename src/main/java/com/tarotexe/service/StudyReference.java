package com.tarotexe.service;

import com.tarotexe.domain.Reading;

/** Page numbers refer to the supplied 251-page PDF, not to a print edition. */
public final class StudyReference {
    private StudyReference() {}
    private static final int[] START = {20, 25, 29, 36, 39, 43, 47, 51, 57, 62, 66, 72, 77, 79, 83, 87, 92, 95, 98, 101, 104, 107};

    public static String pages(int id) {
        if (id < 0 || id > 77) throw new IllegalArgumentException("Carta inválida");
        if (id < 22) {
            // Include the next page: some concluding paragraphs share it with the next heading.
            int end = id == 21 ? 110 : START[id + 1];
            return START[id] + "–" + end;
        }
        return switch ((id - 22) / 14) {
            case 0 -> "110–125";
            case 1 -> "126–140";
            case 2 -> "140–154";
            default -> "154–173";
        };
    }

    public static String describe(Reading reading) {
        String pages = reading.cards().stream().map(c -> pages(c.card().id())).distinct()
                .collect(java.util.stream.Collectors.joining(", "));
        return "Referência de estudo: Rachel Pollack, 78 Graus de Sabedoria. Páginas do PDF: " + pages
                + ". Sínteses adaptadas para o aplicativo; não são transcrições. As práticas e a tiragem Situação/Desafio/Conselho são adaptações locais.";
    }
}
