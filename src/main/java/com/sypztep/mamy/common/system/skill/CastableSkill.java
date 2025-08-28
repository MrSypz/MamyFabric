package com.sypztep.mamy.common.system.skill;

import net.minecraft.util.Identifier;

public interface CastableSkill {
    int getBaseVCT(int skillLevel);

    int getBaseFCT(int skillLevel);

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