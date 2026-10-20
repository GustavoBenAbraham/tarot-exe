package com.tarotexe.domain;

import java.util.ArrayList;
import java.util.List;

public final class TarotDeck {
    private TarotDeck() {}
    private static final String[] MAJOR = {
        "O Louco", "O Mago", "A Sacerdotisa", "A Imperatriz", "O Imperador", "O Hierofante",
        "Os Enamorados", "O Carro", "A Força", "O Eremita", "A Roda da Fortuna", "A Justiça",
        "O Enforcado", "A Morte", "A Temperança", "O Diabo", "A Torre", "A Estrela",
        "A Lua", "O Sol", "O Julgamento", "O Mundo"
    };
    private static final String[] LIGHT = {
        "começos e liberdade", "iniciativa e habilidade", "intuição e silêncio", "criatividade e abundância",
        "estrutura e responsabilidade", "tradição e aprendizado", "escolhas e vínculos", "direção e conquista",
        "coragem e autocontrole", "introspecção e busca", "mudanças e ciclos", "equilíbrio e justiça",
        "pausa e nova perspectiva", "transformação e encerramentos", "moderação e harmonia",
        "apegos e desejos", "rupturas e revelações", "esperança e inspiração", "incerteza e imaginação",
        "clareza e vitalidade", "renovação e avaliação", "realização e integração"
    };
    private static final String[] SHADOW = {
        "impulsividade ou receio de começar", "dispersão ou manipulação", "segredos ou desconexão interior", "bloqueio criativo ou excesso",
        "rigidez ou falta de direção", "dogmatismo ou rebeldia", "indecisão ou desalinhamento", "pressa ou falta de controle",
        "insegurança ou força mal direcionada", "isolamento ou fuga", "resistência à mudança", "injustiça ou desequilíbrio",
        "estagnação ou sacrifício excessivo", "resistência a encerrar ciclos", "desequilíbrio ou exageros",
        "dependência ou perda de autonomia", "medo de mudanças inevitáveis", "desânimo ou falta de fé em si", "confusão ou ansiedade",
        "excesso de confiança ou desânimo", "autocrítica ou medo de recomeçar", "falta de conclusão ou fechamento"
    };
    private static final String[] RANKS = {"Ás", "Dois", "Três", "Quatro", "Cinco", "Seis", "Sete", "Oito", "Nove", "Dez", "Pajem", "Cavaleiro", "Rainha", "Rei"};
    private static final String[] SUITS = {"Paus", "Copas", "Espadas", "Ouros"};
    private static final String[] THEMES = {"ação, energia e criatividade", "emoções, relações e intuição", "pensamentos, conflitos e decisões", "recursos, trabalho e estabilidade"};
    private static final String[] RANK_LIGHT = {"um novo potencial", "uma escolha ou parceria", "crescimento e colaboração", "fundamentos e organização", "um desafio a superar", "ajuste e troca", "reavaliação e persistência", "movimento e dedicação", "maturidade e resultados", "fechamento de um ciclo", "curiosidade e aprendizado", "movimento e iniciativa", "cuidado e domínio emocional", "liderança e domínio"};
    private static final String[] RANK_SHADOW = {"oportunidade negligenciada", "indecisão ou falta de equilíbrio", "dificuldade de cooperar", "rigidez ou instabilidade", "tensão ou frustração", "trocas desequilibradas", "dúvidas e desgaste", "pressa ou bloqueio", "medo de perder resultados", "sobrecarga ou dificuldade de concluir", "imaturidade ou distração", "impulsividade ou estagnação", "dificuldade de acolher ou expressar", "controle excessivo ou falta de liderança"};

    public static List<TarotCard> standard() {
        List<TarotCard> cards = new ArrayList<>(78);
        for (int i = 0; i < MAJOR.length; i++) cards.add(new TarotCard(i, MAJOR[i], "Arcano Maior", LIGHT[i], SHADOW[i]));
        for (int suit = 0; suit < SUITS.length; suit++) {
            for (int rank = 0; rank < RANKS.length; rank++) {
                int id = 22 + suit * RANKS.length + rank;
                cards.add(new TarotCard(id, RANKS[rank] + " de " + SUITS[suit], "Arcano Menor · " + SUITS[suit], RANK_LIGHT[rank] + " em temas de " + THEMES[suit], RANK_SHADOW[rank] + " em temas de " + THEMES[suit]));
            }
        }
        return List.copyOf(cards);
    }
}
