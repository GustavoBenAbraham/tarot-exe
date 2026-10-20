package com.tarotexe;

public final class Launcher {
    public static void main(String[] args) {
        AppLog.initialize();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> AppLog.showFailure(error));
        try { TarotApp.main(args); }
        catch (Throwable error) { AppLog.showFailure(error); System.exit(1); }
    }
}
