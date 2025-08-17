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
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PlayerClassCommand {

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("playerclass")
                .requires(source -> source.hasPermissionLevel(2))

                // /playerclass <player> - Admin info lookup only
                .then(CommandManager.argument("player", EntityArgumentType.players())
                        .executes(PlayerClassCommand::getPlayerInfo)
                )

                // /playerclass set <class> [player] - Set class
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("class", StringArgumentType.word())
                                .suggests(PlayerClassCommand::suggestClasses)
                                .executes(PlayerClassCommand::setSelfClass)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::setPlayerClass)
                                )
                        )
                )

                // /playerclass level <level> [player] - Set level
                .then(CommandManager.literal("level")
                        .then(CommandManager.argument("level", IntegerArgumentType.integer(1, 50))
                                .executes(PlayerClassCommand::setSelfLevel)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::setPlayerLevel)
                                )
                        )
                )

                // /playerclass resource <amount> [player] - Set resource
                .then(CommandManager.literal("resource")
                        .then(CommandManager.argument("amount", FloatArgumentType.floatArg(0))
                                .executes(PlayerClassCommand::setSelfResource)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::setPlayerResource)
                                )
                        )
                )

                // /playerclass points [amount] [player] - Get/Set class points
                .then(CommandManager.literal("points")
                        .executes(PlayerClassCommand::getSelfPoints)
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0, 1000))
                                .executes(PlayerClassCommand::setSelfPoints)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::setPlayerPoints)
                                )
                        )
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .executes(PlayerClassCommand::getPlayerPoints)
                        )
                )

                // /playerclass reset [player] - Reset to Novice level 1
                .then(CommandManager.literal("reset")
                        .executes(PlayerClassCommand::resetSelf)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .executes(PlayerClassCommand::resetPlayer)
                        )
                );
    }

    // === SUGGESTIONS ===
    private static CompletableFuture<Suggestions> suggestClasses(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        ClassRegistry.getAllClasses().forEach(clazz -> builder.suggest(clazz.getId()));
        return builder.buildFuture();
    }

    // === ADMIN INFO LOOKUP ===
    private static int getPlayerInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            showInfo(context.getSource(), player);
        }
        return players.size();
    }

    // === SET CLASS ===
    private static int setSelfClass(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String className = StringArgumentType.getString(context, "class");
        return setClass(context.getSource(), player, className);
    }

    private static int setPlayerClass(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        String className = StringArgumentType.getString(context, "class");
        for (ServerPlayerEntity player : players) {
            setClass(context.getSource(), player, className);
        }
        return players.size();
    }

    // === SET LEVEL ===
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

    // === SET RESOURCE ===
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

    // === CLASS POINTS ===
    private static int getSelfPoints(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return showPoints(context.getSource(), player);
    }

    private static int getPlayerPoints(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            showPoints(context.getSource(), player);
        }
        return players.size();
    }

    private static int setSelfPoints(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        return setPoints(context.getSource(), player, amount);
    }

    private static int setPlayerPoints(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (ServerPlayerEntity player : players) {
            setPoints(context.getSource(), player, amount);
        }
        return players.size();
    }

    // === RESET ===
    private static int resetSelf(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return resetPlayer(context.getSource(), player);
    }

    private static int resetPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            resetPlayer(context.getSource(), player);
        }
        return players.size();
    }

    // === IMPLEMENTATION METHODS ===

    private static int showInfo(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager manager = classComponent.getClassManager();
        PlayerClass currentClass = manager.getCurrentClass();

        Text message = Text.literal(player.getName().getString()).formatted(Formatting.GOLD)
                .append(Text.literal(" | ")).append(currentClass.getFormattedName())
                .append(Text.literal(String.format(" | Lv.%d/%d", manager.getClassLevel(), currentClass.getMaxLevel())))
                .append(Text.literal(String.format(" | Points: %d", manager.getClassStatPoints())))
                .append(Text.literal(String.format(" | %s: %.1f/%.1f",
                        manager.getResourceType().getDisplayName(),
                        manager.getCurrentResource(),
                        manager.getMaxResource())));

        if (manager.hasTranscended()) {
            message = message.copy().append(Text.literal(" ✨").formatted(Formatting.GOLD));
        }

        Text finalMessage = message;
        source.sendFeedback(() -> finalMessage, false);
        return 1;
    }

    private static int setClass(ServerCommandSource source, ServerPlayerEntity player, String className) {
        PlayerClass targetClass = ClassRegistry.getClass(className);
        if (targetClass == null) {
            source.sendError(Text.literal("Unknown class: " + className).formatted(Formatting.RED));
            return 0;
        }

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        classComponent.performBatchUpdate(() -> classComponent.getClassManager().setClass(targetClass));

        Text message = Text.literal("Set " + player.getName().getString() + "'s class to ")
                .formatted(Formatting.GREEN)
                .append(targetClass.getFormattedName());

        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int setLevel(ServerCommandSource source, ServerPlayerEntity player, int level) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        classComponent.setLevel((short) level);

        Text message = Text.literal(String.format("Set %s's level to %d",
                        player.getName().getString(), level))
                .formatted(Formatting.GREEN);

        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int setResource(ServerCommandSource source, ServerPlayerEntity player, float amount) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        classComponent.setCurrentResource(amount);

        Text message = Text.literal(String.format("Set %s's resource to %.1f",
                        player.getName().getString(), amount))
                .formatted(Formatting.GREEN);

        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int showPoints(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager manager = classComponent.getClassManager();

        Text message = Text.literal(String.format("%s | Class Points: %d",
                        player.getName().getString(),
                        manager.getClassStatPoints()))
                .formatted(Formatting.YELLOW);

        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static int setPoints(ServerCommandSource source, ServerPlayerEntity player, int amount) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);

        classComponent.performBatchUpdate(() -> {
            // Set points directly (not add)
            classComponent.getClassManager().getClassLevelSystem().setStatPoints((short) amount);
        });

        Text message = Text.literal(String.format("Set %s's class points to %d",
                        player.getName().getString(), amount))
                .formatted(Formatting.GREEN);

        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int resetPlayer(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);

        // Reset to Novice level 1 with 0 points
        PlayerClass noviceClass = ClassRegistry.getClass("novice");
        if (noviceClass != null) {
            classComponent.performBatchUpdate(() -> {
                classComponent.getClassManager().setClass(noviceClass);
                classComponent.setLevel((short) 1);
                classComponent.getClassManager().getClassLevelSystem().setExperience(0);
                classComponent.getClassManager().getClassLevelSystem().setStatPoints((short) 0);
            });

            Text message = Text.literal(String.format("Reset %s to Novice level 1 (0 points)",
                            player.getName().getString()))
                    .formatted(Formatting.YELLOW);

            source.sendFeedback(() -> message, true);
            return 1;
        } else {
            source.sendError(Text.literal("Could not find Novice class!").formatted(Formatting.RED));
            return 0;
        }
    }
}

/* ===== CLEAN ADMIN COMMAND STRUCTURE =====

Player Info (Admin only):
/playerclass Steve              - Show Steve's full class info

Set Commands:
/playerclass set swordman       - Set your class to Swordman
/playerclass set mage Steve     - Set Steve's class to Mage
/playerclass level 25           - Set your level to 25
/playerclass level 30 Steve     - Set Steve's level to 30
/playerclass resource 150       - Set your resource to 150
/playerclass resource 200 Steve - Set Steve's resource to 200

Class Points:
/playerclass points             - Show your class points
/playerclass points 50          - Set your class points to 50
/playerclass points Steve       - Show Steve's class points
/playerclass points 75 Steve    - Set Steve's class points to 75

Reset:
/playerclass reset              - Reset yourself to Novice level 1
/playerclass reset Steve        - Reset Steve to Novice level 1

Output Example:
Steve | Swordman | Lv.25/40 | Points: 15 | Rage: 150.0/200.0 ✨

================================= */