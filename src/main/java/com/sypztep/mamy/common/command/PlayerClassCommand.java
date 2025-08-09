package com.sypztep.mamy.common.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.ClassRegistry;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.classes.PlayerClassManager;
import com.sypztep.mamy.common.system.stat.Stat;
import com.sypztep.mamy.common.system.stat.StatTypes;
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
                // /playerclass info [player] - Basic class info
                .then(CommandManager.literal("info")
                        .executes(PlayerClassCommand::getSelfClassInfo)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(PlayerClassCommand::getPlayerClassInfo)
                        )
                )

                // /playerclass detailed [player] - Detailed information
                .then(CommandManager.literal("detailed")
                        .executes(PlayerClassCommand::getSelfClassDetailed)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(PlayerClassCommand::getPlayerClassDetailed)
                        )
                )

                // /playerclass setclass <className> [player]
                .then(CommandManager.literal("setclass")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("className", StringArgumentType.word())
                                .suggests(PlayerClassCommand::suggestClassNames)
                                .executes(PlayerClassCommand::setSelfClass)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::setPlayerClass)
                                )
                        )
                )

                // /playerclass transcend <className> [player] - Force transcendence
                .then(CommandManager.literal("transcend")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("className", StringArgumentType.word())
                                .suggests(PlayerClassCommand::suggestTranscendentClasses)
                                .executes(PlayerClassCommand::transcendSelfClass)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::transcendPlayerClass)
                                )
                        )
                )

                // /playerclass level commands
                .then(CommandManager.literal("level")
                        .executes(PlayerClassCommand::getSelfClassLevel)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(PlayerClassCommand::getPlayerClassLevel)
                        )
                )

                // /playerclass setlevel <level> [player]
                .then(CommandManager.literal("setlevel")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("level", IntegerArgumentType.integer(1, 50))
                                .executes(PlayerClassCommand::setSelfClassLevel)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::setPlayerClassLevel)
                                )
                        )
                )

                // /playerclass maxlevel [player] - Set to max level
                .then(CommandManager.literal("maxlevel")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(PlayerClassCommand::maxSelfClassLevel)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .executes(PlayerClassCommand::maxPlayerClassLevel)
                        )
                )

                // /playerclass addexp <amount> [player] - Add class experience
                .then(CommandManager.literal("addexp")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("amount", LongArgumentType.longArg(1))
                                .executes(PlayerClassCommand::addSelfClassExp)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::addPlayerClassExp)
                                )
                        )
                )

                // /playerclass reset commands
                .then(CommandManager.literal("reset")
                        .requires(source -> source.hasPermissionLevel(2))
                        // /playerclass reset level [player] - Reset class level to 1
                        .then(CommandManager.literal("level")
                                .executes(PlayerClassCommand::resetSelfClassLevel)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::resetPlayerClassLevel)
                                )
                        )
                        // /playerclass reset stats [player] - Reset class stats
                        .then(CommandManager.literal("stats")
                                .executes(PlayerClassCommand::resetSelfClassStats)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::resetPlayerClassStats)
                                )
                        )
                        // /playerclass reset all [player] - Reset everything (like transcendence)
                        .then(CommandManager.literal("all")
                                .executes(PlayerClassCommand::resetSelfClassAll)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::resetPlayerClassAll)
                                )
                        )
                )

                // /playerclass points commands
                .then(CommandManager.literal("points")
                        .executes(PlayerClassCommand::getSelfClassPoints)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(PlayerClassCommand::getPlayerClassPoints)
                        )
                )

                // /playerclass addpoints <amount> [player]
                .then(CommandManager.literal("addpoints")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 1000))
                                .executes(PlayerClassCommand::addSelfClassPoints)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::addPlayerClassPoints)
                                )
                        )
                )

                // /playerclass resource commands
                .then(CommandManager.literal("resource")
                        .executes(PlayerClassCommand::getSelfResource)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(PlayerClassCommand::getPlayerResource)
                        )
                )

                // /playerclass setresource <amount> [player]
                .then(CommandManager.literal("setresource")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("amount", FloatArgumentType.floatArg(0))
                                .executes(PlayerClassCommand::setSelfResource)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PlayerClassCommand::setPlayerResource)
                                )
                        )
                )

                // /playerclass restore [player] - Restore resource to full
                .then(CommandManager.literal("restore")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(PlayerClassCommand::restoreSelfResource)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .executes(PlayerClassCommand::restorePlayerResource)
                        )
                )

                // /playerclass evolutions [player] - Show available evolutions
                .then(CommandManager.literal("evolutions")
                        .executes(PlayerClassCommand::getSelfEvolutions)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(PlayerClassCommand::getPlayerEvolutions)
                        )
                )

                // /playerclass list [tier] - List all classes or classes by tier
                .then(CommandManager.literal("list")
                        .executes(PlayerClassCommand::listAllClasses)
                        .then(CommandManager.argument("tier", IntegerArgumentType.integer(0, 3))
                                .executes(PlayerClassCommand::listClassesByTier)
                        )
                )

                // /playerclass tree - Show class progression tree
                .then(CommandManager.literal("tree")
                        .executes(PlayerClassCommand::showClassTree)
                )

                // /playerclass transcendent - List all transcendent classes
                .then(CommandManager.literal("transcendent")
                        .executes(PlayerClassCommand::listTranscendentClasses)
                );
    }

    // === SUGGESTIONS ===
    private static CompletableFuture<Suggestions> suggestClassNames(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        ClassRegistry.getAllClasses().forEach(clazz -> builder.suggest(clazz.getId()));
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestTranscendentClasses(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        ClassRegistry.getTranscendentClasses().forEach(clazz -> builder.suggest(clazz.getId()));
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

    // === TRANSCEND CLASS ===
    private static int transcendSelfClass(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String className = StringArgumentType.getString(context, "className");
        return transcendClass(context.getSource(), player, className);
    }

    private static int transcendPlayerClass(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        String className = StringArgumentType.getString(context, "className");

        for (ServerPlayerEntity player : players) {
            transcendClass(context.getSource(), player, className);
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
        int level = IntegerArgumentType.getInteger(context, "level");
        return setClassLevel(context.getSource(), player, (short) level);
    }

    private static int setPlayerClassLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        int level = IntegerArgumentType.getInteger(context, "level");

        for (ServerPlayerEntity player : players) {
            setClassLevel(context.getSource(), player, (short) level);
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

    // === ADD EXPERIENCE ===
    private static int addSelfClassExp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        long amount = LongArgumentType.getLong(context, "amount");
        return addClassExperience(context.getSource(), player, amount);
    }

    private static int addPlayerClassExp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        long amount = LongArgumentType.getLong(context, "amount");

        for (ServerPlayerEntity player : players) {
            addClassExperience(context.getSource(), player, amount);
        }
        return players.size();
    }

    // === RESET COMMANDS ===
    private static int resetSelfClassLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return resetClassLevel(context.getSource(), player);
    }

    private static int resetPlayerClassLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            resetClassLevel(context.getSource(), player);
        }
        return players.size();
    }

    private static int resetSelfClassStats(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return resetClassStats(context.getSource(), player);
    }

    private static int resetPlayerClassStats(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            resetClassStats(context.getSource(), player);
        }
        return players.size();
    }

    private static int resetSelfClassAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return resetClassAll(context.getSource(), player);
    }

    private static int resetPlayerClassAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            resetClassAll(context.getSource(), player);
        }
        return players.size();
    }

    // === CLASS POINTS ===
    private static int getSelfClassPoints(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return showClassPoints(context.getSource(), player);
    }

    private static int getPlayerClassPoints(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            showClassPoints(context.getSource(), player);
        }
        return players.size();
    }

    private static int addSelfClassPoints(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        return addClassPoints(context.getSource(), player, (short) amount);
    }

    private static int addPlayerClassPoints(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        for (ServerPlayerEntity player : players) {
            addClassPoints(context.getSource(), player, (short) amount);
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

    // === LIST COMMANDS ===
    private static int listAllClasses(CommandContext<ServerCommandSource> context) {
        return showAllClasses(context.getSource());
    }

    private static int listClassesByTier(CommandContext<ServerCommandSource> context) {
        int tier = IntegerArgumentType.getInteger(context, "tier");
        return showClassesByTier(context.getSource(), tier);
    }

    private static int showClassTree(CommandContext<ServerCommandSource> context) {
        return displayClassTree(context.getSource());
    }

    private static int listTranscendentClasses(CommandContext<ServerCommandSource> context) {
        return showTranscendentClasses(context.getSource());
    }

    // === IMPLEMENTATION METHODS ===

    private static int showBasicClassInfo(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager manager = classComponent.getClassManager();
        PlayerClass currentClass = manager.getCurrentClass();

        Text message = Text.literal(String.format("§6%s's Class Info:", player.getName().getString()))
                .append(Text.literal("\n§7Class: ")).append(currentClass.getFormattedName())
                .append(Text.literal(String.format("\n§7Level: §f%d§7/§f%d", manager.getClassLevel(), currentClass.getMaxLevel())))
                .append(Text.literal(String.format("\n§7Experience: §f%d§7/§f%d §8(%.1f%%)",
                        manager.getClassExperience(), manager.getClassExperienceToNext(), manager.getClassProgressPercentage())))
                .append(Text.literal(String.format("\n§7Class Points: §f%d", manager.getClassStatPoints())))
                .append(Text.literal(String.format("\n§7%s: §b%.1f§7/§b%.1f",
                        manager.getResourceType().getDisplayName(), manager.getCurrentResource(), manager.getMaxResource())));

        if (manager.hasTranscended()) {
            message = message.copy().append(Text.literal("\n§6✨ Transcended ✨").formatted(Formatting.GOLD, Formatting.BOLD));
        }

        Text finalMessage = message;
        source.sendFeedback(() -> finalMessage, false);
        return 1;
    }

    private static int showDetailedClassInfo(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager manager = classComponent.getClassManager();
        PlayerClass currentClass = manager.getCurrentClass();

        Text message = Text.literal(String.format("§6=== %s's Detailed Class Info ===", player.getName().getString()))
                .append(Text.literal("\n§7Class: ")).append(currentClass.getFormattedNameWithTier())
                .append(Text.literal(String.format("\n§7Tier: §f%d §8| §7Branch: §f%d", currentClass.getTier(), currentClass.getBranch())))
                .append(Text.literal(String.format("\n§7Level: §f%d§7/§f%d §8(Max Level: %d)",
                        manager.getClassLevel(), currentClass.getMaxLevel(), currentClass.getMaxLevel())))
                .append(Text.literal(String.format("\n§7Experience: §f%d§7/§f%d §8(%.1f%%)",
                        manager.getClassExperience(), manager.getClassExperienceToNext(), manager.getClassProgressPercentage())))
                .append(Text.literal(String.format("\n§7Class Points: §f%d", manager.getClassStatPoints())))
                .append(Text.literal(String.format("\n§7%s: §b%.1f§7/§b%.1f §8(%.1f%%)",
                        manager.getResourceType().getDisplayName(), manager.getCurrentResource(),
                        manager.getMaxResource(), manager.getResourcePercentage())))
                .append(Text.literal("\n§7Description: §f" + currentClass.getDescription()));

        if (manager.hasTranscended()) {
            message = message.copy().append(Text.literal("\n§6✨ Transcended Player ✨").formatted(Formatting.GOLD, Formatting.BOLD));
        }

        if (currentClass.isTranscendent()) {
            message = message.copy().append(Text.literal("\n§5⚡ Transcendent Class ⚡").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        }

        Text finalMessage = message;
        source.sendFeedback(() -> finalMessage, false);
        return 1;
    }

    private static int setClass(ServerCommandSource source, ServerPlayerEntity player, String className) {
        PlayerClass targetClass = ClassRegistry.getClass(className);
        if (targetClass == null) {
            source.sendError(Text.literal(String.format("§cUnknown class: %s", className)));
            return 0;
        }

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);

        classComponent.performBatchUpdate(() -> classComponent.getClassManager().setClass(targetClass));

        Text message = Text.literal(String.format("§6Set %s's class to ", player.getName().getString()))
                .append(targetClass.getFormattedName());

        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int transcendClass(ServerCommandSource source, ServerPlayerEntity player, String className) {
        PlayerClass targetClass = ClassRegistry.getClass(className);
        if (targetClass == null) {
            source.sendError(Text.literal(String.format("§cUnknown class: %s", className)));
            return 0;
        }

        if (!targetClass.isTranscendent()) {
            source.sendError(Text.literal(String.format("§c%s is not a transcendent class!", targetClass.getDisplayName())));
            return 0;
        }

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);

        // Force transcendence
        classComponent.performBatchUpdate(() -> {
            classComponent.getClassManager().setClass(targetClass);
            classComponent.getClassManager().getClassLevelSystem().resetForTranscendence();
        });

        Text message = Text.literal("§6✨ FORCED TRANSCENDENCE ✨\n")
                .append(Text.literal(String.format("§6%s has transcended to ", player.getName().getString())))
                .append(targetClass.getFormattedName())
                .append(Text.literal("§6! Level reset to 1!"));

        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int showClassLevel(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager manager = classComponent.getClassManager();

        Text message = Text.literal(String.format("§6%s's class level: §f%d§6/§f%d §8(%.1f%% to next level)",
                player.getName().getString(), manager.getClassLevel(),
                manager.getClassLevelSystem().getMaxLevel(), manager.getClassProgressPercentage()));

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

    private static int addClassExperience(ServerCommandSource source, ServerPlayerEntity player, long amount) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);

        int oldLevel = classComponent.getClassManager().getClassLevel();
        classComponent.addClassExperience(amount);
        int newLevel = classComponent.getClassManager().getClassLevel();

        Text message = Text.literal(String.format("§6Added §f%d §6class experience to %s", amount, player.getName().getString()));

        if (newLevel > oldLevel) {
            message = message.copy().append(Text.literal(String.format(" §8(Level §f%d §8→ §f%d§8)", oldLevel, newLevel)));
        }

        Text finalMessage = message;
        source.sendFeedback(() -> finalMessage, true);
        return 1;
    }

    private static int resetClassLevel(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);

        classComponent.performBatchUpdate(() -> {
            classComponent.getClassManager().getClassLevelSystem().setLevel((short) 1);
            classComponent.getClassManager().getClassLevelSystem().setExperience(0);
        });

        Text message = Text.literal(String.format("§6Reset %s's class level to §f1", player.getName().getString()));
        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int resetClassStats(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.get(player);

        classComponent.performBatchUpdate(() -> {
            // Reset all stats and return points
            for (StatTypes statType : StatTypes.values()) {
                Stat stat = levelComponent.getStatByType(statType);
                if (stat != null) {
                    short pointsUsed = stat.getTotalPointsUsed();
                    classComponent.getClassManager().getClassLevelSystem().addStatPoints(pointsUsed);
                    stat.reset(player, classComponent.getClassManager().getClassLevelSystem(), false);
                }
            }
        });

        Text message = Text.literal(String.format("§6Reset %s's class stats and returned all points", player.getName().getString()));
        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int resetClassAll(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);

        classComponent.performBatchUpdate(() -> {
            classComponent.getClassManager().getClassLevelSystem().resetForTranscendence();
        });

        Text message = Text.literal(String.format("§6Reset %s's class level, experience, and stat points to 0 (like transcendence)",
                player.getName().getString()));
        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int showClassPoints(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager manager = classComponent.getClassManager();

        Text message = Text.literal(String.format("§6%s's class stat points: §f%d",
                player.getName().getString(), manager.getClassStatPoints()));

        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static int addClassPoints(ServerCommandSource source, ServerPlayerEntity player, short amount) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);

        classComponent.performBatchUpdate(() -> {
            classComponent.getClassManager().getClassLevelSystem().addStatPoints(amount);
        });

        Text message = Text.literal(String.format("§6Added §f%d §6class stat points to %s",
                amount, player.getName().getString()));

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
        var transcendence = manager.getAvailableTranscendence();

        Text message = Text.literal(String.format("§6%s's Available Class Evolutions:", player.getName().getString()));

        if (evolutions.isEmpty() && transcendence.isEmpty()) {
            message = message.copy().append(Text.literal("\n§cNo evolutions available at current level."));
        } else {
            if (!evolutions.isEmpty()) {
                message = message.copy().append(Text.literal("\n§7Normal Evolutions:"));
                for (PlayerClass evolution : evolutions) {
                    message = message.copy().append(Text.literal("\n§7- ")).append(evolution.getFormattedName())
                            .append(Text.literal(String.format(" §8[%s]", evolution.getClassCode())));
                }
            }

            if (!transcendence.isEmpty()) {
                message = message.copy().append(Text.literal("\n§6✨ Transcendent Evolutions:"));
                for (PlayerClass transcendentClass : transcendence) {
                    message = message.copy().append(Text.literal("\n§6- ")).append(transcendentClass.getFormattedName())
                            .append(Text.literal(String.format(" §8[%s] §c(Will reset level!)", transcendentClass.getClassCode())));
                }
            }
        }

        Text finalMessage = message;
        source.sendFeedback(() -> finalMessage, false);
        return 1;
    }

    private static int showAllClasses(ServerCommandSource source) {
        Text message = Text.literal("§6=== All Available Classes ===");

        for (PlayerClass clazz : ClassRegistry.getAllClasses()) {
            String transcendentMark = clazz.isTranscendent() ? " §5⚡" : "";
            message = message.copy().append(Text.literal(String.format("\n§7- §f%s §8[%s] §7(Max Lv.%d)%s",
                    clazz.getDisplayName(), clazz.getClassCode(), clazz.getMaxLevel(), transcendentMark)));
        }

        Text finalMessage = message;
        source.sendFeedback(() -> finalMessage, false);
        return 1;
    }

    private static int showClassesByTier(ServerCommandSource source, int tier) {
        var classes = ClassRegistry.getClassesByTier(tier);

        if (classes.isEmpty()) {
            source.sendError(Text.literal(String.format("§cNo classes found for tier %d", tier)));
            return 0;
        }

        Text message = Text.literal(String.format("§6=== Tier %d Classes ===", tier));

        for (PlayerClass clazz : classes) {
            String transcendentMark = clazz.isTranscendent() ? " §5⚡" : "";
            message = message.copy().append(Text.literal(String.format("\n§7- §f%s §8[%s] §7(Max Lv.%d)%s",
                    clazz.getDisplayName(), clazz.getClassCode(), clazz.getMaxLevel(), transcendentMark)));
        }

        Text finalMessage = message;
        source.sendFeedback(() -> finalMessage, false);
        return 1;
    }

    private static int displayClassTree(ServerCommandSource source) {
        String tree = ClassRegistry.getClassTree();

        Text message = Text.literal("§6=== Class Progression Tree ===\n§f" + tree);
        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static int showTranscendentClasses(ServerCommandSource source) {
        var transcendentClasses = ClassRegistry.getTranscendentClasses();

        if (transcendentClasses.isEmpty()) {
            source.sendError(Text.literal("§cNo transcendent classes found!"));
            return 0;
        }

        Text message = Text.literal("§6✨ === Transcendent Classes === ✨");

        for (PlayerClass clazz : transcendentClasses) {
            message = message.copy().append(Text.literal(String.format("\n§5⚡ §f%s §8[%s] §7(Max Lv.%d)",
                    clazz.getDisplayName(), clazz.getClassCode(), clazz.getMaxLevel())));

            // Show requirements
            if (!clazz.getRequirements().isEmpty()) {
                message = message.copy().append(Text.literal(" §7- Requires: "));
                for (var req : clazz.getRequirements()) {
                    message = message.copy().append(Text.literal(String.format("§f%s Lv.%d ",
                            req.previousClass().getDisplayName(), req.requiredLevel())));
                }
            }
        }

        Text finalMessage = message;
        source.sendFeedback(() -> finalMessage, false);
        return 1;
    }
}