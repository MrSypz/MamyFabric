package com.sypztep.mamy.common.component.living.party;

import com.sypztep.mamy.common.system.party.PartyData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorldPartyComponent implements CommonTickingComponent {
    private World world;

    public WorldPartyComponent(World world) {
        this.world = world;
    }

    private final Map<UUID, PartyData> parties = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerToPartyMap = new ConcurrentHashMap<>(); // Quick lookup
    private int cleanupTicks = 0;

    // ===== PARTY CREATION =====
    public PartyData createParty(String partyName, ServerPlayerEntity leader) {
        UUID partyId = UUID.randomUUID();
        UUID leaderId = leader.getUuid();

        PartyData party = new PartyData(partyId, partyName, leaderId);
        parties.put(partyId, party);
        playerToPartyMap.put(leaderId, partyId);

        return party;
    }

    // ===== PARTY MANAGEMENT =====
    public boolean disbandParty(UUID partyId) {
        PartyData party = parties.remove(partyId);
        if (party != null) {
            // Remove all members from lookup map
            for (UUID memberId : party.getMembers()) {
                playerToPartyMap.remove(memberId);
            }
            return true;
        }
        return false;
    }

    public boolean joinParty(UUID partyId, ServerPlayerEntity player) {
        PartyData party = parties.get(partyId);
        if (party != null && party.addMember(player.getUuid())) {
            playerToPartyMap.put(player.getUuid(), partyId);
            return true;
        }
        return false;
    }

    public boolean leaveParty(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        UUID partyId = playerToPartyMap.get(playerId);

        if (partyId != null) {
            PartyData party = parties.get(partyId);
            if (party != null) {
                party.removeMember(playerId);
                playerToPartyMap.remove(playerId);

                // Disband if empty
                if (party.isEmpty()) {
                    disbandParty(partyId);
                }
                return true;
            }
        }
        return false;
    }

    // ===== GETTERS =====
    public PartyData getParty(UUID partyId) {
        return parties.get(partyId);
    }

    public PartyData getPlayerParty(UUID playerId) {
        UUID partyId = playerToPartyMap.get(playerId);
        return partyId != null ? parties.get(partyId) : null;
    }

    public Collection<PartyData> getAllParties() {
        return new ArrayList<>(parties.values());
    }

    public List<PartyData> getPublicParties() {
        return parties.values().stream()
                .filter(PartyData::isPublic)
                .toList();
    }

    // ===== INVITATIONS =====
    public boolean inviteToParty(UUID partyId, UUID targetPlayerId) {
        PartyData party = parties.get(partyId);
        return party != null && party.invitePlayer(targetPlayerId);
    }

    public boolean acceptInvite(UUID partyId, ServerPlayerEntity player) {
        PartyData party = parties.get(partyId);
        if (party != null && party.isInvited(player.getUuid())) {
            return joinParty(partyId, player);
        }
        return false;
    }

    public void declineInvite(UUID partyId, UUID playerId) {
        PartyData party = parties.get(partyId);
        if (party != null) {
            party.removeInvite(playerId);
        }
    }

    // ===== TICKING =====
    @Override
    public void tick() {
        cleanupTicks++;

        // Cleanup every 20 seconds (400 ticks)
        if (cleanupTicks >= 400) {
            cleanupTicks = 0;
            cleanupExpiredInvites();
            cleanupEmptyParties();
        }
    }

    private void cleanupExpiredInvites() {
        for (PartyData party : parties.values()) {
            party.cleanupExpiredInvites();
        }
    }

    private void cleanupEmptyParties() {
        List<UUID> emptyParties = new ArrayList<>();
        for (Map.Entry<UUID, PartyData> entry : parties.entrySet()) {
            if (entry.getValue().isEmpty()) {
                emptyParties.add(entry.getKey());
            }
        }

        for (UUID partyId : emptyParties) {
            disbandParty(partyId);
        }
    }

    // ===== NBT SERIALIZATION =====
    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        parties.clear();
        playerToPartyMap.clear();

        if (tag.contains("Parties")) {
            NbtList partiesList = tag.getList("Parties", 10);
            for (int i = 0; i < partiesList.size(); i++) {
                NbtCompound partyNbt = partiesList.getCompound(i);
                PartyData party = PartyData.readFromNbt(partyNbt);
                parties.put(party.getPartyId(), party);

                // Rebuild player lookup map
                for (UUID memberId : party.getMembers()) {
                    playerToPartyMap.put(memberId, party.getPartyId());
                }
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList partiesList = new NbtList();
        for (PartyData party : parties.values()) {
            NbtCompound partyNbt = new NbtCompound();
            party.writeToNbt(partyNbt);
            partiesList.add(partyNbt);
        }
        tag.put("Parties", partiesList);
    }
}