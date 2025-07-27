package com.sypztep.mamy.common.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

public class ExpCommand {

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("experience")
                // /experience set <amount> [player]
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ExpCommand::setSelfExperience)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .requires(source -> source.hasPermissionLevel(2))
                                        .executes(ExpCommand::setPlayerExperience)
                                )
                        )
                )
                // /experience add <amount> [player]
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ExpCommand::addSelfExperience)
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .requires(source -> source.hasPermissionLevel(2))
                                        .executes(ExpCommand::addPlayerExperience)
                                )
                        )
                );
    }

    private static int setSelfExperience(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        return setExperience(context.getSource(), player, amount);
    }

    private static int setPlayerExperience(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        for (ServerPlayerEntity player : players) {
            setExperience(context.getSource(), player, amount);
        }

        return players.size();
    }

    private static int addSelfExperience(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        return addExperience(context.getSource(), player, amount);
    }

    private static int addPlayerExperience(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        for (ServerPlayerEntity player : players) {
            addExperience(context.getSource(), player, amount);
        }

        return players.size();
    }

    private static int setExperience(ServerCommandSource source, ServerPlayerEntity player, int amount) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);

        component.performBatchUpdate(() -> {
            component.getLevelSystem().setExperience(amount);
        });

        Text message = Text.literal(String.format(
                "§6Set %s's experience to §f%d",
                player.getName().getString(), amount
        ));

        source.sendFeedback(() -> message, true);
        return 1;
    }

    private static int addExperience(ServerCommandSource source, ServerPlayerEntity player, int amount) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);

        int oldLevel = component.getLevel();
        component.addExperience(amount);
        int newLevel = component.getLevel();

        Text message;
        if (newLevel > oldLevel) {
            message = Text.literal(String.format(
                    "§6Added §f%d §6experience to %s - Leveled up to §f%d§6!",
                    amount, player.getName().getString(), newLevel
            ));
        } else {
            message = Text.literal(String.format(
                    "§6Added §f%d §6experience to %s",
                    amount, player.getName().getString()
            ));
        }
        source.sendFeedback(() -> message, true);

        return 1;
    }
}