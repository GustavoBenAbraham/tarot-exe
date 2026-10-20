package com.tarotexe;

import com.tarotexe.domain.*;
import com.tarotexe.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReleaseRefinementTest {
    @TempDir Path folder;
    private String catalogText() throws IOException {
        try (var stream = getClass().getResourceAsStream("/interpretations.properties")) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    @Test void rejectsDuplicateMissingEmptyAndExtraCatalogFields() throws Exception {
        String source = catalogText();
        for (String invalid : List.of(source + "\n0=duplicado|duplicado|duplicado\n",
                source.replaceAll("(?m)^77=.*$", ""),
                source.replaceAll("(?m)^0=.*$", "0=normal||prática"),
                source.replaceAll("(?m)^0=.*$", "0=normal|invertida|prática|extra"))) {
            assertThrows(RuntimeException.class, () -> new LocalCardCatalog(new StringReader(invalid)));
        }
        assertFalse(new LocalCardCatalog(new StringReader(source)).detail(0).action().isBlank());
    }
    @Test void exportKeepsAccentsAndBothSourcesAndReplacesPreviousFile() throws Exception {
        var reading = new Reading("Reflexão?", SpreadType.ONE,
                List.of(new DrawnCard(TarotDeck.standard().getFirst(), false, "Orientação")),
                LocalDateTime.of(2026, 10, 20, 10, 15), ReadingTheme.GENERAL);
        var text = ReadingExport.text(reading, "Pergunta: Reflexão?\nTexto local.", "Ação com IA.", "qwen2.5:3b");
        var file = folder.resolve("leitura.txt"); Files.writeString(file, "versão anterior");
        ReadingExport.write(file, text);
        assertEquals(text, Files.readString(file, StandardCharsets.UTF_8));
        assertTrue(text.contains("20/10/2026 10:15"));
        assertTrue(text.contains("Texto local.")); assertTrue(text.contains("Ação com IA."));
        assertTrue(text.contains("qwen2.5:3b"));
        assertFalse(ReadingExport.text(reading, "Local", "", "").contains("APROFUNDAMENTO"));
        try (var entries = Files.list(folder)) { assertEquals(1, entries.count()); }
    }
    @Test void windowPreferencesRoundTripAndInvalidFilesArePreserved() throws Exception {
        var file = folder.resolve("window.properties");
        assertEquals(UiPreferences.defaults(), UiPreferences.load(file));
        var settings = new UiPreferences(1280, 800, true, 0.55, 18);
        settings.save(file); assertEquals(settings, UiPreferences.load(file));
        Files.writeString(file, "width=NaN");
        assertThrows(IOException.class, () -> UiPreferences.load(file));
        assertEquals("width=NaN", Files.readString(file));
        assertThrows(IllegalArgumentException.class, () -> new UiPreferences(1280, 800, false, Double.NaN, 15));
    }
}
