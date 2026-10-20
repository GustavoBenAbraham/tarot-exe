package com.tarotexe.engine;

import com.tarotexe.domain.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RandomEngine {
    private final SecureRandom random;
    public RandomEngine() { this(new SecureRandom()); }
    public RandomEngine(SecureRandom random) { this.random = java.util.Objects.requireNonNull(random); }

    public List<DrawnCard> draw(List<TarotCard> deck, SpreadType type, boolean allowReversed) {
        if (deck == null || type == null || deck.size() < type.size()) throw new IllegalArgumentException("Baralho/tiragem inválidos");
        if (deck.stream().map(TarotCard::id).distinct().count() != deck.size()) throw new IllegalArgumentException("Baralho contém cartas duplicadas");
        var shuffled = new ArrayList<>(deck);
        Collections.shuffle(shuffled, random);
        var result = new ArrayList<DrawnCard>();
        for (int i = 0; i < type.size(); i++) result.add(new DrawnCard(shuffled.get(i), allowReversed && random.nextBoolean(), type.position(i)));
        return List.copyOf(result);
    }
}
