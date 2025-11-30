package com.fretka46.fShootingGame;

import com.fretka46.fShootingGame.Messages.Log;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class ZombieSpawner {
    /**
     * List of numbers of array from locations, where zombies are currently spawned
     */
    public static List<Integer> activeZombies = new ArrayList<>();
    public static Dictionary<Integer, LivingEntity> zombieEntities = new Hashtable<>();

    public static int towardsNextPremium = 0;

    public static void killAllZombies() {
        for (int index : new ArrayList<>(activeZombies)) {
            LivingEntity entity = zombieEntities.get(index);
            if (entity != null && entity.isValid() && !entity.isDead()) {
                entity.remove();
            }
            activeZombies.remove(Integer.valueOf(index));
            zombieEntities.remove(index);
        }
    }

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

        int spawnIndex = Engine.spawnPoints.indexOf(location);
        activeZombies.add(spawnIndex);

        // Get premium
        towardsNextPremium++;
        boolean isPremium = false;
        if (towardsNextPremium >= FShootingGame.getPlugin(FShootingGame.class).getConfig().getInt("premium_spawn_interval", 5)) {
            isPremium = true;
            towardsNextPremium = 0;
        }

        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);

        // Store the entity in the dictionary
        zombieEntities.put(spawnIndex, entity);

        // Make it adult
        Zombie zombie = (Zombie) entity;
        zombie.setAdult();

        // Set silent
        entity.setSilent(true);

        // Apply Model Engine model
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(entity);

        ActiveModel activeModel;
        try {
            if (isPremium)
                activeModel = ModelEngineAPI.createActiveModel(FShootingGame.getPlugin(FShootingGame.class).getConfig().getString("premium_mob_skin"));
            else
                activeModel = ModelEngineAPI.createActiveModel(FShootingGame.getPlugin(FShootingGame.class).getConfig().getString("mob_skin"));
            modeledEntity.addModel(activeModel, true);
        } catch (Exception e) {
            Log.severe("Failed to apply model to zombie, does it exist?\n" + e.getMessage());
            activeModel = null;
        }

        // Male zombie invisible to hide default model
        if (activeModel != null)
            entity.setInvisible(true);

        // Disable AI
        entity.setAI(false);

        // Set health to 1
        entity.setHealth(1.0);

        // Mark this entity so we can identify it later in event handlers
        if (isPremium)
            entity.getPersistentDataContainer().set(FShootingGame.PREMIUM_ZOMBIE_KEY, PersistentDataType.BYTE, (byte)1);
        else
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
                int entitySpawnIndex = Engine.spawnPoints.indexOf(entity.getLocation());
                ZombieSpawner.activeZombies.remove(Integer.valueOf(entitySpawnIndex));
                zombieEntities.remove(entitySpawnIndex);
                entity.remove();
                Engine.scheduleRespawn();
            }
        }, Math.max(1, despawnTicks));
    }
}
