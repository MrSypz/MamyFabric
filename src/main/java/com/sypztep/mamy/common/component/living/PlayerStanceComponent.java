package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public class PlayerStanceComponent implements AutoSyncedComponent, CommonTickingComponent {
    private final PlayerEntity player;
    private boolean isInCombatStance = false;
    private int stanceCooldown = 0;
    private static final int STANCE_TOGGLE_COOLDOWN = 20; // 1 second

    public PlayerStanceComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        isInCombatStance = tag.getBoolean("CombatStance");
        stanceCooldown = tag.getInt("StanceCooldown");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean("CombatStance", isInCombatStance);
        tag.putInt("StanceCooldown", stanceCooldown);
    }

    @Override
    public void tick() {
        if (stanceCooldown > 0) {
            stanceCooldown--;
        }
    }

    public boolean isInCombatStance() {
        return isInCombatStance;
    }

    public boolean toggleStance() {
        if (stanceCooldown > 0) return false;

        isInCombatStance = !isInCombatStance;
        stanceCooldown = STANCE_TOGGLE_COOLDOWN;
        sync();
        return true;
    }

    public void setCombatStance(boolean inStance) {
        this.isInCombatStance = inStance;
    }

    public int getStanceCooldown() {
        return stanceCooldown;
    }
    public void sync() {
        ModEntityComponents.PLAYERSTANCE.sync(player);
    }
}
