package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.common.util.MovementLock;
import net.minecraft.util.Identifier;

public interface CastableSkill extends MovementLock {
    int getBaseVCT(int skillLevel);

    int getBaseFCT(int skillLevel);

    @Override
    default boolean shouldLockMovement() {
        return false;
    }
    default boolean canBeInterupt() {
        return false;
    }

    default Identifier getCastAnimation() {
        return null;
    }

    default boolean hasCastAnimation() {
        return getCastAnimation() != null;
    }
}