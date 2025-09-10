package com.sypztep.mamy.common.statuseffect;

import com.sypztep.mamy.client.event.animation.CrowdControlAnimationManager;
import com.sypztep.mamy.common.system.crowdcontrol.CrowdControlManager;
import com.sypztep.mamy.common.util.MovementLock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class CrowdControlStatusEffect extends CleanUpEffect implements MovementLock {
    private final CrowdControlManager.CrowdControlType ccType;
    private final float ccPoints;

    public CrowdControlStatusEffect(StatusEffectCategory category, CrowdControlManager.CrowdControlType ccType, float ccPoints) {
        super(category);
        this.ccType = ccType;
        this.ccPoints = ccPoints;
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity player) {
            CrowdControlManager.applyCrowdControl(player, ccType, ccPoints);
            // TODO: it kick player due this play on server
            CrowdControlAnimationManager.startCrowdControlAnimation(ccType);
        }
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            CrowdControlManager.removeCrowdControl(player, ccType);

            CrowdControlAnimationManager.stopCrowdControlAnimation();
        }
    }
}