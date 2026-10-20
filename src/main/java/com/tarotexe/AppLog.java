package com.tarotexe;

import java.nio.file.*;
import java.util.logging.*;

final class AppLog {
    private static final Logger LOG = Logger.getLogger("com.tarotexe");
    static { LOG.setUseParentHandlers(false); }
    static void initialize() {
        try {
            var directory = Path.of(System.getProperty("user.home"), ".tarot-exe", "logs"); Files.createDirectories(directory);
            var handler = new FileHandler(directory.resolve("tarot-%g.log").toString(), 262144, 3, true);
            handler.setEncoding("UTF-8"); handler.setFormatter(new SimpleFormatter());
            LOG.addHandler(handler); LOG.setUseParentHandlers(false);
        } catch (java.io.IOException e) { LOG.log(Level.WARNING, "Não foi possível criar o log", e); }
    }
    static void record(String message, Throwable failure) { LOG.log(Level.WARNING, message, failure); }
    static void showFailure(Throwable error) {
        record("Falha inesperada", error);
        javax.swing.JOptionPane.showMessageDialog(null,
                "Não foi possível concluir a operação.\nSeu histórico permanece no perfil do usuário.\n"
                + "Detalhes em .tarot-exe/logs, na sua pasta de usuário.", "Tarot.exe", javax.swing.JOptionPane.ERROR_MESSAGE);
    }
}
