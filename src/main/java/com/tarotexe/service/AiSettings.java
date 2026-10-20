package com.tarotexe.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

public record AiSettings(String model, int timeoutSeconds, int responseTokens, int contextTokens, double temperature) {
    public AiSettings {
        if (model == null || !model.matches("[a-zA-Z0-9][a-zA-Z0-9._/:+-]{0,127}") || model.toLowerCase(java.util.Locale.ROOT).contains("cloud"))
            throw new IllegalArgumentException("Informe o nome de um modelo local baixado no Ollama.");
        if (timeoutSeconds < 5 || timeoutSeconds > 300 || responseTokens < 128 || responseTokens > 1024
                || contextTokens < 2048 || contextTokens > 8192 || !Double.isFinite(temperature) || temperature < 0 || temperature > 1)
            throw new IllegalArgumentException("Parâmetros de IA fora do intervalo permitido.");
    }
    public static AiSettings defaults() { return new AiSettings("qwen2.5:3b", 180, 512, 4096, 0.3); }
    public static Path localPath() { return Path.of(System.getProperty("user.home"), ".tarot-exe", "ai.properties"); }
    public static AiSettings load(Path path) throws IOException {
        if (Files.notExists(path)) return defaults();
        var p = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { p.load(reader); }
        try {
            return new AiSettings(p.getProperty("model"), Integer.parseInt(p.getProperty("timeoutSeconds")),
                    Integer.parseInt(p.getProperty("responseTokens")), Integer.parseInt(p.getProperty("contextTokens")),
                    Double.parseDouble(p.getProperty("temperature")));
        } catch (IllegalArgumentException | NullPointerException e) { throw new IOException("Configuração de IA inválida", e); }
    }
    public void save(Path path) throws IOException {
        var p = new Properties(); p.setProperty("model", model); p.setProperty("timeoutSeconds", "" + timeoutSeconds);
        p.setProperty("responseTokens", "" + responseTokens); p.setProperty("contextTokens", "" + contextTokens);
        p.setProperty("temperature", "" + temperature);
        Path parent = path.toAbsolutePath().getParent(); Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, ".ai-settings-", ".tmp");
        try {
            try (var writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) { p.store(writer, "Tarot.exe - Ollama local"); }
            try { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
}
