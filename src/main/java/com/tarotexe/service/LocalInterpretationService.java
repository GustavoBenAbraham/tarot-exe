package com.tarotexe.service;

import com.tarotexe.domain.*;

public final class LocalInterpretationService implements InterpretationService {
    private final LocalCardCatalog catalog = new LocalCardCatalog();
    private final LocalContextCatalog contexts = new LocalContextCatalog();
    @Override public String interpret(Reading reading) {
        StringBuilder text = new StringBuilder();
        if (!reading.question().isBlank()) text.append("Pergunta: ").append(reading.question()).append("\n\n");
        text.append("LEITURA • ").append(reading.type().label()).append("\n\n");
        text.append("FOCO: ").append(reading.theme()).append("\n\n");
        for (DrawnCard drawn : reading.cards()) {
            text.append(drawn.position()).append(" | ").append(drawn.card().name()).append(" (").append(drawn.orientation()).append(")\n");
            var detail = catalog.detail(drawn.card().id());
            text.append("Aprofundamento: ").append(drawn.reversed() ? detail.reversed() : detail.upright()).append("\n");
            text.append("Nesta posição: ").append(prompt(drawn.position())).append("\n");
            if (drawn != reading.cards().getLast()) text.append("Prática sugerida: ").append(detail.action()).append("\n");
            text.append("\n");
        }
        var advice = reading.cards().getLast();
        var context = contexts.context(reading.theme(), group(advice));
        text.append("APLICAÇÃO AO TEMA\n").append(context.application()).append("\n")
            .append("Para refletir: ").append(context.question()).append("\n\n");
        text.append("SÍNTESE\n");
        if (reading.cards().size() == 1) {
            var card = reading.cards().getFirst();
            text.append(card.card().name()).append(": ").append(theme(card)).append("\n");
            text.append("Pergunta de integração: em que situação concreta este tema aparece e qual resposta está ao seu alcance?\n");
        } else {
            for (int i = 0; i < reading.cards().size() - 1; i++) {
                var from = reading.cards().get(i); var to = reading.cards().get(i + 1);
                text.append(from.position()).append(" → ").append(to.position()).append(": ")
                    .append(from.card().name()).append(": ").append(theme(from))
                    .append(" Em diálogo com ").append(to.card().name()).append(": ").append(theme(to)).append(" ");
                if (group(from) == group(to)) text.append("As duas cartas retomam o mesmo campo de experiência: investigue se o padrão se repete ou pede uma resposta diferente.\n");
                else text.append("Observe como esses dois temas se influenciam antes de escolher uma atitude.\n");
            }
            long majors = reading.cards().stream().filter(c -> c.card().id() < 22).count();
            if (majors >= 2) text.append("Há predominância de Arcanos Maiores: esta leitura destaca valores, transições e escolhas de perspectiva, além das tarefas imediatas.\n");
            for (int suit = 1; suit <= 4; suit++) {
                final int current = suit;
                if (reading.cards().stream().filter(c -> group(c) == current).count() >= 2)
                    text.append("Naipe recorrente: ").append(suitName(suit)).append(". Dê atenção a ")
                        .append(suitTheme(suit)).append(" na relação entre as posições.\n");
            }
            long reversed = reading.cards().stream().filter(DrawnCard::reversed).count();
            if (reversed >= 2) text.append("As inversões se repetem: observe em cada carta se há bloqueio, mudança de direção ou liberação de uma qualidade. Não são um placar de acontecimentos negativos.\n");
            if (reading.cards().stream().anyMatch(c -> c.card().id() >= 22 && (c.card().id() - 22) % 14 >= 10))
                text.append("As figuras da corte podem representar uma atitude sua, uma forma de agir no vínculo ou uma influência de outra pessoa; não definem gênero, idade ou identidade de alguém.\n");
            text.append("Pergunta de integração: como a situação e o desafio ajudam a escolher uma aplicação concreta do conselho?\n");
        }
        if (reading.cards().stream().anyMatch(DrawnCard::reversed))
            text.append("\nSobre as invertidas: a orientação pode bloquear, redirecionar ou liberar a qualidade da carta. Compare as possibilidades com sua experiência e com as outras posições.\n");
        text.append("\nPRÓXIMO PASSO\n").append(catalog.detail(advice.card().id()).action())
            .append(" Depois, registre o que observou e o que deseja ajustar.\n\n")
            .append(StudyReference.describe(reading));
        return text.toString();
    }
    private int group(DrawnCard card) { return card.card().id() < 22 ? 0 : 1 + (card.card().id() - 22) / 14; }
    private String theme(DrawnCard card) {
        var detail = catalog.detail(card.card().id());
        String meaning = card.reversed() ? detail.reversed() : detail.upright();
        int end = meaning.indexOf('.');
        return end < 0 ? meaning : meaning.substring(0, end + 1);
    }
    private String suitName(int suit) { return switch (suit) { case 1 -> "Paus"; case 2 -> "Copas"; case 3 -> "Espadas"; default -> "Ouros"; }; }
    private String suitTheme(int suit) { return switch (suit) {
        case 1 -> "iniciativa, energia e continuidade";
        case 2 -> "sentimentos, reciprocidade e vínculos";
        case 3 -> "pensamentos, comunicação e decisões";
        default -> "rotina, recursos e sustentação prática";
    }; }
    private String prompt(String pos) {
        return switch (pos) {
            case "Situação" -> "Reflita sobre como este tema se manifesta no momento presente.";
            case "Desafio" -> "Pergunte-se qual padrão ou obstáculo merece atenção.";
            case "Conselho" -> "Considere uma ação realista e coerente com seus valores.";
            default -> "Use a carta como convite a uma reflexão pessoal.";
        };
    }
}
