package com.fretka46.fShootingGame;

import com.fretka46.fShootingGame.Messages.Log;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import java.util.List;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class ZombieSpawner {
    /**
     * List of numbers of array from locations, where zombies are currently spawned
     */
    public static List<Integer> activeZombies = new ArrayList<>();

    public static void spawnZombie(int currentMaxScore) {
        if (Engine.spawnPoints.isEmpty()) return; // No spawn points configured

        // Choose a random spawn point that is not currently occupied
        Location location;
        int attempts = 0;
        do {
            int index = (int) (Math.random() * Engine.spawnPoints.size());
            location = Engine.spawnPoints.get(index);
            attempts++;
            if (attempts > 20) {
                Log.severe("Failed to find unoccupied spawn point for zombie.");
                return; // Avoid infinite loop if all points are occupied
            }
        } while (activeZombies.contains(Engine.spawnPoints.indexOf(location)));

        activeZombies.add(Engine.spawnPoints.indexOf(location));


        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        if (!(entity instanceof Zombie)) return;

        // Disable AI
        entity.setAI(false);

        // Set health to 1
        entity.setHealth(1.0);

        // Mark this entity so we can identify it later in event handlers
        entity.getPersistentDataContainer().set(FShootingGame.ZOMBIE_KEY, PersistentDataType.BYTE, (byte)1);

        // Compute despawn time based on current max score (more score = faster spawns)
        var config = FShootingGame.getPlugin(FShootingGame.class).getConfig();
        int baseDespawnTicks = config.getInt("baseDespawnTicks", 100);
        int minDespawnTicks = config.getInt("minDespawnTicks", 30);
        int despawnTicks = baseDespawnTicks - (currentMaxScore * config.getInt("despawnMultiplier", 30));
        if (despawnTicks < minDespawnTicks) despawnTicks = minDespawnTicks;

        Log.info("Spawned zombie with despawn time " + (despawnTicks / 20.0) + " seconds.");

        // Schedule despawn if not killed in time
        Bukkit.getScheduler().runTaskLater(FShootingGame.getPlugin(FShootingGame.class), () -> {
            if (!entity.isDead() && entity.isValid()) {
                // Remove from active zombies list
                int spawnIndex = Engine.spawnPoints.indexOf(entity.getLocation());
                ZombieSpawner.activeZombies.remove(Integer.valueOf(spawnIndex));
                entity.remove();
                Engine.scheduleRespawn();
            }
        }, Math.max(1, despawnTicks));
    }
}
