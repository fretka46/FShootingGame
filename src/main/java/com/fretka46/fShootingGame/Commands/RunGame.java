package com.fretka46.fShootingGame.Commands;

import com.fretka46.fShootingGame.Engine;
import com.fretka46.fShootingGame.Messages.Translations;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RunGame implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Engine.isRunning) {
            Translations.send(source.getSender(), "already_running");
            return;
        }

        Translations.send(source.getSender(), "start_message");
        Engine.startGame();
    }
}