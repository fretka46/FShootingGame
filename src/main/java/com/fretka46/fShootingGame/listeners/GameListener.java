package com.fretka46.fShootingGame.listeners;

import com.fretka46.fShootingGame.Engine;
import com.fretka46.fShootingGame.Storage.DatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Zombie) {
            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                int newScore = Engine.addScore(killer.getUniqueId(), 1);
                // Update persistent max score
                if (DatabaseManager.Connection != null) {
                    DatabaseManager.updateScore(killer.getUniqueId(), newScore);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Engine.PLAYER_SCORES.remove(event.getPlayer().getUniqueId());
    }
}

