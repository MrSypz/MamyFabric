package com.sypztep.mamy.common.init;

import com.sypztep.mamy.common.command.ExpCommand;
import com.sypztep.mamy.common.command.LevelCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;

public final class ModCommands {
    public ModCommands() {}
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("mamy")
                    .then(LevelCommand.register())
                    .then(ExpCommand.register())
//                    .then(BenefitsCommand.register())
//                    .then(DebugCommand.register())
//                    .then(MobStatsCommand.register())
//                    .then(PlayerStatsCommand.register())
            );
        });
    }
}
