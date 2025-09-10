package com.sypztep.mamy.mixin.core.classes.theif.hiding;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyReturnValue(method = "getAttackDistanceScalingFactor", at = @At("RETURN"))
    private double hidingReduceDetection(double original, Entity entity) {
        if (ModEntityComponents.HIDING.get(this).getBuryPos() != null) return original * 0.05;
        return original;
    }
}
