package com.sypztep.mamy.common.util;

public interface MovementLock {
    default boolean shouldLockMovement() {
        return true;
    }
    default boolean shouldLockYaw() {
        return true;
    }
}
