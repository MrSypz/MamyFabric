package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.common.system.level.LevelSystem;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

public class ClassLevelSystem extends LevelSystem {

    public ClassLevelSystem(LivingEntity livingEntity) {
        super(livingEntity);
        this.maxLevel = 50;
        this.statPoints = 0;
    }
    // Give 1 each time level up
    @Override
    protected short getStatPointsForLevel(short level) {
        return 1;
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putShort("ClassLevel", level);
        tag.putLong("ClassExperience", experience);
        tag.putLong("ClassExperienceToNextLevel", experienceToNextLevel);
        if (LivingEntityUtil.isPlayer(livingEntity)) tag.putShort("ClassPoints", statPoints);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.level = tag.getShort("ClassLevel");
        this.experience = tag.getLong("ClassExperience");
        this.experienceToNextLevel = tag.getLong("ClassExperienceToNextLevel");
        if (LivingEntityUtil.isPlayer(livingEntity)) this.statPoints = tag.getShort("ClassPoints");
    }
}
