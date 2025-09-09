package com.sypztep.mamy.mixin.vanilla.balanceshield;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.PlayerShieldScoreComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;blockedByShield(Lnet/minecraft/entity/damage/DamageSource;)Z"))
    private boolean wrapShieldBlocking(LivingEntity entity, DamageSource source, Operation<Boolean> original, DamageSource damageSource, float amount) {
        boolean vanillaBlocked = original.call(entity, source);

        if (!vanillaBlocked || !(entity instanceof PlayerEntity player)) return vanillaBlocked;

        PlayerShieldScoreComponent shieldComponent = ModEntityComponents.PLAYERSHIELDSCORE.get(player);
        Mamy.LOGGER.info("Shield blocking check - damage: {}, current score: {}", amount, shieldComponent.getCurrentShieldScore());

        shieldComponent.startBlocking();

        if (!shieldComponent.canBlock()) return false;

        double currentShieldScore = shieldComponent.getCurrentShieldScore();
        if (amount <= currentShieldScore)
            shieldComponent.consumeShieldScore(amount, false);
         else {
            shieldComponent.consumeShieldScore(currentShieldScore, true);
            entity.getWorld().playSound(entity,player.getBlockPos(),SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS,1,1);
        }

        return true;
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
