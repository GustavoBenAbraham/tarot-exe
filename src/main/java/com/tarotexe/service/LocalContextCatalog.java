package com.tarotexe.service;

import com.tarotexe.domain.ReadingTheme;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class LocalContextCatalog {
    public record Context(String application, String question) {}
    private static final Map<String, Context> CONTENT = load();

    public Context context(ReadingTheme theme, int group) { return CONTENT.get(theme.name() + "." + group); }

    private static Map<String, Context> load() {
        try (var stream = LocalContextCatalog.class.getResourceAsStream("/contexts.properties")) {
            if (stream == null) throw new IllegalStateException("Contextos locais ausentes");
            var properties = StrictProperties.read(new InputStreamReader(stream, StandardCharsets.UTF_8));
            var result = new HashMap<String, Context>();
            for (var theme : ReadingTheme.values()) for (int group = 0; group < 5; group++) {
                String key = theme.name() + "." + group;
                String[] fields = properties.getProperty(key, "").split("\\|", -1);
                if (fields.length != 2 || Arrays.stream(fields).anyMatch(String::isBlank))
                    throw new IllegalStateException("Contexto incompleto: " + key);
                result.put(key, new Context(fields[0].strip(), fields[1].strip()));
            }
            if (properties.size() != result.size()) throw new IllegalStateException("Contextos desconhecidos no catálogo");
            return Map.copyOf(result);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}
