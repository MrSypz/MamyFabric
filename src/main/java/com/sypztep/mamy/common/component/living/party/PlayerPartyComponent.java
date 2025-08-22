package com.sypztep.mamy.common.component.living.party;

import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.UUID;

public class PlayerPartyComponent implements AutoSyncedComponent {
    private PlayerEntity player;

    public PlayerPartyComponent(PlayerEntity player) {
        this.player = player;
    }

    private UUID currentPartyId = null;
    private long lastPartyLeaveTime = 0;

    // ===== GETTERS =====
    public UUID getCurrentPartyId() { return currentPartyId; }
    public boolean hasParty() { return currentPartyId != null; }
    public long getLastPartyLeaveTime() { return lastPartyLeaveTime; }

    // ===== PARTY MANAGEMENT =====
    public void joinParty(UUID partyId) {
        this.currentPartyId = partyId;
        // Sync to client
        sync();
    }

    public void leaveParty() {
        this.currentPartyId = null;
        this.lastPartyLeaveTime = System.currentTimeMillis();
        // Sync to client
        sync();
    }

    public boolean canRejoinParty() {
        // Prevent party hopping - 30 second cooldown
        return System.currentTimeMillis() - lastPartyLeaveTime > 30000;
    }

    // ===== NBT SERIALIZATION =====
    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (tag.contains("CurrentPartyId")) {
            currentPartyId = tag.getUuid("CurrentPartyId");
        } else {
            currentPartyId = null;
        }
        lastPartyLeaveTime = tag.getLong("LastPartyLeaveTime");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (currentPartyId != null) {
            tag.putUuid("CurrentPartyId", currentPartyId);
        }
        tag.putLong("LastPartyLeaveTime", lastPartyLeaveTime);
    }
    public void sync() {
        ModEntityComponents.PLAYERPARTY.sync(player);
    }
}