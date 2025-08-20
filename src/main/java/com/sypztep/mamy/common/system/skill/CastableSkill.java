package com.sypztep.mamy.common.system.skill;

public interface CastableSkill {
    int getCastTime(int skillLevel); // Cast time in ticks

    default boolean shouldLockMovement() {
        return false;
    }
}