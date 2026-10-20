package com.tarotexe;

import com.tarotexe.service.AiSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class AiSettingsTest {
    @TempDir Path folder;
    @Test void defaultsAndSavedSettingsSurviveNewLoad() throws Exception {
        var path = folder.resolve("preferences/ai.properties");
        assertEquals(AiSettings.defaults(), AiSettings.load(path));
        var settings = new AiSettings("qwen2.5:3b", 120, 384, 2048, 0.2);
        settings.save(path); assertEquals(settings, AiSettings.load(path));
    }
    @Test void rejectsInvalidModelAndParameters() {
        assertThrows(IllegalArgumentException.class, () -> new AiSettings("", 60, 256, 2048, 0.2));
        assertThrows(IllegalArgumentException.class, () -> new AiSettings("qwen:cloud", 60, 256, 2048, 0.2));
        assertThrows(IllegalArgumentException.class, () -> new AiSettings("qwen2.5:3b", 0, 256, 2048, 0.2));
        assertThrows(IllegalArgumentException.class, () -> new AiSettings("qwen2.5:3b", 60, 0, 2048, 0.2));
        assertThrows(IllegalArgumentException.class, () -> new AiSettings("qwen2.5:3b", 60, 256, 2048, Double.NaN));
    }
    @Test void invalidFileIsReportedAndNotOverwritten() throws Exception {
        var path = folder.resolve("ai.properties"); Files.writeString(path, "broken");
        assertThrows(java.io.IOException.class, () -> AiSettings.load(path));
        assertEquals("broken", Files.readString(path));
    }
}
