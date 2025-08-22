package com.sypztep.mamy.common.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.Stat;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class StatCommand {

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("stat")
                // /stat get [player]
                .then(CommandManager.literal("get")
                        .executes(StatCommand::getSelfStats)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(StatCommand::getPlayerStats)
                        )
                )
                // /stat info [player] - Detailed information
                .then(CommandManager.literal("info")
                        .executes(StatCommand::getSelfStatsDetailed)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(StatCommand::getPlayerStatsDetailed)
                        )
                )
                // /stat set <statType> <value> [player]
                .then(CommandManager.literal("set")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("statType", StringArgumentType.word())
                                .suggests(StatCommand::suggestStatTypes)
                                .then(CommandManager.argument("value", IntegerArgumentType.integer(0, ModConfig.maxStatValue))
                                        .executes(StatCommand::setSelfStat)
                                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                                .executes(StatCommand::setPlayerStat)
                                        )
                                )
                        )
                )
                // /stat add <statType> <value> [player]
                .then(CommandManager.literal("add")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("statType", StringArgumentType.word())
                                .suggests(StatCommand::suggestStatTypes)
                                .then(CommandManager.argument("value", IntegerArgumentType.integer(1))
                                        .executes(StatCommand::addSelfStat)
                                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                                .executes(StatCommand::addPlayerStat)
                                        )
                                )
                        )
                )
                // /stat reset [player]
                .then(CommandManager.literal("reset")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(StatCommand::resetSelfStats)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .executes(StatCommand::resetPlayerStats)
                        )
                )
                // /stat cost <statType> [player] - Show cost to increase
                .then(CommandManager.literal("cost")
                        .then(CommandManager.argument("statType", StringArgumentType.word())
                                .suggests(StatCommand::suggestStatTypes)
                                .executes(StatCommand::getSelfStatCost)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .requires(source -> source.hasPermissionLevel(2))
                                        .executes(StatCommand::getPlayerStatCost)
                                )
                        )
                );
    }

    // Suggest stat types for auto-completion
    private static CompletableFuture<Suggestions> suggestStatTypes(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        Arrays.stream(StatTypes.values())
                .map(StatTypes::getName)
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    // Parse stat type from string
    private static StatTypes parseStatType(String statName) {
        for (StatTypes type : StatTypes.values()) {
            if (type.getName().equalsIgnoreCase(statName) || type.getAka().equalsIgnoreCase(statName)) {
                return type;
            }
        }
        return null;
    }

    // === GET COMMANDS ===
    private static int getSelfStats(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return showBasicStats(context.getSource(), player);
    }

    private static int getPlayerStats(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            showBasicStats(context.getSource(), player);
        }
        return players.size();
    }

    private static int getSelfStatsDetailed(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return showDetailedStats(context.getSource(), player);
    }

    private static int getPlayerStatsDetailed(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            showDetailedStats(context.getSource(), player);
        }
        return players.size();
    }

    // === SET COMMANDS ===
    private static int setSelfStat(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String statName = StringArgumentType.getString(context, "statType");
        int value = IntegerArgumentType.getInteger(context, "value");
        return setStat(context.getSource(), player, statName, value);
    }

    private static int setPlayerStat(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        String statName = StringArgumentType.getString(context, "statType");
        int value = IntegerArgumentType.getInteger(context, "value");

        for (ServerPlayerEntity player : players) {
            setStat(context.getSource(), player, statName, value);
        }
        return players.size();
    }

    // === ADD COMMANDS ===
    private static int addSelfStat(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String statName = StringArgumentType.getString(context, "statType");
        int value = IntegerArgumentType.getInteger(context, "value");
        return addStat(context.getSource(), player, statName, value);
    }

    private static int addPlayerStat(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        String statName = StringArgumentType.getString(context, "statType");
        int value = IntegerArgumentType.getInteger(context, "value");

        for (ServerPlayerEntity player : players) {
            addStat(context.getSource(), player, statName, value);
        }
        return players.size();
    }

    // === RESET COMMANDS ===
    private static int resetSelfStats(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return resetStats(context.getSource(), player);
    }

    private static int resetPlayerStats(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            resetStats(context.getSource(), player);
        }
        return players.size();
    }

    // === COST COMMANDS ===
    private static int getSelfStatCost(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String statName = StringArgumentType.getString(context, "statType");
        return showStatCost(context.getSource(), player, statName);
    }

    private static int getPlayerStatCost(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        String statName = StringArgumentType.getString(context, "statType");

        for (ServerPlayerEntity player : players) {
            showStatCost(context.getSource(), player, statName);
        }
        return players.size();
    }

    // === IMPLEMENTATION METHODS ===

    private static int showBasicStats(ServerCommandSource source, ServerPlayerEntity player) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);

        StringBuilder stats = new StringBuilder();
        stats.append(String.format("§6=== %s's Stats ===\n", player.getName().getString()));
        stats.append(String.format("§7Level: §f%d §8| §7Available Points: §e%d\n\n",
                component.getLevel(), component.getAvailableStatPoints()));

        for (StatTypes statType : StatTypes.values()) {
            short value = component.getStatValue(statType);
            int cost = component.getStatCost(statType);
            stats.append(String.format("§7%s: §f%d §8(§7Cost: §e%d§8)\n",
                    statType.getAka(), value, cost));
        }

        Text message = Text.literal(stats.toString());
        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static int showDetailedStats(ServerCommandSource source, ServerPlayerEntity player) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);

        StringBuilder stats = new StringBuilder();
        stats.append(String.format("§6=== %s's Detailed Stats ===\n", player.getName().getString()));
        stats.append(String.format("§7Level: §f%d §8| §7Experience: §f%d§7/§f%d §8(§f%.1f%%§8)\n",
                component.getLevel(), component.getExperience(), component.getExperienceToNextLevel(),
                component.getExperiencePercentage()));
        stats.append(String.format("§7Available Stat Points: §e%d\n\n", component.getAvailableStatPoints()));

        for (StatTypes statType : StatTypes.values()) {
            Stat stat = component.getStatByType(statType);
            stats.append(String.format("§6%s §8(§7%s§8):\n", statType.getName().toUpperCase(), statType.getAka()));
            stats.append(String.format("  §7Current Value: §f%d\n", stat.getCurrentValue()));
            stats.append(String.format("  §7Base Value: §f%d\n", stat.getBaseValue()));
            stats.append(String.format("  §7Points Used: §e%d\n", stat.getTotalPointsUsed()));
            stats.append(String.format("  §7Next Upgrade Cost: §e%d\n\n", stat.getIncreasePerPoint()));
        }

        Text message = Text.literal(stats.toString());
        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static int setStat(ServerCommandSource source, ServerPlayerEntity player, String statName, int value) {
        StatTypes statType = parseStatType(statName);
        if (statType == null) {
            source.sendError(Text.literal(String.format("§cUnknown stat type: %s", statName)));
            return 0;
        }

        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);
        Stat stat = component.getStatByType(statType);

        short oldValue = stat.getCurrentValue();

        component.performBatchUpdate(() -> {
            stat.setPoints((short) value);
            stat.applyPrimaryEffect(player);
            stat.applySecondaryEffect(player);
            component.getPassiveAbilityManager().updatePassiveAbilitiesForStat(statType);
        });
        Text message = Text.literal(String.format(
                "§6Set %s's §e%s §6from §f%d §6to §f%d",
                player.getName().getString(), statType.getAka(), oldValue, value
        ));

        source.sendFeedback(() -> message, true);

        // Notify player
        player.sendMessage(Text.literal(String.format(
                "§6Your §e%s §6has been set to §f%d §6by an administrator",
                statType.getAka(), value
        )).formatted(Formatting.GOLD), false);

        return 1;
    }

    private static int addStat(ServerCommandSource source, ServerPlayerEntity player, String statName, int value) {
        StatTypes statType = parseStatType(statName);
        if (statType == null) {
            source.sendError(Text.literal(String.format("§cUnknown stat type: %s", statName)));
            return 0;
        }

        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);
        Stat stat = component.getStatByType(statType);

        short oldValue = stat.getCurrentValue();
        short newValue = (short) Math.min(oldValue + value, ModConfig.maxStatValue);

        if (newValue == oldValue) {
            source.sendError(Text.literal(String.format(
                    "§c%s's %s is already at maximum (%d)",
                    player.getName().getString(), statType.getAka(), ModConfig.maxStatValue
            )));
            return 0;
        }

        component.performBatchUpdate(() -> {
            stat.add((short) (newValue - oldValue));
            stat.applyPrimaryEffect(player);
            stat.applySecondaryEffect(player);
            component.getPassiveAbilityManager().updatePassiveAbilitiesForStat(statType);
        });

        Text message = Text.literal(String.format(
                "§6Added §f%d §6to %s's §e%s §8(§f%d §6→ §f%d§8)",
                newValue - oldValue, player.getName().getString(), statType.getAka(), oldValue, newValue
        ));

        source.sendFeedback(() -> message, true);

        // Notify player
        player.sendMessage(Text.literal(String.format(
                "§6Your §e%s §6has been increased by §f%d §6by an administrator §8(§f%d §6→ §f%d§8)",
                statType.getAka(), newValue - oldValue, oldValue, newValue
        )).formatted(Formatting.GOLD), false);

        return 1;
    }

    private static int resetStats(ServerCommandSource source, ServerPlayerEntity player) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);

        // Store old values for feedback
        StringBuilder oldStats = new StringBuilder();
        for (StatTypes statType : StatTypes.values()) {
            oldStats.append(String.format("%s: %d, ", statType.getAka(), component.getStatValue(statType)));
        }

        component.resetStatsWithPointReturn();

        Text message = Text.literal(String.format(
                "§6Reset all stats for %s\n§7Previous values: §8%s\n§7All stat points have been returned",
                player.getName().getString(), oldStats.toString().replaceAll(", $", "")
        ));

        source.sendFeedback(() -> message, true);

        // Notify player
        player.sendMessage(Text.literal(
                "§6All your stats have been reset and points returned by an administrator"
        ).formatted(Formatting.GOLD), false);

        return 1;
    }

    private static int showStatCost(ServerCommandSource source, ServerPlayerEntity player, String statName) {
        StatTypes statType = parseStatType(statName);
        if (statType == null) {
            source.sendError(Text.literal(String.format("§cUnknown stat type: %s", statName)));
            return 0;
        }

        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);
        Stat stat = component.getStatByType(statType);

        Text message = Text.literal(String.format(
                "§6%s's §e%s §6upgrade cost:\n" +
                        "§7Current Value: §f%d\n" +
                        "§7Cost for next point: §e%d\n" +
                        "§7Available points: §e%d\n" +
                        "§7Can afford: §%s",
                player.getName().getString(), statType.getAka(),
                stat.getCurrentValue(),
                stat.getIncreasePerPoint(),
                component.getAvailableStatPoints(),
                component.canIncreaseStat(statType) ? "aYes" : "cNo"
        ));

        source.sendFeedback(() -> message, false);
        return 1;
    }
}