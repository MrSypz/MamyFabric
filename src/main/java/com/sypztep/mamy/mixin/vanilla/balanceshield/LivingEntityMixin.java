package com.sypztep.mamy.mixin.vanilla.balanceshield;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.common.component.living.PlayerShieldScoreComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;blockedByShield(Lnet/minecraft/entity/damage/DamageSource;)Z"))
    private boolean wrapShieldBlocking(LivingEntity entity, DamageSource source, Operation<Boolean> original, DamageSource damageSource, float amount) {
        boolean vanillaBlocked = original.call(entity, source);
        if (!ModConfig.shieldReblance) return vanillaBlocked;
        if (!vanillaBlocked || !(entity instanceof PlayerEntity player)) return vanillaBlocked;

        PlayerShieldScoreComponent shieldComponent = ModEntityComponents.PLAYERSHIELDSCORE.get(player);

        shieldComponent.startBlocking();

        if (!shieldComponent.canBlock()) return false;

        double currentShieldScore = shieldComponent.getCurrentShieldScore();
        if (amount <= currentShieldScore)
            shieldComponent.consumeShieldScore(amount, false);
         else {
            shieldComponent.consumeShieldScore(currentShieldScore, true);
        }

        return true;
    }

    @ModifyConstant(method = "isBlocking", constant = @Constant(intValue = 5))
    private int shieldNoDelay(int constant) {
        if (!ModConfig.shieldReblance) return constant;
        return 0;
    }

    // This part are prevent damage when shield are not have score || shield are not function it not gonna damage item dut due to we are mark at item when broke prevent user to using it so I comment it out
//    @WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
//    private void wrapDamageShield(LivingEntity entity, float amount, Operation<Void> original, DamageSource source, float damage) {
//        if (!(entity instanceof PlayerEntity player)) {
//            original.call(entity, amount);
//            return;
//        }
//
//        PlayerShieldScoreComponent shieldComponent = ModEntityComponents.PLAYERSHIELDSCORE.get(player);
//        double currentScore = shieldComponent.getCurrentShieldScore();
//
//        if (damage > currentScore) {
//            float excessDamage = (float)(damage - currentScore);
//            original.call(entity, excessDamage);
//        }
//    }
}
