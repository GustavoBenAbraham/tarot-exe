package com.tarotexe;

import com.tarotexe.engine.ProbabilityModel;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;

class ProbabilityModelTest {
    @Test void exactOutcomeCounts() {
        assertEquals(BigInteger.valueOf(76076), ProbabilityModel.combinations(78, 3));
        assertEquals(BigInteger.valueOf(456456), ProbabilityModel.orderedOutcomes(78, 3, false));
        assertEquals(BigInteger.valueOf(3651648), ProbabilityModel.orderedOutcomes(78, 3, true));
        assertEquals(BigInteger.valueOf(156), ProbabilityModel.orderedOutcomes(78, 1, true));
        assertEquals(BigInteger.ONE, ProbabilityModel.combinations(78, 0));
    }

    @Test void orientationsFollowBinomialAndCanBeDisabled() {
        double[] expected = {0.125, 0.375, 0.375, 0.125};
        for (int k = 0; k <= 3; k++) {
            assertEquals(expected[k], ProbabilityModel.reversedCountProbability(3, k, true));
            assertEquals(k == 0 ? 1 : 0, ProbabilityModel.reversedCountProbability(3, k, false));
        }
    }

    @Test void categoryDistributionIsNormalizedAndHasCorrectExpectation() {
        double total = 0, expectation = 0;
        for (int k = 0; k <= 3; k++) {
            double p = ProbabilityModel.categoryCountProbability(78, 22, 3, k);
            total += p; expectation += k * p;
        }
        assertEquals(1, total, 1e-12);
        assertEquals(3 * 22.0 / 78, expectation, 1e-12);
        assertEquals(22.0 / 78, ProbabilityModel.categoryCountProbability(78, 22, 1, 1), 1e-12);
        assertEquals(0, ProbabilityModel.categoryCountProbability(78, 0, 3, 1));
        assertEquals(1, ProbabilityModel.categoryCountProbability(78, 78, 3, 3));
    }

    @Test void invalidPopulationsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ProbabilityModel.combinations(2, 3));
        assertThrows(IllegalArgumentException.class, () -> ProbabilityModel.orderedOutcomes(2, 3, true));
        assertThrows(IllegalArgumentException.class, () -> ProbabilityModel.categoryCountProbability(78, 79, 3, 1));
        assertThrows(IllegalArgumentException.class, () -> ProbabilityModel.reversedCountProbability(3, 4, true));
    }
}
