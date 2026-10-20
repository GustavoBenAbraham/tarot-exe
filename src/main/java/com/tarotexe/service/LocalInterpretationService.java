package com.tarotexe.service;

import com.tarotexe.domain.*;

public final class LocalInterpretationService implements InterpretationService {
    private final LocalCardCatalog catalog = new LocalCardCatalog();
    private final LocalContextCatalog contexts = new LocalContextCatalog();
    @Override public String interpret(Reading reading) {
        StringBuilder text = new StringBuilder();
        if (!reading.question().isBlank()) text.append("Pergunta: ").append(reading.question()).append("\n\n");
        text.append("LEITURA SIMBÓLICA • ").append(reading.type().label()).append("\n\n");
        text.append("FOCO: ").append(reading.theme()).append("\n\n");
        for (DrawnCard drawn : reading.cards()) {
            text.append(drawn.position()).append(" | ").append(drawn.card().name()).append(" (").append(drawn.orientation()).append(")\n");
            text.append("Tema: ").append(drawn.reversed() ? drawn.card().shadow() : drawn.card().meaning()).append(".\n");
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
            text.append(card.card().name()).append(" concentra esta leitura em ").append(theme(card)).append(".\n");
            text.append("Pergunta de integração: em que situação concreta este tema aparece e qual resposta está ao seu alcance?\n");
        } else {
            for (int i = 0; i < reading.cards().size() - 1; i++) {
                var from = reading.cards().get(i); var to = reading.cards().get(i + 1);
                text.append(from.position()).append(" → ").append(to.position()).append(": ")
                    .append(from.card().name()).append(" coloca em foco ").append(theme(from))
                    .append("; ").append(to.card().name()).append(" convida a examinar ").append(theme(to)).append(". ");
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
            if (reversed >= 2) text.append("As inversões se repetem: reserve espaço para revisar ritmo, limites e pressupostos, sem transformar isso em previsão de dificuldades.\n");
            text.append("Pergunta de integração: como a situação e o desafio ajudam a escolher uma aplicação concreta do conselho?\n");
        }
        if (reading.cards().stream().anyMatch(DrawnCard::reversed))
            text.append("\nSobre as invertidas: considere excesso, bloqueio ou elaboração interna do tema, sem assumir um resultado negativo.\n");
        text.append("\nPRÓXIMO PASSO\n").append(catalog.detail(advice.card().id()).action())
            .append(" Depois, registre o que observou e o que deseja ajustar.\n\n")
            .append("Leitura simbólica e reflexiva, composta por textos e regras locais. A pergunta orienta sua reflexão; o aplicativo não interpreta livremente seu conteúdo nem prevê acontecimentos.");
        return text.toString();
    }
    private int group(DrawnCard card) { return card.card().id() < 22 ? 0 : 1 + (card.card().id() - 22) / 14; }
    private String theme(DrawnCard card) { return card.reversed() ? card.card().shadow() : card.card().meaning(); }
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
