package com.fretka46.fShootingGame.Messages;

import com.fretka46.fShootingGame.FShootingGame;

public class Log {

    public static void info(String message) {
        FShootingGame.getPlugin(FShootingGame.class).getLogger().info("[FShootingGame] " + message);
    }

    public static void debug(String message) {
        if (FShootingGame.getPlugin(FShootingGame.class).getConfig().getBoolean("debug")) {
            FShootingGame.getPlugin(FShootingGame.class).getLogger().info("[FShootingGame] [DEBUG] " + message);
        }
    }

    public static void severe(String message) {
        FShootingGame.getPlugin(FShootingGame.class).getLogger().severe("[FShootingGame] " + message);
    }
}
