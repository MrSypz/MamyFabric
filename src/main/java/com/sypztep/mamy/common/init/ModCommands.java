package com.sypztep.mamy.common.init;

import com.sypztep.mamy.common.command.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;

public final class ModCommands {
    public ModCommands() {}
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("mamy")
                .then(LevelCommand.register())
                .then(ExpCommand.register())
                .then(StatCommand.register())
                .then(PassiveAbilityCommand.register())
                .then(PlayerClassCommand.register())
        ));
    }
}
