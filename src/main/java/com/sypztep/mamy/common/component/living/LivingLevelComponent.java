package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.level.LevelSystem;
import com.sypztep.mamy.common.system.stat.LivingStats;
import com.sypztep.mamy.common.system.stat.Stat;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public final class LivingLevelComponent implements AutoSyncedComponent {
    private final LivingEntity living;
    private final LivingStats livingStats;

    public LivingLevelComponent(LivingEntity living) {
        this.living = living;
        this.livingStats = new LivingStats(living);
    }

    public LivingEntity getLiving() {
        return living;
    }

    // ====================
    // CORE GETTERS - Keep only what's actually used
    // ====================

    public LevelSystem getLevelSystem() {
        return livingStats.getLevelSystem();
    }

    public LivingStats getLivingStats() {
        return livingStats;
    }

    public int getLevel() {
        return livingStats.getLevelSystem().getLevel();
    }

    public long getExperience() {
        return livingStats.getLevelSystem().getExperience();
    }

    public long getExperienceToNextLevel() {
        return livingStats.getLevelSystem().getExperienceToNextLevel();
    }

    public float getExperiencePercentage() {
        return livingStats.getLevelSystem().getExperiencePercentage();
    }

    public int getAvailableStatPoints() {
        if (!LivingEntityUtil.isPlayer(living)) return 0;
        return livingStats.getLevelSystem().getStatPoints();
    }

    public Stat getStatByType(StatTypes types) {
        return livingStats.getStat(types);
    }

    public short getStatValue(StatTypes types) {
        return getStatByType(types).getCurrentValue();
    }

    public int getStatCost(StatTypes types) {
        return getStatByType(types).getIncreasePerPoint();
    }

    // ====================
    // BATCH OPERATIONS (DOMINATUS STYLE) - Single sync
    // ====================

    public void performBatchUpdate(Runnable updates) {
        updates.run();
        sync();
    }

    /**
     * Increase stat and refresh effects in one operation
     */
    public boolean tryIncreaseStat(StatTypes statType, short points) {
        if (!canIncreaseStat(statType)) return false;

        performBatchUpdate(() -> {
            livingStats.useStatPoint(statType, points);
            refreshStatEffectsInternal(statType);
        });
        return true;
    }

    public void addExperience(long amount) {
        performBatchUpdate(() -> livingStats.getLevelSystem().addExperience(amount));
    }

    public void setLevel(int level) {
        performBatchUpdate(() -> {
            livingStats.getLevelSystem().setLevel((short) level);
            livingStats.getLevelSystem().setExperience(0);
        });
    }

    public void resetStatsWithPointReturn() {
        if (!LivingEntityUtil.isPlayer(living)) return;

        performBatchUpdate(() -> livingStats.resetStats((PlayerEntity) living, true));
    }

    public void handleRespawn() {
        performBatchUpdate(this::refreshAllStatEffectsInternal);
    }

    // ====================
    // VALIDATION HELPERS
    // ====================

    public boolean canIncreaseStat(StatTypes statType) {
        if (!LivingEntityUtil.isPlayer(living)) return false;
        int cost = getStatCost(statType);
        return getAvailableStatPoints() >= cost;
    }

    // ====================
    // INTERNAL METHODS (NO SYNC) - for batch operations
    // ====================
    public void refreshAllStatEffectsInternal() {
        for (StatTypes statType : StatTypes.values()) {
            refreshStatEffectsInternal(statType);
        }
    }
    // single
    public void refreshStatEffectsInternal(StatTypes statType) {
        Stat stat = getStatByType(statType);
        stat.applyPrimaryEffect(living);
        stat.applySecondaryEffect(living);
    }

    // ====================
    // NBT & SYNC
    // ====================

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        livingStats.readFromNbt(nbtCompound, living);
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        livingStats.writeToNbt(nbtCompound, living);
    }

    private void sync() {
        ModEntityComponents.LIVINGLEVEL.sync(this.living);
    }
}