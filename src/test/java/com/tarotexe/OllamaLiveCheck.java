package com.tarotexe;

import com.tarotexe.domain.*;
import com.tarotexe.service.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** Executar explicitamente: mvnw.cmd -Dtest=OllamaLiveCheck test. Requer Ollama e modelo baixado. */
class OllamaLiveCheck {
    @Test void generateWithInstalledModelUsingApplicationClient() throws Exception {
        var deck = TarotDeck.standard();
        var reading = new Reading("Como posso organizar melhor meu projeto pessoal?", SpreadType.THREE,
                List.of(new DrawnCard(deck.get(1), false, "Situação"), new DrawnCard(deck.get(31), true, "Desafio"),
                        new DrawnCard(deck.get(71), false, "Conselho")), LocalDateTime.now(), ReadingTheme.WORK);
        long start = System.nanoTime();
        String answer = new OllamaService().deepen(reading, AiSettings.defaults()).get(200, TimeUnit.SECONDS);
        double seconds = (System.nanoTime() - start) / 1e9;
        assertFalse(answer.isBlank()); assertFalse(answer.contains("<think>"));
        Files.writeString(Path.of("target/ollama-app-test.md"), "# Teste do cliente Java\n\nModelo: qwen2.5:3b\nTempo: "
                + Math.round(seconds) + " segundos\n\n" + answer);
        System.out.println("Ollama local: resposta completa em " + Math.round(seconds) + " segundos; target/ollama-app-test.md");
    }
}
