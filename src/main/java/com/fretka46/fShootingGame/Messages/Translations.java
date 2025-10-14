package com.fretka46.fShootingGame.Messages;

import com.fretka46.fShootingGame.FShootingGame;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Translations {

    private static final String PREFIX = FShootingGame.getPlugin(FShootingGame.class).getConfig().getString("message_prefix");
    public static final MiniMessage MINI_MESSAGE = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();

    public static void send(CommandSender player, String key) {
        String message = FShootingGame.getPlugin(
                FShootingGame.class).getConfig().getString(key, "ERR: " + key + " not found in config.");

        player.sendMessage(MINI_MESSAGE.deserialize(PREFIX + " " + message));
    }

    public static void send(Player player, String key, TagResolver... resolvers) {
        String message = FShootingGame.getPlugin(
                FShootingGame.class).getConfig().getString(key, "ERR: " + key + " not found in config.");

        player.sendMessage(MINI_MESSAGE.deserialize(PREFIX + " " + message, resolvers));
    }
}
