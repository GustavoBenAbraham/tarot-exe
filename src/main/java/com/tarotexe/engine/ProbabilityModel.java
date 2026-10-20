package com.tarotexe.engine;

import com.tarotexe.domain.Reading;
import java.math.BigInteger;
import java.util.Locale;

/** Exact counts for a uniform draw without replacement and independent fair orientations. */
public final class ProbabilityModel {
    private ProbabilityModel() {}

    public static BigInteger combinations(int n, int k) {
        if (n < 0 || k < 0 || k > n) throw new IllegalArgumentException("Invalid combination");
        BigInteger value = BigInteger.ONE;
        for (int i = 1; i <= Math.min(k, n - k); i++)
            value = value.multiply(BigInteger.valueOf(n - i + 1)).divide(BigInteger.valueOf(i));
        return value;
    }

    public static BigInteger orderedOutcomes(int deckSize, int count, boolean reversed) {
        if (deckSize < 0 || count < 0 || count > deckSize) throw new IllegalArgumentException("Invalid draw");
        BigInteger value = BigInteger.ONE;
        for (int i = 0; i < count; i++) value = value.multiply(BigInteger.valueOf(deckSize - i));
        return reversed ? value.shiftLeft(count) : value;
    }

    public static double reversedCountProbability(int count, int reversedCount, boolean enabled) {
        if (count < 0 || reversedCount < 0 || reversedCount > count) throw new IllegalArgumentException("Invalid count");
        if (!enabled) return reversedCount == 0 ? 1 : 0;
        return Math.scalb(combinations(count, reversedCount).doubleValue(), -count);
    }

    public static double categoryCountProbability(int deckSize, int categorySize, int count, int matches) {
        if (deckSize < 0 || categorySize < 0 || categorySize > deckSize || count < 0 || count > deckSize)
            throw new IllegalArgumentException("Invalid population");
        if (matches < 0 || matches > count || matches > categorySize || count - matches > deckSize - categorySize) return 0;
        return combinations(categorySize, matches).multiply(combinations(deckSize - categorySize, count - matches)).doubleValue()
                / combinations(deckSize, count).doubleValue();
    }

    private static String percent(double value) { return String.format(Locale.forLanguageTag("pt-BR"), "%.6f%%", value * 100); }

    public static String describe(Reading reading, boolean reversed) {
        int n = reading.cards().size();
        var text = new StringBuilder("MODELO DO SORTEIO\n\n")
                .append("78 cartas; embaralhamento local com SecureRandom; sem reposi\u00e7\u00e3o dentro da tiragem.\n")
                .append("Cada nova tiragem usa o baralho completo. Pergunta e tema n\u00e3o alteram as chances.\n\n")
                .append("Uma carta espec\u00edfica em uma posi\u00e7\u00e3o: 1/78 = ").append(percent(1.0 / 78)).append("\n")
                .append("Uma carta espec\u00edfica em qualquer posi\u00e7\u00e3o: ").append(n).append("/78 = ").append(percent(n / 78.0)).append("\n")
                .append("Conjuntos de cartas (sem ordem nem orienta\u00e7\u00e3o): C(78, ").append(n).append(") = ").append(combinations(78, n)).append("\n")
                .append("Resultados com posi\u00e7\u00f5es e orienta\u00e7\u00f5es: ").append(orderedOutcomes(78, n, reversed)).append("\n")
                .append("Probabilidade desta sequ\u00eancia exata, incluindo orienta\u00e7\u00f5es: 1/").append(orderedOutcomes(78, n, reversed)).append("\n\n")
                .append(reversed ? "Orienta\u00e7\u00e3o independente por carta: 50% normal e 50% invertida.\n" : "Invertidas desativadas: 100% normais.\n")
                .append("QUANTIDADE DE INVERTIDAS (binomial)\n");
        for (int k = 0; k <= n; k++) text.append(k).append(": ").append(percent(reversedCountProbability(n, k, reversed))).append("\n");
        text.append("\nQUANTIDADE DE ARCANOS MAIORES (hipergeom\u00e9trica; 22 de 78)\n");
        for (int k = 0; k <= n; k++) text.append(k).append(": ").append(percent(categoryCountProbability(78, 22, n, k))).append("\n");
        long observed = reading.cards().stream().filter(c -> c.reversed()).count();
        text.append("\nNesta tiragem: ").append(observed).append(" invertida(s).\n")
                .append("As probabilidades descrevem o sorteio; n\u00e3o medem a chance de acontecimentos da vida.\n");
        return text.toString();
    }
}
