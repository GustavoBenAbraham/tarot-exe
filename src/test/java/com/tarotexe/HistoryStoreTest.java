package com.tarotexe;

import com.tarotexe.domain.*;
import com.tarotexe.service.HistoryStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class HistoryStoreTest {
    @Test void readsLegacyHistoryWithoutChangingOriginalFile() throws Exception {
        var store = new HistoryStore(temp);
        var saved = store.save(reading(LocalDateTime.now()), "Texto local", true);
        var file = temp.resolve(saved.id() + ".properties");
        String legacy = Files.readString(file) + "\naiText=Texto antigo\naiModel=modelo-antigo\n";
        Files.writeString(file, legacy);
        assertEquals(saved, store.list().entries().getFirst());
        assertEquals("Texto local", store.list().entries().getFirst().fullText());
        assertEquals(legacy, Files.readString(file));
        assertFalse(Files.readString(temp.resolve(store.save(saved.reading(), "Nova", true).id() + ".properties")).contains("aiText"));
    }
    @TempDir Path temp;
    private Reading reading(LocalDateTime time) {
        var deck = TarotDeck.standard();
        return new Reading("Relações, ação e coração?\nSegunda linha = teste | ç", SpreadType.THREE,
                List.of(new DrawnCard(deck.get(8), true, "Situação"),
                        new DrawnCard(deck.get(36), false, "Desafio"),
                        new DrawnCard(deck.get(77), false, "Conselho")), time, ReadingTheme.RELATIONSHIPS);
    }

    @Test void savesAndReloadsCompleteSnapshotAfterNewStoreInstance() throws Exception {
        var folder = temp.resolve("history");
        var reading = reading(LocalDateTime.of(2026, 10, 19, 12, 30));
        var saved = new HistoryStore(folder).save(reading, "Texto original: reflexão\nAção = cuidado | sim", true);
        var listing = new HistoryStore(folder).list();
        assertTrue(listing.problems().isEmpty());
        assertEquals(List.of(saved), listing.entries());
        try (var paths = Files.list(folder)) { assertEquals(1, paths.count()); }
    }

    @Test void sortsNewestFirstAndDeletesOnlyChosenEntry() throws Exception {
        var store = new HistoryStore(temp);
        var older = store.save(reading(LocalDateTime.of(2026, 1, 1, 0, 0)), "Antiga", true);
        var newer = store.save(reading(LocalDateTime.of(2026, 10, 19, 0, 0)), "Nova", true);
        assertEquals(List.of(newer, older), store.list().entries());
        store.delete(newer.id());
        assertEquals(List.of(older), new HistoryStore(temp).list().entries());
    }

    @Test void missingHistoryIsEmptyWithoutCreatingDirectory() throws Exception {
        var path = temp.resolve("not-created");
        assertTrue(new HistoryStore(path).list().entries().isEmpty());
        assertFalse(Files.exists(path));
    }

    @Test void corruptFilesAreReportedAndPreservedWhileValidEntriesRemainReadable() throws Exception {
        var store = new HistoryStore(temp);
        var entry = store.save(reading(LocalDateTime.now()), "Texto", true);
        var source = temp.resolve(entry.id() + ".properties");
        for (String alteration : List.of("version=999", "createdAt=invalid", "card.0.id=100", "card.1.id=8", "card.0.reversed=maybe")) {
            String original = Files.readString(source);
            String key = alteration.substring(0, alteration.indexOf('='));
            String changed = original.replaceAll("(?m)^" + java.util.regex.Pattern.quote(key) + "=.*$", alteration);
            Files.writeString(source, changed);
            var listing = store.list();
            assertTrue(listing.entries().isEmpty(), alteration);
            assertEquals(1, listing.problems().size(), alteration);
            assertEquals(changed, Files.readString(source));
            Files.writeString(source, original);
        }
        Files.writeString(temp.resolve("broken.properties"), "broken");
        assertEquals(List.of(entry), store.list().entries());
        assertEquals(1, store.list().problems().size());
    }

    @Test void unavailableStorageReturnsFailureWithoutDestroyingExistingFile() throws Exception {
        var file = temp.resolve("not-a-directory"); Files.writeString(file, "preservar");
        var store = new HistoryStore(file);
        assertThrows(java.io.IOException.class, () -> store.save(reading(LocalDateTime.now()), "Texto", true));
        assertThrows(java.io.IOException.class, store::list);
        assertEquals("preservar", Files.readString(file));
    }
}
