package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.common.system.level.LevelSystem;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

public class ClassLevelSystem extends LevelSystem {
    private PlayerClass currentClass;

    public ClassLevelSystem(LivingEntity livingEntity, PlayerClass playerClass) {
        super(livingEntity);
        this.currentClass = playerClass;
        this.maxLevel = playerClass != null ? (short) playerClass.getMaxLevel() : 50;
        this.statPoints = 0;
    }

    public void updateForClass(PlayerClass playerClass) {
        this.currentClass = playerClass;
        if (playerClass != null) {
            short newMaxLevel = (short) playerClass.getMaxLevel();

            if (level > newMaxLevel) {
                level = newMaxLevel;
                updateNextLvl();
                if (experience > experienceToNextLevel) experience = experienceToNextLevel;
            }

            this.maxLevel = newMaxLevel;
        }
    }

    public void resetForTranscendence() {
        this.level = 1;
        this.experience = 0;
        this.statPoints = 0;
        updateNextLvl();
    }

    @Override
    protected short getStatPointsForLevel(short level) {
        return 1;
    }

    // Override to use current class max level
    @Override
    public boolean isMaxLevel() {
        return level >= getEffectiveMaxLevel();
    }

    private short getEffectiveMaxLevel() {
        return currentClass != null ? (short) currentClass.getMaxLevel() : maxLevel;
    }

    @Override
    public void addExperience(long amount) {
        short effectiveMaxLevel = getEffectiveMaxLevel();

        if (level >= effectiveMaxLevel) {
            experience = Math.min(experience + amount, experienceToNextLevel);
            return;
        }

        experience += amount;
        while (experience >= experienceToNextLevel && level < effectiveMaxLevel) {
            levelUp();
        }

        if (level >= effectiveMaxLevel) {
            experience = Math.min(experience, experienceToNextLevel);
        }
    }

    private void levelUp() {
        short effectiveMaxLevel = getEffectiveMaxLevel();
        if (level >= effectiveMaxLevel) return;

        experience -= experienceToNextLevel;
        level++;
        statPoints += getStatPointsForLevel(level);
        if (level < effectiveMaxLevel) updateNextLvl();
    }

    private void updateNextLvl() {
        experienceToNextLevel = calculateXpForNextLevel(level);
    }

    private long calculateXpForNextLevel(int level) {
        if (level < 1 || level >= getEffectiveMaxLevel()) return 0L;

        if (level - 1 < ModConfig.EXP_MAP.length) {
            return ModConfig.EXP_MAP[level - 1];
        }

        return (long) (100 * Math.pow(1.1, level - 1));
    }

    @Override
    public short getMaxLevel() {
        return getEffectiveMaxLevel();
    }

    @Override
    public void setMaxLevel(short maxLevel) {
        if (currentClass == null) {
            this.maxLevel = maxLevel;
        }
    }

    public PlayerClass getCurrentClass() {
        return currentClass;
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putShort("ClassLevel", level);
        tag.putLong("ClassExperience", experience);
        tag.putLong("ClassExperienceToNextLevel", experienceToNextLevel);
        if (LivingEntityUtil.isPlayer(livingEntity)) {
            tag.putShort("ClassPoints", statPoints);
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.level = tag.getShort("ClassLevel");
        this.experience = tag.getLong("ClassExperience");
        this.experienceToNextLevel = tag.getLong("ClassExperienceToNextLevel");
        if (LivingEntityUtil.isPlayer(livingEntity)) this.statPoints = tag.getShort("ClassPoints");
    }
}