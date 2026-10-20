package com.tarotexe.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

public record UiPreferences(double width, double height, boolean maximized, double divider, int fontSize) {
    public UiPreferences {
        if (!Double.isFinite(width) || !Double.isFinite(height) || !Double.isFinite(divider)
                || width < 720 || width > 10000 || height < 600 || height > 10000
                || divider < 0.2 || divider > 0.8 || fontSize < 12 || fontSize > 22)
            throw new IllegalArgumentException("Preferências de janela inválidas");
    }
    public static UiPreferences defaults() { return new UiPreferences(1380, 940, false, 0.58, 15); }
    public static Path localPath() { return Path.of(System.getProperty("user.home"), ".tarot-exe", "window.properties"); }
    public static UiPreferences load(Path path) throws IOException {
        if (Files.notExists(path)) return defaults();
        var p = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { p.load(reader); }
        try {
            String max = p.getProperty("maximized");
            if (!"true".equals(max) && !"false".equals(max)) throw new IllegalArgumentException("Booleano inválido");
            return new UiPreferences(Double.parseDouble(p.getProperty("width")), Double.parseDouble(p.getProperty("height")),
                    Boolean.parseBoolean(max), Double.parseDouble(p.getProperty("divider")), Integer.parseInt(p.getProperty("fontSize")));
        } catch (IllegalArgumentException | NullPointerException e) { throw new IOException("Preferências inválidas", e); }
    }
    public void save(Path path) throws IOException {
        var p = new Properties(); p.setProperty("width", "" + width); p.setProperty("height", "" + height);
        p.setProperty("maximized", "" + maximized); p.setProperty("divider", "" + divider); p.setProperty("fontSize", "" + fontSize);
        Path absolute = path.toAbsolutePath(); Files.createDirectories(absolute.getParent());
        Path temporary = Files.createTempFile(absolute.getParent(), ".window-", ".tmp");
        try {
            try (var writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) { p.store(writer, "Tarot.exe - janela"); }
            try { Files.move(temporary, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
}
