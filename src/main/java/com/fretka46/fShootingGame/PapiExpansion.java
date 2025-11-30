package com.fretka46.fShootingGame;

import com.fretka46.fShootingGame.Storage.DatabaseManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PapiExpansion extends PlaceholderExpansion {

    private final FShootingGame plugin;

    public PapiExpansion(FShootingGame plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors()); //
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "fsg";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion(); //
    }

    @Override
    public boolean persist() {
        return true; //
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // Top player placeholders: %fsg_top_1_name%, %fsg_top_1_score%, ...
        if (params.matches("top_\\d+_(name|score)")) {
            String[] parts = params.split("_");
            int idx;
            try {
                idx = Integer.parseInt(parts[1]) - 1; // 1-based to 0-based
            } catch (NumberFormatException e) {
                return "";
            }
            var topPlayers = DatabaseManager.getTopPlayers();
            if (idx >= 0 && idx < topPlayers.size()) {
                if ("name".equals(parts[2])) {
                    return topPlayers.get(idx)[0] != null ? topPlayers.get(idx)[0] : "Nikdo";
                } else if ("score".equals(parts[2])) {
                    return topPlayers.get(idx)[1] != null ? topPlayers.get(idx)[1] : "Nic";
                }
            } else {
                return "";
            }
        }
        return null; //
    }
}