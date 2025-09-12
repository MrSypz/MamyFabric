package com.sypztep.mamy.common.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

public class LevelCommand {

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("level")
                // /level get [player]
                .then(CommandManager.literal("get")
                        .executes(LevelCommand::getSelfLevel)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(LevelCommand::getPlayerLevel)
                        )
                )
                // /level set <level> [player]
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("level", IntegerArgumentType.integer(1))
                                .executes(LevelCommand::setSelfLevel)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .requires(source -> source.hasPermissionLevel(2))
                                        .executes(LevelCommand::setPlayerLevel)
                                )
                        )
                )
                // /level reset [player]
                .then(CommandManager.literal("reset")
                        .executes(LevelCommand::resetSelfLevel)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(LevelCommand::resetPlayerLevel)
                        )
                )
                // /level max [player]
                .then(CommandManager.literal("max")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(LevelCommand::maxSelfLevel)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .executes(LevelCommand::maxPlayerLevel)
                        )
                )
                .then(CommandManager.literal("info")
                        .executes(LevelCommand::getSelfInfo)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(LevelCommand::getPlayerInfo)
                        )
                );
    }

    private static int getSelfLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return showLevelInfo(context.getSource(), player);
    }

    private static int getPlayerLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");

        for (ServerPlayerEntity player : players) {
            showLevelInfo(context.getSource(), player);
        }

        return players.size();
    }

    private static int setSelfLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        int level = IntegerArgumentType.getInteger(context, "level");
        return setLevel(context.getSource(), player, level);
    }

    private static int setPlayerLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        int level = IntegerArgumentType.getInteger(context, "level");

        for (ServerPlayerEntity player : players) {
            setLevel(context.getSource(), player, level);
        }

        return players.size();
    }

    private static int resetSelfLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return resetLevel(context.getSource(), player);
    }

    private static int resetPlayerLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");

        for (ServerPlayerEntity player : players) {
            resetLevel(context.getSource(), player);
        }

        return players.size();
    }

    private static int maxSelfLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return maxLevel(context.getSource(), player);
    }

    private static int maxPlayerLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");

        for (ServerPlayerEntity player : players) {
            maxLevel(context.getSource(), player);
        }

        return players.size();
    }

    private static int showLevelInfo(ServerCommandSource source, ServerPlayerEntity player) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);

        Text message = Text.literal(String.format(
                "§6%s's Level Info:\n" +
                        "§7Level: §f%d§7/§f%d\n" +
                        "§7Experience: §f%d§7/§f%d §8(§f%.1f%%§8)\n" +
                        "§7Stat Points: §f%d",
                player.getName().getString(),
                component.getLevel(),
                component.getLevelSystem().getMaxLevel(),
                component.getExperience(),
                component.getExperienceToNextLevel(),
                component.getExperiencePercentage(),
                component.getAvailableStatPoints()
        ));

        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static int setLevel(ServerCommandSource source, ServerPlayerEntity player, int level) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);

        int maxLevel = component.getLevelSystem().getMaxLevel();
        if (level > maxLevel) {
            source.sendError(Text.literal(String.format("§cLevel %d exceeds maximum level %d", level, maxLevel)));
            return 0;
        }

        int oldLevel = component.getLevel();

        component.setLevel(level);
        LivingEntityUtil.updateClassModifierBonus(player);

        Text message = Text.literal(String.format(
                "§6Set %s's level from §f%d §6to §f%d",
                player.getName().getString(), oldLevel, level
        ));

        source.sendFeedback(() -> message, true);

        // Notify the player
        player.sendMessage(Text.literal(String.format(
                "§6Your level has been set to §f%d §6by an administrator",
                level
        )).formatted(Formatting.GOLD), false);

        return 1;
    }

    // Fixed resetLevel method
    private static int resetLevel(ServerCommandSource source, ServerPlayerEntity player) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);

        int oldLevel = component.getLevel();
        long oldExp = component.getExperience();
        int oldStatPoints = component.getAvailableStatPoints();

        component.performBatchUpdate(() -> {
            component.setLevel((short) 1);
            component.getLevelSystem().setExperience(0);
            component.getLevelSystem().setStatPoints((short) ModConfig.startStatpoints);
            component.getLivingStats().resetStats(player,false);
        });

        Text message = Text.literal(String.format(
                "§6Reset %s's progress:\n" +
                        "§7Level: §f%d §7→ §f1\n" +
                        "§7Experience: §f%d §7→ §f0\n" +
                        "§7Stat Points: §f%d §7→ §f%d",
                player.getName().getString(),
                oldLevel,
                oldExp,
                oldStatPoints, ModConfig.startStatpoints
        ));

        source.sendFeedback(() -> message, true);

        player.sendMessage(Text.literal(
                "§6Your level progress has been reset by an administrator"
        ).formatted(Formatting.GOLD), false);

        return 1;
    }

    private static int maxLevel(ServerCommandSource source, ServerPlayerEntity player) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);

        int oldLevel = component.getLevel();
        int maxLevel = component.getLevelSystem().getMaxLevel();

        component.setLevel(maxLevel);

        Text message = Text.literal(String.format(
                "§6Set %s to maximum level (§f%d §6→ §f%d§6)",
                player.getName().getString(), oldLevel, maxLevel
        ));

        source.sendFeedback(() -> message, true);

        player.sendMessage(Text.literal(
                "§6You have been set to maximum level by an administrator!"
        ).formatted(Formatting.GOLD), false);

        return 1;
    }

    private static int getSelfInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return showLevelInfo(context.getSource(), player);
    }

    private static int getPlayerInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");

        for (ServerPlayerEntity player : players)
            showLevelInfo(context.getSource(), player);

        return players.size();
    }
}