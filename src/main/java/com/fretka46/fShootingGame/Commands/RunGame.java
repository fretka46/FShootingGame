package com.fretka46.fShootingGame.Commands;

import com.fretka46.fShootingGame.Engine;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RunGame implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        Engine.startGame();
    }
}