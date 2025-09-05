package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public final class PlayerCrowdControlComponent implements AutoSyncedComponent, CommonTickingComponent {
    private final PlayerEntity player;
    private float ccPoints = 0.0f;
    private int ccDecayTimer = 0;
    private static final float MAX_CC_POINTS = 2f;
    private static final int CC_DECAY_INTERVAL = 100; // 5 seconds

    public PlayerCrowdControlComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        ccPoints = tag.getFloat("CrowdControlPoints");
        ccDecayTimer = tag.getInt("CrowdControlDecayTimer");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putFloat("CrowdControlPoints", ccPoints);
        tag.putInt("CrowdControlDecayTimer", ccDecayTimer);
    }

    @Override
    public void tick() {
        if (ccPoints > 0.0f) {
            ccDecayTimer++;
            if (ccDecayTimer >= CC_DECAY_INTERVAL) {
                ccPoints = Math.max(0.0f, ccPoints - 0.5f);
                ccDecayTimer = 0;
                sync();
            }
        } else {
            ccDecayTimer = 0;
        }
    }

    public boolean canReceiveCrowdControl() {
        return ccPoints < MAX_CC_POINTS;
    }

    public boolean addCrowdControlPoints(float points) {
        if (ccPoints >= MAX_CC_POINTS) {
            return false;
        }

        ccPoints = Math.min(MAX_CC_POINTS, ccPoints + points);
        ccDecayTimer = 0; // Reset decay timer
        sync();
        return true;
    }

    public float getCrowdControlPoints() {
        return ccPoints;
    }

    public void resetCrowdControlPoints() {
        ccPoints = 0.0f;
        ccDecayTimer = 0;
        sync();
    }

    public void sync() {
        ModEntityComponents.PLAYERCROWDCONTROL.sync(player);
    }
}