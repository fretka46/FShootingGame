package com.fretka46.fShootingGame;

import com.fretka46.fShootingGame.Messages.Log;
import com.fretka46.fShootingGame.Messages.Translations;
import com.fretka46.fShootingGame.Storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Engine {

    // Global score storage for this session, keyed by player UUID
    public static final Map<UUID, Integer> PLAYER_SCORES = new ConcurrentHashMap<>();
    public static Boolean isRunning = false;
    public static List<Location> spawnPoints = new ArrayList<>();

    public static void startGame() {
        Log.info("Starting game");

        // Get all spawning points from config
        FileConfiguration config = FShootingGame.getPlugin(FShootingGame.class).getConfig();
        if (config.isList("spawnPoints")) {
            spawnPoints.clear();

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

        if (spawnPoints.isEmpty()) {
            Log.severe("Cannot start game: No valid spawn points configured.");
            return;
        }

        isRunning = true;
        PLAYER_SCORES.clear();

        Bukkit.getScheduler().runTaskLater(FShootingGame.getPlugin(FShootingGame.class), () -> {

            for (int i = 1; i <= 3 ; i++) {
                // Spawn initial zombies
                Bukkit.getScheduler().runTaskLater(FShootingGame.getPlugin(FShootingGame.class), () -> {
                    ZombieSpawner.spawnZombie(0);
                }, Math.max(1, 20 * i));
            }

            // Schedule game stop
            Bukkit.getScheduler().runTaskLater(FShootingGame.getPlugin(FShootingGame.class), () -> {
                isRunning = false;

                // Save scores to database
                // foreach
                PLAYER_SCORES.forEach((uuid, score) -> {
                    var player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        if (player.isOnline()) {
                            var tagResolver = net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("score", String.valueOf(score));
                            Translations.send(Bukkit.getPlayer(uuid), "gameover_message", tagResolver);
                        }

                        DatabaseManager.updateScore(uuid, score);
                        // execute commands from config
                        if (config.isList("rewardCommands")) {
                            for (Object cmdObj : config.getList("rewardCommands")) {
                                if (cmdObj instanceof String cmd) {
                                    String command = cmd.replace("{player}", player.getName())
                                            .replace("{uuid}", uuid.toString())
                                            .replace("{multiplier_score}", String.valueOf(score * config.getDouble("rewardMultiplier", 1)))
                                            .replace("{score}", String.valueOf(score));
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                                }
                            }
                        }
                        else
                            Log.severe("Reward commands are not properly configured.");
                    }
                });
            }, Math.max(1, 20 * config.getInt("gameLength", 20)));

        }, Math.max(1, 20 * 5));
    }

    public static void scheduleRespawn() {
        if (!isRunning) return;

        // Determine current max score
        int currentMaxScore = PLAYER_SCORES.values().stream().max(Integer::compareTo).orElse(0);

        // Schedule next spawn based on max score
        int baseSpawnTicks = 100; // 5 seconds
        int minSpawnTicks = 10; // 0.5 seconds
        int spawnTicks = baseSpawnTicks - currentMaxScore;
        if (spawnTicks < minSpawnTicks) spawnTicks = minSpawnTicks;

       float timeBeforeSpawn = Math.round((spawnTicks / 20f) * 100f) / 100f;

        Log.debug("Scheduling next zombie spawn in " + timeBeforeSpawn + " s (max score: " + currentMaxScore + ")");

        Bukkit.getScheduler().runTaskLater(FShootingGame.getPlugin(FShootingGame.class), () -> {
            if (!isRunning) return;

            ZombieSpawner.spawnZombie(currentMaxScore);
        }, Math.max(1, spawnTicks));
    }


}
