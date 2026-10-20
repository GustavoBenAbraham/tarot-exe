package com.tarotexe.service;

import com.tarotexe.domain.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/** Um arquivo por leitura: escrita temporária e publicação por rename. */
public final class HistoryStore {
    public record Entry(UUID id, Reading reading, String interpretation, boolean allowReversed) {
        public String fullText() { return interpretation; }
    }
    public record Listing(List<Entry> entries, List<String> problems) {}
    private final Path directory;

    public HistoryStore(Path directory) { this.directory = directory.toAbsolutePath(); }
    public static HistoryStore local() {
        return new HistoryStore(Path.of(System.getProperty("user.home"), ".tarot-exe", "history"));
    }
    public Path directory() { return directory; }

    public Entry save(Reading reading, String interpretation, boolean allowReversed) throws IOException {
        Objects.requireNonNull(interpretation);
        UUID id = UUID.randomUUID();
        var entry = new Entry(id, reading, interpretation, allowReversed);
        var data = new Properties();
        data.setProperty("version", "1"); data.setProperty("id", id.toString());
        data.setProperty("question", reading.question());
        data.setProperty("spread", reading.type().name()); data.setProperty("theme", reading.theme().name());
        data.setProperty("createdAt", reading.createdAt().toString());
        data.setProperty("interpretation", interpretation);
        data.setProperty("allowReversed", Boolean.toString(allowReversed));
        for (int i = 0; i < reading.cards().size(); i++) {
            data.setProperty("card." + i + ".id", Integer.toString(reading.cards().get(i).card().id()));
            data.setProperty("card." + i + ".reversed", Boolean.toString(reading.cards().get(i).reversed()));
        }
        // Validate before publishing, including callers outside the UI.
        decode(data, id);
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, ".saving-", ".tmp");
        try {
            try (var writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                data.store(writer, "Tarot.exe - histórico local v1");
            }
            try { Files.move(temporary, file(id), StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, file(id)); }
        } finally { Files.deleteIfExists(temporary); }
        return entry;
    }

    public Listing list() throws IOException {
        if (Files.notExists(directory)) return new Listing(List.of(), List.of());
        var entries = new ArrayList<Entry>(); var problems = new ArrayList<String>();
        try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(p -> p.getFileName().toString().endsWith(".properties")).sorted().toList()) {
                try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    String filename = path.getFileName().toString();
                    var id = UUID.fromString(filename.substring(0, filename.length() - ".properties".length()));
                    var data = new Properties(); data.load(reader);
                    entries.add(decode(data, id));
                } catch (IOException | IllegalArgumentException | java.time.DateTimeException e) {
                    problems.add(path.getFileName() + ": arquivo não pôde ser lido; original preservado.");
                }
            }
        }
        entries.sort(Comparator.comparing((Entry e) -> e.reading().createdAt()).reversed().thenComparing(Entry::id));
        return new Listing(List.copyOf(entries), List.copyOf(problems));
    }

    public void delete(UUID id) throws IOException { Files.deleteIfExists(file(Objects.requireNonNull(id))); }
    private Path file(UUID id) { return directory.resolve(id + ".properties"); }

    private Entry decode(Properties p, UUID expectedId) {
        if (!required(p, "version").equals("1")) throw new IllegalArgumentException("Versão desconhecida");
        var id = UUID.fromString(required(p, "id"));
        if (!id.equals(expectedId)) throw new IllegalArgumentException("Identificador inconsistente");
        var spread = SpreadType.valueOf(required(p, "spread"));
        var theme = ReadingTheme.valueOf(required(p, "theme"));
        var createdAt = LocalDateTime.parse(required(p, "createdAt"));
        String question = required(p, "question");
        if (question.length() > 500) throw new IllegalArgumentException("Pergunta muito longa");
        boolean allowReversed = bool(p, "allowReversed");
        var deck = TarotDeck.standard(); var cards = new ArrayList<DrawnCard>(); var ids = new HashSet<Integer>();
        for (int i = 0; i < spread.size(); i++) {
            int cardId = Integer.parseInt(required(p, "card." + i + ".id"));
            if (cardId < 0 || cardId >= deck.size() || !ids.add(cardId)) throw new IllegalArgumentException("Carta inválida");
            boolean reversed = bool(p, "card." + i + ".reversed");
            if (reversed && !allowReversed) throw new IllegalArgumentException("Inversão inconsistente");
            cards.add(new DrawnCard(deck.get(cardId), reversed, spread.position(i)));
        }
        return new Entry(id, new Reading(question, spread, cards, createdAt, theme), required(p, "interpretation"), allowReversed);
    }
    private String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null) throw new IllegalArgumentException("Campo ausente: " + key);
        return value;
    }
    private boolean bool(Properties p, String key) {
        String value = required(p, key);
        if (!value.equals("true") && !value.equals("false")) throw new IllegalArgumentException("Booleano inválido");
        return Boolean.parseBoolean(value);
    }
}
