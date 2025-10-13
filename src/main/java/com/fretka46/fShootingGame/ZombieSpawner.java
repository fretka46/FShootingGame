package com.fretka46.fShootingGame;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;

/**
 * Utility for spawning zombies and handling timed despawn if not killed.
 */
public class ZombieSpawner {

    /**
     * Spawns a zombie at the given location and schedules a despawn if it's still alive after despawnTicks.
     *
     * @param location     Where to spawn
     * @param despawnTicks How long (in ticks) before removing the zombie if it's not killed
     */
    public static void spawnZombie(Location location, int despawnTicks) {
        if (location == null || location.getWorld() == null) return;

        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        if (!(entity instanceof Zombie)) return; // Safety, though EntityType.ZOMBIE should be a Zombie

        // Optional: tweak zombie attributes here if desired
        // Example: entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.25);

        // Schedule despawn if not killed in time
        Bukkit.getScheduler().runTaskLater(FShootingGame.getPlugin(FShootingGame.class), () -> {
            if (!entity.isDead() && entity.isValid()) {
                entity.remove();
            }
        }, Math.max(1, despawnTicks));
    }
}

