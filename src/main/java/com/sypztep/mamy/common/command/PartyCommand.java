package com.sypztep.mamy.common.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.sypztep.mamy.common.component.living.party.WorldPartyComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModWorldComponents;
import com.sypztep.mamy.common.system.party.PartyData;
import com.sypztep.mamy.common.system.party.PartyManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.UUID;

public class PartyCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("party")
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                .executes(PartyCommand::createParty)))
                .then(CommandManager.literal("join")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(PartyCommand::joinPartyByPlayer)))
                .then(CommandManager.literal("leave")
                        .executes(PartyCommand::leaveParty))
                .then(CommandManager.literal("invite")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(PartyCommand::invitePlayer)))
                .then(CommandManager.literal("accept")
                        .executes(PartyCommand::acceptInvite))
                .then(CommandManager.literal("decline")
                        .executes(PartyCommand::declineInvite))
                .then(CommandManager.literal("info")
                        .executes(PartyCommand::partyInfo))
                .then(CommandManager.literal("list")
                        .executes(PartyCommand::listPublicParties))
                .then(CommandManager.literal("disband")
                        .executes(PartyCommand::disbandParty))
                .then(CommandManager.literal("kick")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(PartyCommand::kickPlayer)))
                .then(CommandManager.literal("leader")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(PartyCommand::transferLeadership)))
                .executes(PartyCommand::partyHelp);
    }

    private static int createParty(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            String partyName = StringArgumentType.getString(context, "name");

            if (partyName.length() > 32) {
                player.sendMessage(Text.literal("Party name too long! (Max 32 characters)").formatted(Formatting.RED), false);
                return 0;
            }

            PartyManager.createParty(player, partyName);
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to create party: " + e.getMessage()));
            return 0;
        }
    }

    private static int joinPartyByPlayer(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

            PartyData targetParty = PartyManager.getPlayerParty(target);
            if (targetParty == null) {
                player.sendMessage(Text.literal(target.getName().getString() + " is not in a party!").formatted(Formatting.RED), false);
                return 0;
            }

            if (!targetParty.isPublic()) {
                player.sendMessage(Text.literal("That party is private!").formatted(Formatting.RED), false);
                return 0;
            }

            PartyManager.joinParty(player, targetParty.getPartyId());
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to join party: " + e.getMessage()));
            return 0;
        }
    }

    private static int leaveParty(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            PartyManager.leaveParty(player);
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to leave party: " + e.getMessage()));
            return 0;
        }
    }

    private static int invitePlayer(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

            PartyManager.inviteToParty(player, target);
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to invite player: " + e.getMessage()));
            return 0;
        }
    }

    private static int acceptInvite(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

            // Find the first party invitation for this player
            WorldPartyComponent worldParty = ModWorldComponents.WORLDPARTY.get(player.getServerWorld());

            for (PartyData party : worldParty.getAllParties()) {
                if (party.isInvited(player.getUuid())) {
                    if (worldParty.acceptInvite(party.getPartyId(), player)) {
                        ModEntityComponents.PLAYERPARTY.get(player).joinParty(party.getPartyId());

                        player.sendMessage(Text.literal("Joined party ").formatted(Formatting.GREEN)
                                .append(party.getFormattedName())
                                .append(Text.literal("!").formatted(Formatting.GREEN)), false);

                        return 1;
                    }
                }
            }

            player.sendMessage(Text.literal("No pending party invitations!").formatted(Formatting.RED), false);
            return 0;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to accept invite: " + e.getMessage()));
            return 0;
        }
    }

    private static int declineInvite(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

            WorldPartyComponent worldParty = ModWorldComponents.WORLDPARTY.get(player.getServerWorld());
            boolean foundInvite = false;

            for (PartyData party : worldParty.getAllParties()) {
                if (party.isInvited(player.getUuid())) {
                    worldParty.declineInvite(party.getPartyId(), player.getUuid());
                    player.sendMessage(Text.literal("Declined invitation to ").formatted(Formatting.YELLOW)
                            .append(party.getFormattedName()), false);
                    foundInvite = true;
                    break;
                }
            }

            if (!foundInvite) {
                player.sendMessage(Text.literal("No pending party invitations!").formatted(Formatting.RED), false);
            }

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to decline invite: " + e.getMessage()));
            return 0;
        }
    }

    private static int partyInfo(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            PartyData party = PartyManager.getPlayerParty(player);

            if (party == null) {
                player.sendMessage(Text.literal("You are not in a party!").formatted(Formatting.RED), false);
                return 0;
            }

            player.sendMessage(Text.literal("=== Party Info ===").formatted(Formatting.GOLD), false);
            player.sendMessage(Text.literal("Name: ").formatted(Formatting.GRAY)
                    .append(party.getFormattedName()), false);
            player.sendMessage(Text.literal("Members: " + party.getMemberCount() + "/" + party.getMaxMembers()).formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("Leader: ").formatted(Formatting.GRAY)
                    .append(getPlayerName(player.getServer(), party.getLeaderId())), false);

            if (!party.getDescription().isEmpty()) {
                player.sendMessage(Text.literal("Description: " + party.getDescription()).formatted(Formatting.GRAY), false);
            }

            // List members
            player.sendMessage(Text.literal("Members:").formatted(Formatting.YELLOW), false);
            for (UUID memberId : party.getMembers()) {
                String memberName = getPlayerName(player.getServer(), memberId).getString();
                boolean isLeader = party.isLeader(memberId);
                player.sendMessage(Text.literal("  - " + memberName + (isLeader ? " (Leader)" : "")).formatted(Formatting.WHITE), false);
            }

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to show party info: " + e.getMessage()));
            return 0;
        }
    }

    private static int listPublicParties(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            List<PartyData> publicParties = PartyManager.getPublicParties(player);

            if (publicParties.isEmpty()) {
                player.sendMessage(Text.literal("No public parties available!").formatted(Formatting.YELLOW), false);
                return 0;
            }

            player.sendMessage(Text.literal("=== Public Parties ===").formatted(Formatting.GOLD), false);
            for (PartyData party : publicParties) {
                String leaderName = getPlayerName(player.getServer(), party.getLeaderId()).getString();
                player.sendMessage(Text.literal("• ").formatted(Formatting.GRAY)
                        .append(party.getFormattedName())
                        .append(Text.literal(" (" + party.getMemberCount() + "/" + party.getMaxMembers() + ") - " + leaderName).formatted(Formatting.GRAY)), false);
            }

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to list parties: " + e.getMessage()));
            return 0;
        }
    }

    private static int disbandParty(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            PartyData party = PartyManager.getPlayerParty(player);

            if (party == null) {
                player.sendMessage(Text.literal("You are not in a party!").formatted(Formatting.RED), false);
                return 0;
            }

            if (!party.isLeader(player.getUuid())) {
                player.sendMessage(Text.literal("Only the party leader can disband the party!").formatted(Formatting.RED), false);
                return 0;
            }

            WorldPartyComponent worldParty = ModWorldComponents.WORLDPARTY.get(player.getServerWorld());

            // Notify all members
            for (UUID memberId : party.getMembers()) {
                ServerPlayerEntity member = player.getServer().getPlayerManager().getPlayer(memberId);
                if (member != null) {
                    ModEntityComponents.PLAYERPARTY.get(member).leaveParty();
                    member.sendMessage(Text.literal("Party has been disbanded!").formatted(Formatting.RED), false);
                }
            }

            worldParty.disbandParty(party.getPartyId());
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to disband party: " + e.getMessage()));
            return 0;
        }
    }

    private static int kickPlayer(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

            PartyData party = PartyManager.getPlayerParty(player);
            if (party == null) {
                player.sendMessage(Text.literal("You are not in a party!").formatted(Formatting.RED), false);
                return 0;
            }

            if (!party.isLeader(player.getUuid())) {
                player.sendMessage(Text.literal("Only the party leader can kick members!").formatted(Formatting.RED), false);
                return 0;
            }

            if (!party.isMember(target.getUuid())) {
                player.sendMessage(Text.literal(target.getName().getString() + " is not in your party!").formatted(Formatting.RED), false);
                return 0;
            }

            if (party.isLeader(target.getUuid())) {
                player.sendMessage(Text.literal("You cannot kick yourself!").formatted(Formatting.RED), false);
                return 0;
            }

            WorldPartyComponent worldParty = ModWorldComponents.WORLDPARTY.get(player.getServerWorld());
            worldParty.leaveParty(target);
            ModEntityComponents.PLAYERPARTY.get(target).leaveParty();

            // Notify players
            player.sendMessage(Text.literal("Kicked " + target.getName().getString() + " from the party!").formatted(Formatting.YELLOW), false);
            target.sendMessage(Text.literal("You were kicked from the party!").formatted(Formatting.RED), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to kick player: " + e.getMessage()));
            return 0;
        }
    }

    private static int transferLeadership(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

            PartyData party = PartyManager.getPlayerParty(player);
            if (party == null) {
                player.sendMessage(Text.literal("You are not in a party!").formatted(Formatting.RED), false);
                return 0;
            }

            if (!party.isLeader(player.getUuid())) {
                player.sendMessage(Text.literal("Only the party leader can transfer leadership!").formatted(Formatting.RED), false);
                return 0;
            }

            if (!party.isMember(target.getUuid())) {
                player.sendMessage(Text.literal(target.getName().getString() + " is not in your party!").formatted(Formatting.RED), false);
                return 0;
            }

            party.transferLeadership(target.getUuid());

            // Notify party
            for (UUID memberId : party.getMembers()) {
                ServerPlayerEntity member = player.getServer().getPlayerManager().getPlayer(memberId);
                if (member != null) {
                    member.sendMessage(Text.literal(target.getName().getString() + " is now the party leader!").formatted(Formatting.GOLD), false);
                }
            }

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to transfer leadership: " + e.getMessage()));
            return 0;
        }
    }

    private static int partyHelp(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

            player.sendMessage(Text.literal("=== Party Commands ===").formatted(Formatting.GOLD), false);
            player.sendMessage(Text.literal("/mamy party create <name> - Create a new party").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("/mamy party join <player> - Join a player's public party").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("/mamy party leave - Leave your current party").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("/mamy party invite <player> - Invite a player to your party").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("/mamy party accept - Accept a party invitation").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("/mamy party decline - Decline a party invitation").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("/mamy party info - Show party information").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("/mamy party list - List public parties").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("/mamy party disband - Disband your party (leader only)").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("/mamy party kick <player> - Kick a player (leader only)").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("/mamy party leader <player> - Transfer leadership (leader only)").formatted(Formatting.GRAY), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to show help: " + e.getMessage()));
            return 0;
        }
    }

    private static Text getPlayerName(net.minecraft.server.MinecraftServer server, UUID playerId) {
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
        if (player != null) {
            return player.getName();
        }
        return Text.literal("Unknown Player");
    }
}
