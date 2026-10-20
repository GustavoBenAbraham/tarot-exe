package com.tarotexe.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Catálogo autoral local; cada ID possui interpretação normal, invertida e ação. */
public final class LocalCardCatalog {
    public record Detail(String upright, String reversed, String action) {}
    private static final Map<Integer, Detail> DEFAULT = load();
    private final Map<Integer, Detail> details;
    public LocalCardCatalog() { details = DEFAULT; }
    public LocalCardCatalog(Reader reader) throws IOException { details = parse(reader); }

    public Detail detail(int id) {
        var detail = details.get(id);
        if (detail == null) throw new IllegalArgumentException("Carta sem interpretação: " + id);
        return detail;
    }

    private static Map<Integer, Detail> load() {
        try (var stream = LocalCardCatalog.class.getResourceAsStream("/interpretations.properties")) {
            if (stream == null) throw new IllegalStateException("Catálogo local ausente");
            return parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) { throw new UncheckedIOException("Não foi possível ler o catálogo", e); }
    }
    private static Map<Integer, Detail> parse(Reader reader) throws IOException {
            var properties = StrictProperties.read(reader);
            if (properties.size() != 78) throw new IllegalStateException("Catálogo deve conter 78 cartas");
            var result = new HashMap<Integer, Detail>();
            for (int id = 0; id < 78; id++) {
                String value = properties.getProperty(Integer.toString(id), "");
                String[] fields = value.split("\\|", -1);
                if (fields.length != 3 || Arrays.stream(fields).anyMatch(String::isBlank))
                    throw new IllegalStateException("Interpretação incompleta: " + id);
                result.put(id, new Detail(fields[0].strip(), fields[1].strip(), fields[2].strip()));
            }
            return Map.copyOf(result);
    }
}
