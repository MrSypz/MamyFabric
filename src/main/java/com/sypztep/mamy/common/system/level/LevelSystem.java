package com.sypztep.mamy.common.system.level;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

public class LevelSystem {
    private short level;
    private long experience;
    private long experienceToNextLevel;
    private short statPoints;
    private final LivingEntity livingEntity;
    private static final short MAX_LEVEL = ModConfig.maxLevel;

    public LevelSystem(LivingEntity livingEntity) {
        this.level = 1;
        this.experience = 0;
        this.statPoints = ModConfig.startStatpoints;
        this.livingEntity = livingEntity;
        this.experienceToNextLevel = calculateXpForNextLevel(level);
    }

    private long calculateXpForNextLevel(int level) {
        if (level < 1 || level >= ModConfig.maxLevel) return 0L;
        return ModConfig.EXP_MAP[level - 1];
    }

    public void addExperience(long amount) {
        if (level >= MAX_LEVEL) {
            experience = Math.min(experience + amount, experienceToNextLevel);
            return;
        }
        experience += amount;
        while (experience >= experienceToNextLevel && level < MAX_LEVEL) levelUp();
        if (level >= MAX_LEVEL) experience = Math.min(experience, experienceToNextLevel);
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    private void levelUp() {
        if (level >= MAX_LEVEL) return;

        experience -= experienceToNextLevel;
        level++;
        statPoints += getStatPointsForLevel(level);
        if (level < MAX_LEVEL) updateNextLvl();
    }

    private short getStatPointsForLevel(short level) {
        return (short) (level / 5 + 3);
    }

    private void updateNextLvl() {
        experienceToNextLevel = calculateXpForNextLevel(level);
    }

    public boolean isMaxLevel() {
        return level >= MAX_LEVEL;
    }

    public short getMaxLevel() {
        return MAX_LEVEL;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
        updateNextLvl();
    }

    public long getExperience() {
        return experience;
    }

    public long getExperienceToNextLevel() {
        return experienceToNextLevel;
    }

    public float getExperiencePercentage() {
        if (this.experienceToNextLevel == 0) return 100.0f;
        return ((float) this.experience / this.experienceToNextLevel) * 100;
    }

    public short getStatPoints() {
        return statPoints;
    }

    public void setStatPoints(short statPoints) {
        this.statPoints = statPoints;
    }

    public void addStatPoints(short statPoints) {
        this.statPoints += statPoints;
    }

    public void subtractStatPoints(short points) {
        this.statPoints -= points;
    }

    public void writeToNbt(NbtCompound tag) {
        tag.putShort("Level", level);
        tag.putLong("Experience", experience);
        tag.putLong("ExperienceToNextLevel", experienceToNextLevel);
        if (LivingEntityUtil.isPlayer(livingEntity)) tag.putShort("StatPoints", statPoints);
    }

    public void readFromNbt(NbtCompound tag) {
        this.level = tag.getShort("Level");
        this.experience = tag.getLong("Experience");
        this.experienceToNextLevel = tag.getLong("ExperienceToNextLevel");
        if (LivingEntityUtil.isPlayer(livingEntity)) this.statPoints = tag.getShort("StatPoints");
    }
}
