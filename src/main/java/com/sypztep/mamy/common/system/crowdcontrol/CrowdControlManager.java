package com.sypztep.mamy.common.system.crowdcontrol;

import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.player.PlayerEntity;

public class CrowdControlManager {
    public enum CrowdControlType {
        KNOCKDOWN(1f),
        BOUND(1f),
        STUN(1f),
        STIFFNESS(0.7f),
        FREEZING(1.0f),
        KNOCKBACK(0.7f),
        FLOATING(1f);

        private final float ccPoints;

        CrowdControlType(float ccPoints) {
            this.ccPoints = ccPoints;
        }

        public float getCcPoints() {
            return ccPoints;
        }
    }

    public static boolean applyCrowdControl(PlayerEntity player, CrowdControlType ccType, float customCcPoints) {
        var ccComponent = ModEntityComponents.PLAYERCROWDCONTROL.get(player);

        if (!ccComponent.canReceiveCrowdControl()) {
            return false;
        }

        float pointsToAdd = customCcPoints > 0 ? customCcPoints : ccType.getCcPoints();
        return ccComponent.addCrowdControlPoints(pointsToAdd);
    }

    public static boolean applyCrowdControl(PlayerEntity player, CrowdControlType ccType) {
        return applyCrowdControl(player, ccType, ccType.getCcPoints());
    }

    public static void removeCrowdControl(PlayerEntity player, CrowdControlType ccType) {
        // Effect-based removal is handled automatically when effect expires
        // This method can be used for manual removal if needed
    }

    public static boolean canApplyCrowdControl(PlayerEntity player) {
        var ccComponent = ModEntityComponents.PLAYERCROWDCONTROL.get(player);
        return ccComponent.canReceiveCrowdControl();
    }

    public static float getCrowdControlPoints(PlayerEntity player) {
        var ccComponent = ModEntityComponents.PLAYERCROWDCONTROL.get(player);
        return ccComponent.getCrowdControlPoints();
    }
}