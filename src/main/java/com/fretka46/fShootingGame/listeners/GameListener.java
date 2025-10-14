package com.fretka46.fShootingGame.listeners;

import com.fretka46.fShootingGame.Engine;
import com.fretka46.fShootingGame.FShootingGame;
import com.fretka46.fShootingGame.Storage.DatabaseManager;
import com.fretka46.fShootingGame.ZombieSpawner;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.persistence.PersistentDataType;
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
            // Check if this zombie has our plugin's persistent tag
            NamespacedKey key = new NamespacedKey(FShootingGame.getPlugin(FShootingGame.class), "fsg_zombie");
            if (!event.getEntity().getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
                return; // not our zombie
            }

            // Disable drops
            event.getDrops().clear();
            event.setDroppedExp(0);

            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                // Play sound
                killer.playSound(killer.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.7f);


                // Update score
                Engine.PLAYER_SCORES.putIfAbsent(killer.getUniqueId(), 0);
                int newScore = Engine.PLAYER_SCORES.get(killer.getUniqueId()) + 1;
                Engine.PLAYER_SCORES.put(killer.getUniqueId(), newScore);
            }


            // Remove from active zombies list
            int spawnIndex = Engine.spawnPoints.indexOf(event.getEntity().getLocation());
            ZombieSpawner.activeZombies.remove(Integer.valueOf(spawnIndex));

            Engine.scheduleRespawn();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Engine.PLAYER_SCORES.remove(event.getPlayer().getUniqueId());
    }
}
