package com.sypztep.mamy.mixin.vanilla.stats.vitality;

import com.llamalad7.mixinextras.sugar.Local;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @ModifyArg(method = "update",at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V"),index = 0)
    private float modifyHungerHealAmount(float originalHealAmount, @Local(argsOnly = true) PlayerEntity player) {
        double healEffectiveness = player.getAttributeValue(ModEntityAttributes.HEAL_EFFECTIVE);
        float multiplier = 1.0f + (float) healEffectiveness;
        return originalHealAmount * multiplier;
    }
}