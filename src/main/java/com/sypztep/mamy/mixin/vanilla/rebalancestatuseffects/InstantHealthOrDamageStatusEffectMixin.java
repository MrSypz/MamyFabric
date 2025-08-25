package com.sypztep.mamy.mixin.vanilla.rebalancestatuseffects;

import net.minecraft.entity.effect.InstantHealthOrDamageStatusEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InstantHealthOrDamageStatusEffect.class)
public class InstantHealthOrDamageStatusEffectMixin {
    @ModifyArg(method = "applyInstantEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;heal(F)V"))
    private float rebalanceStatusEffectsInstantHealth(float value) {
        return value * 3 / 4F;
    }

    @ModifyArg(method = "applyInstantEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private float rebalanceStatusEffectsInstantDamage(float value) {
        return value / 2F;
    }

    @ModifyArg(method = "applyUpdateEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;heal(F)V"))
    private float rebalanceStatusEffectsUpdateHealth(float value) {
        return value * 3 / 4F;
    }

    @ModifyArg(method = "applyUpdateEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private float rebalanceStatusEffectsUpdateDamage(float value) {
        return value / 2F;
    }
}
