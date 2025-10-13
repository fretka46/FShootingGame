package com.fretka46.fShootingGame;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Engine {

    // Global score storage for this session, keyed by player UUID
    public static final Map<UUID, Integer> PLAYER_SCORES = new ConcurrentHashMap<>();
    public static Boolean isRunning = false;

    public static void startGame() {
        isRunning = true;

        // Get all spawning points from config
        FileConfiguration config = FShootingGame.getPlugin(FShootingGame.class).getConfig();
        List<Location> spawnPoints = new ArrayList<>();
        if (config.isList("spawnPoints")) {
            for (Object obj : config.getList("spawnPoints")) {
                if (obj instanceof java.util.Map map) {
                    String world = (String) map.get("world");
                    double x = Double.parseDouble(map.get("x").toString());
                    double y = Double.parseDouble(map.get("y").toString());
                    double z = Double.parseDouble(map.get("z").toString());
                    float yaw = map.containsKey("yaw") ? Float.parseFloat(map.get("yaw").toString()) : 0f;
                    float pitch = map.containsKey("pitch") ? Float.parseFloat(map.get("pitch").toString()) : 0f;
                    if (Bukkit.getWorld(world) == null) continue; // skip invalid world
                    Location loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                    spawnPoints.add(loc);
                }
            }
        }

        // Initialize scores for online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            PLAYER_SCORES.putIfAbsent(p.getUniqueId(), 0);
        }

        // Delay start a bit, then start adaptive spawning
        Bukkit.getScheduler().runTaskLater(FShootingGame.getPlugin(FShootingGame.class), () -> new BukkitRunnable() {
            // Use a small tick to allow dynamic interval changes; we accumulate until next spawn
            private int ticksUntilNextSpawn = computeSpawnIntervalTicks(0);

            @Override
            public void run() {
                // Ensure we have entries for any new players
                for (Player p : Bukkit.getOnlinePlayers()) {
                    PLAYER_SCORES.putIfAbsent(p.getUniqueId(), 0);
                }

                // Determine current highest score
                int maxScore = 0;
                for (int sc : PLAYER_SCORES.values()) {
                    if (sc > maxScore) maxScore = sc;
                }

                // Compute current target interval and despawn time from maxScore
                int targetInterval = computeSpawnIntervalTicks(maxScore);
                int despawnTicks = computeDespawnTicks(maxScore);

                // Count down and spawn when due
                ticksUntilNextSpawn -= 5; // this task runs every 5 ticks
                if (ticksUntilNextSpawn <= 0) {
                    for (Location spawnPoint : spawnPoints) {
                        ZombieSpawner.spawnZombie(spawnPoint, despawnTicks);
                    }
                    ticksUntilNextSpawn = targetInterval;
                }
            }
        }.runTaskTimer(FShootingGame.getPlugin(FShootingGame.class), 0L, 5L), 20L * 5); // Start after 5 seconds
    }

    public static int addScore(UUID playerId, int delta) {
        return PLAYER_SCORES.merge(playerId, delta, Integer::sum);
    }

    // Spawn interval in ticks; gets faster (smaller) with higher score
    private static int computeSpawnIntervalTicks(int maxScore) {
        int base = 100; // 5 seconds
        int min = 20;   // 1 second
        int interval = base - (maxScore * 2); // 2 ticks faster per score point
        return Math.max(min, interval);
    }

    // Despawn time in ticks; shorter with higher score
    private static int computeDespawnTicks(int maxScore) {
        int base = 200; // 10 seconds
        int min = 40;   // 2 seconds
        int ticks = base - (maxScore * 4); // 4 ticks shorter per score point
        return Math.max(min, ticks);
    }
}
