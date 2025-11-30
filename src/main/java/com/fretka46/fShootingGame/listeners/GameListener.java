package com.fretka46.fShootingGame.listeners;

import com.fretka46.fShootingGame.Engine;
import com.fretka46.fShootingGame.FShootingGame;
import com.fretka46.fShootingGame.Messages.Log;
import com.fretka46.fShootingGame.ZombieSpawner;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Zombie) {
            // Check if this zombie has our plugin's persistent tag
            NamespacedKey key = new NamespacedKey(FShootingGame.getPlugin(FShootingGame.class), "fsg_zombie");
            NamespacedKey premiumKey = new NamespacedKey(FShootingGame.getPlugin(FShootingGame.class), "fsg_premium_zombie");

            if (!event.getEntity().getPersistentDataContainer().has(key, PersistentDataType.BYTE) &&
                    !event.getEntity().getPersistentDataContainer().has(premiumKey, PersistentDataType.BYTE)) {
                return;
            }

            var config = FShootingGame.getPlugin(FShootingGame.class).getConfig();

            boolean isAllowedDamage = false;
            Player killer = null;

            if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
                var damager = damageByEntityEvent.getDamager();

                // Check if damager is a projectile and get the shooter
                if (damager instanceof Projectile projectile) {
                    if (projectile.getShooter() instanceof Player) {
                        killer = (Player) projectile.getShooter();
                    }

                    // Check if this type of projectile is allowed
                    switch (damager) {
                        case org.bukkit.entity.Arrow arrow when config.getBoolean("allowArrow") -> isAllowedDamage = true;
                        case org.bukkit.entity.Snowball snowball when config.getBoolean("allowSnowball") -> isAllowedDamage = true;
                        case org.bukkit.entity.Egg egg when config.getBoolean("allowEggs") -> isAllowedDamage = true;
                        default -> {
                        }
                    }
                }
            }

            if (!isAllowedDamage) {
                event.setCancelled(true);
                return;
            }

            boolean isPremium = event.getEntity().getPersistentDataContainer().has(premiumKey, PersistentDataType.BYTE);

            if (killer != null) {
                // Play sound
                if (isPremium) {
                    killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                } else
                    killer.playSound(killer.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.7f);

                // Update score
                int newScore;
                Engine.PLAYER_SCORES.putIfAbsent(killer.getUniqueId(), 0);
                if (isPremium)
                    newScore = Engine.PLAYER_SCORES.get(killer.getUniqueId()) + 5;
                else
                    newScore = Engine.PLAYER_SCORES.get(killer.getUniqueId()) + 1;
                Engine.PLAYER_SCORES.put(killer.getUniqueId(), newScore);
            }

            // Silently remove the zombie
            event.setCancelled(true);
            event.getEntity().remove();


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
