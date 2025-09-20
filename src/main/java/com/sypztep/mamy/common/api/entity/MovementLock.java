package com.sypztep.mamy.common.api.entity;

public interface MovementLock {
    default boolean shouldLockMovement() {
        return true;
    }
    default boolean shouldLockYaw() {
        return true;
    }
}
