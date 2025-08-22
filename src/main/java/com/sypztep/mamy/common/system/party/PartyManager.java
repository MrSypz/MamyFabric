package com.sypztep.mamy.common.system.party;

import com.sypztep.mamy.common.component.living.party.PlayerPartyComponent;
import com.sypztep.mamy.common.component.living.party.WorldPartyComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModWorldComponents;
import com.sypztep.mamy.common.system.skill.novice.BasicSkill;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.UUID;

public class PartyManager {

    // ===== PARTY CREATION =====
    public static boolean createParty(ServerPlayerEntity leader, String partyName) {
        // Check Basic Skill requirement
        if (!BasicSkill.canJoinParty(leader)) {
            leader.sendMessage(Text.literal("You need Basic Skill Level 5 to create a party!").formatted(Formatting.RED), false);
            return false;
        }

        PlayerPartyComponent playerParty = ModEntityComponents.PLAYERPARTY.get(leader);

        // Check if already in a party
        if (playerParty.hasParty()) {
            leader.sendMessage(Text.literal("You are already in a party!").formatted(Formatting.RED), false);
            return false;
        }

        // Check cooldown
        if (!playerParty.canRejoinParty()) {
            leader.sendMessage(Text.literal("You must wait before creating/joining another party!").formatted(Formatting.RED), false);
            return false;
        }

        WorldPartyComponent worldParty = ModWorldComponents.WORLDPARTY.get(leader.getServerWorld());
        PartyData party = worldParty.createParty(partyName, leader);

        // Update player component
        playerParty.joinParty(party.getPartyId());

        leader.sendMessage(Text.literal("Party '").formatted(Formatting.GREEN)
                .append(party.getFormattedName())
                .append(Text.literal("' created!").formatted(Formatting.GREEN)), false);

        return true;
    }

    // ===== PARTY JOINING =====
    public static boolean joinParty(ServerPlayerEntity player, UUID partyId) {
        // Check Basic Skill requirement
        if (!BasicSkill.canJoinParty(player)) {
            player.sendMessage(Text.literal("You need Basic Skill Level 5 to join a party!").formatted(Formatting.RED), false);
            return false;
        }

        PlayerPartyComponent playerParty = ModEntityComponents.PLAYERPARTY.get(player);

        // Check if already in a party
        if (playerParty.hasParty()) {
            player.sendMessage(Text.literal("You are already in a party!").formatted(Formatting.RED), false);
            return false;
        }

        // Check cooldown
        if (!playerParty.canRejoinParty()) {
            player.sendMessage(Text.literal("You must wait before joining another party!").formatted(Formatting.RED), false);
            return false;
        }

        WorldPartyComponent worldParty = ModWorldComponents.WORLDPARTY.get(player.getServerWorld());
        PartyData party = worldParty.getParty(partyId);

        if (party == null) {
            player.sendMessage(Text.literal("Party not found!").formatted(Formatting.RED), false);
            return false;
        }

        if (!party.canJoin()) {
            player.sendMessage(Text.literal("Party is full!").formatted(Formatting.RED), false);
            return false;
        }

        if (worldParty.joinParty(partyId, player)) {
            playerParty.joinParty(partyId);

            // Notify player
            player.sendMessage(Text.literal("Joined party ").formatted(Formatting.GREEN)
                    .append(party.getFormattedName())
                    .append(Text.literal("!").formatted(Formatting.GREEN)), false);

            // Notify party members
            notifyPartyMembers(party, player.getServerWorld(),
                    Text.literal(player.getName().getString() + " joined the party!").formatted(Formatting.YELLOW));

            return true;
        }

        return false;
    }

    // ===== PARTY LEAVING =====
    public static boolean leaveParty(ServerPlayerEntity player) {
        PlayerPartyComponent playerParty = ModEntityComponents.PLAYERPARTY.get(player);

        if (!playerParty.hasParty()) {
            player.sendMessage(Text.literal("You are not in a party!").formatted(Formatting.RED), false);
            return false;
        }

        WorldPartyComponent worldParty = ModWorldComponents.WORLDPARTY.get(player.getServerWorld());
        PartyData party = worldParty.getPlayerParty(player.getUuid());

        if (party != null) {
            // Notify party members before leaving
            notifyPartyMembers(party, player.getServerWorld(),
                    Text.literal(player.getName().getString() + " left the party!").formatted(Formatting.YELLOW));
        }

        if (worldParty.leaveParty(player)) {
            playerParty.leaveParty();

            player.sendMessage(Text.literal("Left the party!").formatted(Formatting.GREEN), false);
            return true;
        }

        return false;
    }

    // ===== INVITATIONS =====
    public static boolean inviteToParty(ServerPlayerEntity inviter, ServerPlayerEntity target) {
        PlayerPartyComponent inviterParty = ModEntityComponents.PLAYERPARTY.get(inviter);
        PlayerPartyComponent targetParty = ModEntityComponents.PLAYERPARTY.get(target);

        // Check if inviter has a party
        if (!inviterParty.hasParty()) {
            inviter.sendMessage(Text.literal("You are not in a party!").formatted(Formatting.RED), false);
            return false;
        }

        // Check if target is already in a party
        if (targetParty.hasParty()) {
            inviter.sendMessage(Text.literal(target.getName().getString() + " is already in a party!").formatted(Formatting.RED), false);
            return false;
        }

        // Check Basic Skill requirement for target
        if (!BasicSkill.canJoinParty(target)) {
            inviter.sendMessage(Text.literal(target.getName().getString() + " needs Basic Skill Level 5 to join parties!").formatted(Formatting.RED), false);
            return false;
        }

        WorldPartyComponent worldParty = ModWorldComponents.WORLDPARTY.get(inviter.getServerWorld());
        PartyData party = worldParty.getPlayerParty(inviter.getUuid());

        if (party == null) {
            inviter.sendMessage(Text.literal("Party not found!").formatted(Formatting.RED), false);
            return false;
        }

        // Check if inviter can invite (leader or party allows invites)
        if (!party.isLeader(inviter.getUuid()) && !party.allowsInvites()) {
            inviter.sendMessage(Text.literal("Only the party leader can invite players!").formatted(Formatting.RED), false);
            return false;
        }

        if (worldParty.inviteToParty(party.getPartyId(), target.getUuid())) {
            // Notify inviter
            inviter.sendMessage(Text.literal("Invited " + target.getName().getString() + " to the party!").formatted(Formatting.GREEN), false);

            // Notify target
            target.sendMessage(Text.literal("You've been invited to join ").formatted(Formatting.YELLOW)
                    .append(party.getFormattedName())
                    .append(Text.literal(" by " + inviter.getName().getString() + "!").formatted(Formatting.YELLOW)), false);
            target.sendMessage(Text.literal("Use /party accept to join or /party decline to refuse.").formatted(Formatting.GRAY), false);

            return true;
        }

        return false;
    }

    // ===== UTILITY =====
    public static PartyData getPlayerParty(ServerPlayerEntity player) {
        WorldPartyComponent worldParty = ModWorldComponents.WORLDPARTY.get(player.getServerWorld());
        return worldParty.getPlayerParty(player.getUuid());
    }

    public static List<PartyData> getPublicParties(ServerPlayerEntity player) {
        WorldPartyComponent worldParty = ModWorldComponents.WORLDPARTY.get(player.getServerWorld());
        return worldParty.getPublicParties();
    }

    private static void notifyPartyMembers(PartyData party, net.minecraft.server.world.ServerWorld world, Text message) {
        for (UUID memberId : party.getMembers()) {
            ServerPlayerEntity member = world.getServer().getPlayerManager().getPlayer(memberId);
            if (member != null) {
                member.sendMessage(message, false);
            }
        }
    }
}
