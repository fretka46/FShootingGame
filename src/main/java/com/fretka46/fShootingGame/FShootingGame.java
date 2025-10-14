package com.fretka46.fShootingGame;

import com.fretka46.fShootingGame.Commands.RunGame;
import com.fretka46.fShootingGame.Storage.DatabaseManager;
import com.fretka46.fShootingGame.listeners.GameListener;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class FShootingGame extends JavaPlugin {

    // Reusable NamespacedKey for marking our spawned zombies
    public static NamespacedKey ZOMBIE_KEY;

    @Override
    public void onEnable() {
        // Ensure default config exists
        saveDefaultConfig();

        // Initialize shared NamespacedKey
        ZOMBIE_KEY = new NamespacedKey(this, "fsg_zombie");

        // Connect database
        try {
            DatabaseManager.Connection = DatabaseManager.connect();
        } catch (Exception e) {
            getLogger().severe("Failed to connect to database: " + e.getMessage());
        }

        // Register event listeners
        Bukkit.getPluginManager().registerEvents(new GameListener(), this);

        // Register PlaceholderAPI expansion if present
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PapiExpansion(this).register();
        }

        // Register command to start the game (Paper's BasicCommand)
        registerCommand("fsg-start", new RunGame());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
