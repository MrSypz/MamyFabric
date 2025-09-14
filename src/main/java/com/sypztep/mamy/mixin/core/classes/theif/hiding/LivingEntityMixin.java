package com.sypztep.mamy.mixin.core.classes.theif.hiding;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyReturnValue(method = "getAttackDistanceScalingFactor", at = @At("RETURN"))
    private double hidingReduceDetection(double original, Entity entity) {
        if (ModEntityComponents.HIDING.get(this).getHiddingPos() != null) return original * 0.05;
        return original;
    }
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void preventDamageWhileHiding(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity target = (LivingEntity)(Object)this;

        if (target instanceof PlayerEntity player) {
            if (ModEntityComponents.HIDING.get(player).getHiddingPos() != null) {
                if (damageSource.getAttacker() instanceof PlayerEntity ||
                        damageSource.isIn(ModTags.DamageTags.PROJECTILE_DAMAGE) ||
                        damageSource.isIn(DamageTypeTags.IS_PLAYER_ATTACK)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
