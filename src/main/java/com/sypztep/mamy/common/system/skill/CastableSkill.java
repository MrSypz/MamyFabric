package com.sypztep.mamy.common.system.skill;

import net.minecraft.util.Identifier;

public interface CastableSkill {
    int getBaseVCT(int skillLevel);

    int getBaseFCT(int skillLevel);

    @Deprecated
    default int getCastTime(int skillLevel) {
        return getBaseVCT(skillLevel) + getBaseFCT(skillLevel);
    }

    default boolean shouldLockMovement() {
        return false;
    }

    default Identifier getCastAnimation() {
        return null;
    }

    default boolean hasCastAnimation() {
        return getCastAnimation() != null;
    }
}