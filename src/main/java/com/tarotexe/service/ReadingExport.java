package com.tarotexe.service;

import com.tarotexe.domain.Reading;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;

public final class ReadingExport {
    private ReadingExport() {}
    public static String text(Reading reading, String local) {
        String header = "TAROT.EXE\n"
                + reading.createdAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + " · " + reading.type() + " · " + reading.theme() + "\n\n";
        return header + local + "\n";
    }
    public static void write(Path file, String text) throws IOException {
        Path absolute = file.toAbsolutePath();
        Path temp = Files.createTempFile(absolute.getParent(), ".tarot-export-", ".tmp");
        try {
            Files.writeString(temp, text, StandardCharsets.UTF_8);
            try { Files.move(temp, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
}
