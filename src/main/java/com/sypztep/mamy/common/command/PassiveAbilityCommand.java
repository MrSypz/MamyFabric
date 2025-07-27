package com.sypztep.mamy.common.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PassiveAbilityCommand {

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("passive")
                // /passive list [player]
                .then(CommandManager.literal("list")
                        .executes(PassiveAbilityCommand::listSelfPassives)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(PassiveAbilityCommand::listPlayerPassives)
                        )
                )
                // /passive info <ability> [player]
                .then(CommandManager.literal("info")
                        .then(CommandManager.argument("ability", StringArgumentType.word())
                                .suggests(PassiveAbilityCommand::suggestPassiveAbilities)
                                .executes(PassiveAbilityCommand::infoSelfPassive)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .requires(source -> source.hasPermissionLevel(2))
                                        .executes(PassiveAbilityCommand::infoPlayerPassive)
                                )
                        )
                )
                // /passive unlock <ability> [player]
                .then(CommandManager.literal("unlock")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("ability", StringArgumentType.word())
                                .suggests(PassiveAbilityCommand::suggestPassiveAbilities)
                                .executes(PassiveAbilityCommand::unlockSelfPassive)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PassiveAbilityCommand::unlockPlayerPassive)
                                )
                        )
                )
                // /passive lock <ability> [player]
                .then(CommandManager.literal("lock")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("ability", StringArgumentType.word())
                                .suggests(PassiveAbilityCommand::suggestPassiveAbilities)
                                .executes(PassiveAbilityCommand::lockSelfPassive)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(PassiveAbilityCommand::lockPlayerPassive)
                                )
                        )
                )
                // /passive reset [player]
                .then(CommandManager.literal("reset")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(PassiveAbilityCommand::resetSelfPassives)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .executes(PassiveAbilityCommand::resetPlayerPassives)
                        )
                )
                // /passive check <ability> [player] - Check if player meets requirements
                .then(CommandManager.literal("check")
                        .then(CommandManager.argument("ability", StringArgumentType.word())
                                .suggests(PassiveAbilityCommand::suggestPassiveAbilities)
                                .executes(PassiveAbilityCommand::checkSelfPassive)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .requires(source -> source.hasPermissionLevel(2))
                                        .executes(PassiveAbilityCommand::checkPlayerPassive)
                                )
                        )
                )
                // /passive update [player] - Force update passive abilities
                .then(CommandManager.literal("update")
                        .executes(PassiveAbilityCommand::updateSelfPassives)
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(PassiveAbilityCommand::updatePlayerPassives)
                        )
                );
    }

    // Suggest passive abilities for auto-completion
    private static CompletableFuture<Suggestions> suggestPassiveAbilities(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        ModPassiveAbilities.getAllAbilityIds().forEach(builder::suggest);
        return builder.buildFuture();
    }

    // Parse passive ability from string
    private static PassiveAbility parsePassiveAbility(String abilityName) {
        return ModPassiveAbilities.getAbilityByName(abilityName);
    }

    // === LIST COMMANDS ===
    private static int listSelfPassives(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return listPassives(context.getSource(), player);
    }

    private static int listPlayerPassives(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            listPassives(context.getSource(), player);
        }
        return players.size();
    }

    // === INFO COMMANDS ===
    private static int infoSelfPassive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String abilityName = StringArgumentType.getString(context, "ability");
        return showPassiveInfo(context.getSource(), player, abilityName);
    }

    private static int infoPlayerPassive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        String abilityName = StringArgumentType.getString(context, "ability");

        for (ServerPlayerEntity player : players) {
            showPassiveInfo(context.getSource(), player, abilityName);
        }
        return players.size();
    }

    // === UNLOCK COMMANDS ===
    private static int unlockSelfPassive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String abilityName = StringArgumentType.getString(context, "ability");
        return unlockPassive(context.getSource(), player, abilityName);
    }

    private static int unlockPlayerPassive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        String abilityName = StringArgumentType.getString(context, "ability");

        for (ServerPlayerEntity player : players) {
            unlockPassive(context.getSource(), player, abilityName);
        }
        return players.size();
    }

    // === LOCK COMMANDS ===
    private static int lockSelfPassive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String abilityName = StringArgumentType.getString(context, "ability");
        return lockPassive(context.getSource(), player, abilityName);
    }

    private static int lockPlayerPassive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        String abilityName = StringArgumentType.getString(context, "ability");

        for (ServerPlayerEntity player : players) {
            lockPassive(context.getSource(), player, abilityName);
        }
        return players.size();
    }

    // === RESET COMMANDS ===
    private static int resetSelfPassives(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return resetPassives(context.getSource(), player);
    }

    private static int resetPlayerPassives(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            resetPassives(context.getSource(), player);
        }
        return players.size();
    }

    // === CHECK COMMANDS ===
    private static int checkSelfPassive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String abilityName = StringArgumentType.getString(context, "ability");
        return checkPassiveRequirements(context.getSource(), player, abilityName);
    }

    private static int checkPlayerPassive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        String abilityName = StringArgumentType.getString(context, "ability");

        for (ServerPlayerEntity player : players) {
            checkPassiveRequirements(context.getSource(), player, abilityName);
        }
        return players.size();
    }

    // === UPDATE COMMANDS ===
    private static int updateSelfPassives(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return updatePassives(context.getSource(), player);
    }

    private static int updatePlayerPassives(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        for (ServerPlayerEntity player : players) {
            updatePassives(context.getSource(), player);
        }
        return players.size();
    }

    // === IMPLEMENTATION METHODS ===

    private static int listPassives(ServerCommandSource source, ServerPlayerEntity player) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);
        PassiveAbilityManager manager = component.getPassiveAbilityManager();

        if (manager == null) {
            source.sendError(Text.literal("§cPlayer does not have passive ability manager"));
            return 0;
        }

        var unlockedAbilities = manager.getUnlockedAbilities();
        var activeAbilities = manager.getActiveAbilities();

        StringBuilder message = new StringBuilder();
        message.append(String.format("§6=== %s's Passive Abilities ===\n", player.getName().getString()));
        message.append(String.format("§7Unlocked: §e%d §8| §7Active: §a%d\n\n",
                unlockedAbilities.size(), activeAbilities.size()));

        if (unlockedAbilities.isEmpty()) {
            message.append("§7No passive abilities unlocked yet.\n");
        } else {
            message.append("§6Unlocked Abilities:\n");
            for (PassiveAbility ability : unlockedAbilities) {
                boolean isActive = manager.isActive(ability);
                String status = isActive ? "§a✓ Active" : "§c✗ Inactive";
                message.append(String.format("  §e%s §8- %s\n",
                        ability.getDisplayName().getString(), status));
            }
        }

        Text finalMessage = Text.literal(message.toString());
        source.sendFeedback(() -> finalMessage, false);
        return 1;
    }

    private static int showPassiveInfo(ServerCommandSource source, ServerPlayerEntity player, String abilityName) {
        PassiveAbility ability = parsePassiveAbility(abilityName);
        if (ability == null) {
            source.sendError(Text.literal(String.format("§cUnknown passive ability: %s", abilityName)));
            return 0;
        }

        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);
        PassiveAbilityManager manager = component.getPassiveAbilityManager();

        if (manager == null) {
            source.sendError(Text.literal("§cPlayer does not have passive ability manager"));
            return 0;
        }

        boolean unlocked = manager.isUnlocked(ability);
        boolean active = manager.isActive(ability);
        boolean meetsRequirements = ability.meetsRequirements(player);

        StringBuilder info = new StringBuilder();
        info.append(String.format("§6=== %s's %s ===\n",
                player.getName().getString(), ability.getDisplayName().getString()));

        info.append(String.format("§7Status: %s\n",
                unlocked ? (active ? "§a✓ Unlocked & Active" : "§e⚠ Unlocked but Inactive") : "§c✗ Locked"));

        info.append(String.format("§7Requirements Met: %s\n\n",
                meetsRequirements ? "§a✓ Yes" : "§c✗ No"));

        // Add ability description and requirements
        var tooltip = ability.getTooltip(player);
        for (Text line : tooltip) {
            info.append("§7").append(line.getString()).append("\n");
        }

        Text message = Text.literal(info.toString());
        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static int unlockPassive(ServerCommandSource source, ServerPlayerEntity player, String abilityName) {
        PassiveAbility ability = parsePassiveAbility(abilityName);
        if (ability == null) {
            source.sendError(Text.literal(String.format("§cUnknown passive ability: %s", abilityName)));
            return 0;
        }

        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);
        PassiveAbilityManager manager = component.getPassiveAbilityManager();

        if (manager == null) {
            source.sendError(Text.literal("§cPlayer does not have passive ability manager"));
            return 0;
        }

        component.performBatchUpdate(() -> manager.forceUnlock(ability));

        Text message = Text.literal(String.format(
                "§6Force unlocked §e%s §6for %s",
                ability.getDisplayName().getString(), player.getName().getString()
        ));

        source.sendFeedback(() -> message, true);

        // Notify player
        player.sendMessage(Text.literal(String.format(
                "§6Passive ability §e%s §6has been unlocked by an administrator!",
                ability.getDisplayName().getString()
        )).formatted(Formatting.GOLD), false);

        return 1;
    }

    private static int lockPassive(ServerCommandSource source, ServerPlayerEntity player, String abilityName) {
        PassiveAbility ability = parsePassiveAbility(abilityName);
        if (ability == null) {
            source.sendError(Text.literal(String.format("§cUnknown passive ability: %s", abilityName)));
            return 0;
        }

        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);
        PassiveAbilityManager manager = component.getPassiveAbilityManager();

        if (manager == null) {
            source.sendError(Text.literal("§cPlayer does not have passive ability manager"));
            return 0;
        }

        component.performBatchUpdate(() -> manager.forceLock(ability));

        Text message = Text.literal(String.format(
                "§6Force locked §e%s §6for %s",
                ability.getDisplayName().getString(), player.getName().getString()
        ));

        source.sendFeedback(() -> message, true);

        // Notify player
        player.sendMessage(Text.literal(String.format(
                "§cPassive ability §e%s §chas been locked by an administrator",
                ability.getDisplayName().getString()
        )).formatted(Formatting.RED), false);

        return 1;
    }

    private static int resetPassives(ServerCommandSource source, ServerPlayerEntity player) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);
        PassiveAbilityManager manager = component.getPassiveAbilityManager();

        if (manager == null) {
            source.sendError(Text.literal("§cPlayer does not have passive ability manager"));
            return 0;
        }

        int unlockedCount = manager.getUnlockedAbilities().size();

        component.performBatchUpdate(() -> manager.resetAll());

        Text message = Text.literal(String.format(
                "§6Reset all passive abilities for %s §7(§e%d §7abilities removed)",
                player.getName().getString(), unlockedCount
        ));

        source.sendFeedback(() -> message, true);

        // Notify player
        player.sendMessage(Text.literal(
                "§cAll your passive abilities have been reset by an administrator"
        ).formatted(Formatting.RED), false);

        return 1;
    }

    private static int checkPassiveRequirements(ServerCommandSource source, ServerPlayerEntity player, String abilityName) {
        PassiveAbility ability = parsePassiveAbility(abilityName);
        if (ability == null) {
            source.sendError(Text.literal(String.format("§cUnknown passive ability: %s", abilityName)));
            return 0;
        }

        boolean meetsRequirements = ability.meetsRequirements(player);

        StringBuilder check = new StringBuilder();
        check.append(String.format("§6=== Requirement Check: %s ===\n", ability.getDisplayName().getString()));
        check.append(String.format("§7Player: §f%s\n", player.getName().getString()));
        check.append(String.format("§7Result: %s\n\n",
                meetsRequirements ? "§a✓ Requirements Met" : "§c✗ Requirements Not Met"));

        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);
        for (var entry : ability.getRequirements().entrySet()) {
            var statType = entry.getKey();
            int required = entry.getValue();
            int current = component.getStatValue(statType);

            String status = current >= required ? "§a✓" : "§c✗";
            check.append(String.format("  %s §7%s: §f%d§7/§e%d\n",
                    status, statType.getAka(), current, required));
        }

        Text message = Text.literal(check.toString());
        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static int updatePassives(ServerCommandSource source, ServerPlayerEntity player) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);
        PassiveAbilityManager manager = component.getPassiveAbilityManager();

        if (manager == null) {
            source.sendError(Text.literal("§cPlayer does not have passive ability manager"));
            return 0;
        }

        int beforeUnlocked = manager.getUnlockedAbilities().size();
        int beforeActive = manager.getActiveAbilities().size();

        component.updatePassiveAbilities();

        int afterUnlocked = manager.getUnlockedAbilities().size();
        int afterActive = manager.getActiveAbilities().size();

        Text message = Text.literal(String.format(
                "§6Updated passive abilities for %s\n" +
                        "§7Unlocked: §e%d §7→ §e%d §8| §7Active: §a%d §7→ §a%d",
                player.getName().getString(),
                beforeUnlocked, afterUnlocked, beforeActive, afterActive
        ));

        source.sendFeedback(() -> message, true);
        return 1;
    }
}