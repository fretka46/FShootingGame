package com.fretka46.fShootingGame.Storage;

import com.fretka46.fShootingGame.FShootingGame;
import com.fretka46.fShootingGame.Messages.Log;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Dictionary;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    public static Connection Connection;

    public static Connection connect() throws SQLException, ClassNotFoundException {
        // Create directory if not exist
        java.io.File dir = new java.io.File("plugins/FShootingGame");
        if (dir.mkdirs())
            Log.info("Creating database directory: " + dir.getAbsolutePath());

        Class.forName("org.sqlite.JDBC");
        var connection = DriverManager.getConnection("jdbc:sqlite:plugins/FShootingGame/database.db");

        // Create default tables if not exist
        var ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS scores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "uuid TEXT NOT NULL," +
                "date TEXT NOT NULL," +
                "maxScore INTEGER NOT NULL" +
                ");");
        ps.executeUpdate();
        ps.close();

        return connection;
    }

    public static boolean updateScore(UUID uuid, int score) {
        try {
            // Check if user already has a score
            var ps = Connection.prepareStatement("SELECT maxScore FROM scores WHERE uuid = ?;");
            ps.setString(1, uuid.toString());
            var rs = ps.executeQuery();

            if (rs.next()) {
                int currentMaxScore = rs.getInt("maxScore");
                if (score > currentMaxScore) {
                    // Update score
                    ps = Connection.prepareStatement("UPDATE scores SET maxScore = ?, date = ? WHERE uuid = ?;");
                    ps.setInt(1, score);
                    ps.setString(2, LocalDateTime.now().toString());
                    ps.setString(3, uuid.toString());
                    ps.executeUpdate();
                    Log.info("Updated score for " + uuid + " to " + score);

                    rs.close();
                    ps.close();
                    return true;

                } else {
                    Log.debug("Score " + score + " is not higher than current max score " + currentMaxScore + " for " + uuid);
                }
            } else {
                // Insert new score
                ps = Connection.prepareStatement("INSERT INTO scores (uuid, date, maxScore) VALUES (?, ?, ?);");
                ps.setString(1, uuid.toString());
                ps.setString(2, LocalDateTime.now().toString());
                ps.setInt(3, score);
                ps.executeUpdate();
                Log.info("Inserted new score for " + uuid + ": " + score);

                rs.close();
                ps.close();

                return false;
            }


        } catch (SQLException e) {
            Log.severe("Failed to update score for " + uuid + ": " + e.getMessage());
            return false;
        }

        return false;
    }

    public static List<Dictionary<String, Object>> getTopScores() {
        var list = new java.util.ArrayList<Dictionary<String, Object>>();

        try {
            var ps = Connection.prepareStatement("SELECT uuid, maxScore FROM scores ORDER BY maxScore DESC LIMIT 5;");
            var rs = ps.executeQuery();

            while (rs.next()) {
                var dict = new java.util.Hashtable<String, Object>();
                var uuid = UUID.fromString(rs.getString("uuid"));
                var offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                dict.put("name", offlinePlayer.getName());
                dict.put("score", rs.getInt("maxScore"));
                list.add(dict);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            Log.severe("Failed to get top scores: " + e.getMessage());
        }

        return list;
    }

    /**
     * Returns a list of top 5 players as pairs of name and score.
     * Each element is a String[]{name, score}
     */
    public static List<String[]> getTopPlayers() {
        var list = new java.util.ArrayList<String[]>();
        try {
            var ps = Connection.prepareStatement("SELECT uuid, maxScore FROM scores ORDER BY maxScore DESC LIMIT 5;");
            var rs = ps.executeQuery();
            while (rs.next()) {
                var uuid = UUID.fromString(rs.getString("uuid"));
                var offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                String name = offlinePlayer.getName();
                String score = String.valueOf(rs.getInt("maxScore"));
                list.add(new String[]{name, score});
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            Log.severe("Failed to get top players: " + e.getMessage());
        }
        return list;
    }
}
