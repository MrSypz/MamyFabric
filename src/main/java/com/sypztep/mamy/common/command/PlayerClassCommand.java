package com.sypztep.mamy.common.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.ClassRegistry;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.classes.PlayerClassManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PlayerClassCommand {

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("class")
                // /class get [player]
                .then(CommandManager.literal("get")
                        .executes(PlayerClassCommand::getSelfClassInfo)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(PlayerClassCommand::getPlayerClassInfo)
                        )
                )
                // /class info [player] - Detailed information
                .then(CommandManager.literal("info")
                        .executes(PlayerClassCommand::getSelfClassDetailed)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(PlayerClassCommand::getPlayerClassDetailed)
                        )
                )
                // /class set <className> [player]
                .then(CommandManager.literal("set")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("className", StringArgumentType.word())
                                .suggests(PlayerClassCommand::suggestClassNames)
                                .executes(PlayerClassCommand::setSelfClass)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::setPlayerClass)
                                )
                        )
                )
                // /class level get [player]
                .then(CommandManager.literal("level")
                        .then(CommandManager.literal("get")
                                .executes(PlayerClassCommand::getSelfClassLevel)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .requires(source -> source.hasPermissionLevel(2))
                                        .executes(PlayerClassCommand::getPlayerClassLevel)
                                )
                        )
                        // /class level set <level> [player]
                        .then(CommandManager.literal("set")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.argument("level", IntegerArgumentType.integer(1, 50))
                                        .executes(PlayerClassCommand::setSelfClassLevel)
                                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                                .executes(PlayerClassCommand::setPlayerClassLevel)
                                        )
                                )
                        )
                        // /class level max [player]
                        .then(CommandManager.literal("max")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(PlayerClassCommand::maxSelfClassLevel)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::maxPlayerClassLevel)
                                )
                        )
                )
                // /class resource get [player]
                .then(CommandManager.literal("resource")
                        .then(CommandManager.literal("get")
                                .executes(PlayerClassCommand::getSelfResource)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .requires(source -> source.hasPermissionLevel(2))
                                        .executes(PlayerClassCommand::getPlayerResource)
                                )
                        )
                        // /class resource set <amount> [player]
                        .then(CommandManager.literal("set")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(PlayerClassCommand::setSelfResource)
                                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                                .executes(PlayerClassCommand::setPlayerResource)
                                        )
                                )
                        )
                        // /class resource restore [player]
                        .then(CommandManager.literal("restore")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(PlayerClassCommand::restoreSelfResource)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::restorePlayerResource)
                                )
                        )
                )
                // /class evolution [player] - Show available evolutions
                .then(CommandManager.literal("evolution")
                        .executes(PlayerClassCommand::getSelfEvolutions)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(PlayerClassCommand::getPlayerEvolutions)
                        )
                );
    }

    // === SUGGESTIONS ===
    private static CompletableFuture<Suggestions> suggestClassNames(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        ClassRegistry.getAllClasses().forEach(clazz -> builder.suggest(clazz.getId()));
        return builder.buildFuture();
    }

    // === BASIC CLASS INFO ===
    private static int getSelfClassInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return showBasicClassInfo(context.getSource(), player);
    }

    private static int getPlayerClassInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            showBasicClassInfo(context.getSource(), player);
        }
        return players.size();
    }

    private static int getSelfClassDetailed(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return showDetailedClassInfo(context.getSource(), player);
    }

    private static int getPlayerClassDetailed(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            showDetailedClassInfo(context.getSource(), player);
        }
        return players.size();
    }

    // === SET CLASS ===
    private static int setSelfClass(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String className = StringArgumentType.getString(context, "className");
        return setClass(context.getSource(), player, className);
    }

    private static int setPlayerClass(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        String className = StringArgumentType.getString(context, "className");

        for (ServerPlayerEntity player : players) {
            setClass(context.getSource(), player, className);
        }
        return players.size();
    }

    // === CLASS LEVEL ===
    private static int getSelfClassLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return showClassLevel(context.getSource(), player);
    }

    private static int getPlayerClassLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            showClassLevel(context.getSource(), player);
        }
        return players.size();
    }

    private static int setSelfClassLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        short level = (short) IntegerArgumentType.getInteger(context, "level");
        return setClassLevel(context.getSource(), player, level);
    }

    private static int setPlayerClassLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        short level = (short) IntegerArgumentType.getInteger(context, "level");

        for (ServerPlayerEntity player : players) {
            setClassLevel(context.getSource(), player, level);
        }
        return players.size();
    }

    private static int maxSelfClassLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return maxClassLevel(context.getSource(), player);
    }

    private static int maxPlayerClassLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            maxClassLevel(context.getSource(), player);
        }
        return players.size();
    }

    // === RESOURCE ===
    private static int getSelfResource(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return showResource(context.getSource(), player);
    }

    private static int getPlayerResource(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            showResource(context.getSource(), player);
        }
        return players.size();
    }

    private static int setSelfResource(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        float amount = FloatArgumentType.getFloat(context, "amount");
        return setResource(context.getSource(), player, amount);
    }

    private static int setPlayerResource(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        float amount = FloatArgumentType.getFloat(context, "amount");

        for (ServerPlayerEntity player : players) {
            setResource(context.getSource(), player, amount);
        }
        return players.size();
    }

    private static int restoreSelfResource(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return restoreResource(context.getSource(), player);
    }

    private static int restorePlayerResource(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            restoreResource(context.getSource(), player);
        }
        return players.size();
    }

    // === EVOLUTION ===
    private static int getSelfEvolutions(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return showEvolutions(context.getSource(), player);
    }

    private static int getPlayerEvolutions(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            showEvolutions(context.getSource(), player);
        }
        return players.size();
    }

    // === IMPLEMENTATION METHODS ===

    private static int showBasicClassInfo(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager manager = classComponent.getClassManager();

        Text message = Text.literal(String.format("§6%s's Class Info:", player.getName().getString()))
                .append(Text.literal("\n§7Class: ").append(manager.getCurrentClass().getFormattedName()))
                .append(Text.literal(String.format("\n§7Level: §f%d§7/§f%d",
                        manager.getClassLevel(), manager.getClassLevelSystem().getMaxLevel())))
                .append(Text.literal(String.format("\n§7Resource: §b%.1f§7/§b%.1f %s",
                        manager.getCurrentResource(), manager.getMaxResource(),
                        manager.getResourceType().getDisplayName())));

        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static int showDetailedClassInfo(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager manager = classComponent.getClassManager();

        Text message = Text.literal(String.format("§6=== %s's Detailed Class Info ===", player.getName().getString()))
                .append(Text.literal("\n§7Class: ").append(manager.getCurrentClass().getFormattedName()))
                .append(Text.literal(String.format(" §8[%s]", manager.getCurrentClass().getClassCode())))
                .append(Text.literal(String.format("\n§7Level: §f%d§7/§f%d §8(§a%.1f%% progress§8)",
                        manager.getClassLevel(), manager.getClassLevelSystem().getMaxLevel(),
                        manager.getClassProgressPercentage())))
                .append(Text.literal(String.format("\n§7Experience: §e%d§7/§e%d",
                        manager.getClassExperience(), manager.getClassLevelSystem().getExperienceToNextLevel())))
                .append(Text.literal(String.format("\n§7Resource: §b%.1f§7/§b%.1f %s §8(§a%.1f%%§8)",
                        manager.getCurrentResource(), manager.getMaxResource(),
                        manager.getResourceType().getDisplayName(), manager.getResourcePercentage())))
                .append(Text.literal("\n§7Description: §f").append(manager.getCurrentClass().getFormattedDescription()));

        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static int setClass(ServerCommandSource source, ServerPlayerEntity player, String className) {
        PlayerClass targetClass = ClassRegistry.getClass(className);
        if (targetClass == null) {
            source.sendError(Text.literal(String.format("§cUnknown class: %s", className)));
            return 0;
        }

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);

        classComponent.performBatchUpdate(() -> {
            classComponent.getClassManager().setClass(targetClass);
        });

        Text message = Text.literal(String.format("§6Set %s's class to ", player.getName().getString()))
                .append(targetClass.getFormattedName());

        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int showClassLevel(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager manager = classComponent.getClassManager();

        Text message = Text.literal(String.format("§6%s's class level: §f%d§6/§f%d",
                player.getName().getString(), manager.getClassLevel(), manager.getClassLevelSystem().getMaxLevel()));

        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static int setClassLevel(ServerCommandSource source, ServerPlayerEntity player, short level) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);

        classComponent.setLevel(level);

        Text message = Text.literal(String.format("§6Set %s's class level to §f%d",
                player.getName().getString(), level));

        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int maxClassLevel(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager manager = classComponent.getClassManager();

        int maxLevel = manager.getClassLevelSystem().getMaxLevel();

        classComponent.performBatchUpdate(() -> {
            manager.getClassLevelSystem().setLevel((short) maxLevel);
            manager.getClassLevelSystem().setExperience(0);
        });

        Text message = Text.literal(String.format("§6Set %s's class to max level §f%d",
                player.getName().getString(), maxLevel));

        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int showResource(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager manager = classComponent.getClassManager();

        Text message = Text.literal(String.format("§6%s's %s: §b%.1f§6/§b%.1f §8(§a%.1f%%§8)",
                player.getName().getString(), manager.getResourceType().getDisplayName(),
                manager.getCurrentResource(), manager.getMaxResource(), manager.getResourcePercentage()));

        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static int setResource(ServerCommandSource source, ServerPlayerEntity player, float amount) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);

        classComponent.setCurrentResource(amount);

        Text message = Text.literal(String.format("§6Set %s's resource to §b%.1f",
                player.getName().getString(), amount));

        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int restoreResource(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager manager = classComponent.getClassManager();

        classComponent.setCurrentResource(manager.getMaxResource());

        Text message = Text.literal(String.format("§6Restored %s's %s to full",
                player.getName().getString(), manager.getResourceType().getDisplayName()));

        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int showEvolutions(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager manager = classComponent.getClassManager();

        var evolutions = manager.getAvailableEvolutions();

        Text message = Text.literal(String.format("§6%s's Available Class Evolutions:", player.getName().getString()));

        if (evolutions.isEmpty()) {
            message = message.copy().append(Text.literal("\n§cNo evolutions available at current level."));
        } else {
            for (PlayerClass evolution : evolutions) {
                message = message.copy().append(Text.literal("\n§7- ").append(evolution.getFormattedName())
                        .append(Text.literal(String.format(" §8[%s]", evolution.getClassCode()))));
            }
        }

        Text finalMessage = message;
        source.sendFeedback(() -> finalMessage, false);
        return 1;
    }
}