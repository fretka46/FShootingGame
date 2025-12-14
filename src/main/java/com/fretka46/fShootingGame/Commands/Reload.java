package com.fretka46.fShootingGame.Commands;

import com.fretka46.fShootingGame.FShootingGame;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Reload implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {

        // Check permissions

        var sender = source.getSender();
        if (!sender.hasPermission("fshootinggame.reload")) {
            sender.sendMessage("You do not have permission to use this command.");
        }

        // Reload the config
        var plugin = FShootingGame.getPlugin(FShootingGame.class);
        plugin.reloadConfig();

        // Send confirmation message
        sender.sendMessage("Config reloaded successfully.");
    }
}