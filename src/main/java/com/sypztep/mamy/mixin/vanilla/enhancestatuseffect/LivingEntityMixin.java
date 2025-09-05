package com.sypztep.mamy.mixin.vanilla.enhancestatuseffect;

import com.sypztep.mamy.common.statuseffect.EffectRemoval;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "onStatusEffectUpgraded", at = @At("HEAD"))
    private void onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect, @Nullable Entity source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (reapplyEffect && !self.getWorld().isClient) {
            StatusEffect statusEffect = effect.getEffectType().value();
            if (statusEffect instanceof EffectRemoval effectRemoval) {
                effectRemoval.onRemoved(self);
            }
        }
    }

    @Inject(method = "onStatusEffectRemoved", at = @At("HEAD"))
    private void onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!self.getWorld().isClient) {
            StatusEffect statusEffect = effect.getEffectType().value();
            if (statusEffect instanceof EffectRemoval effectRemoval) {
                effectRemoval.onRemoved(self);
            }
        }
    }
}