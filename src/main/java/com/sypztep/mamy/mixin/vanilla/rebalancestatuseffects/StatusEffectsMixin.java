package com.sypztep.mamy.mixin.vanilla.rebalancestatuseffects;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StatusEffects.class)
public abstract class StatusEffectsMixin {

    @ModifyReturnValue(method = "register", at = @At("RETURN"))
    private static RegistryEntry<StatusEffect> reduceAccuracyfromEffectSoItNowMakesenseIfPlayerAreBlindnessOrLessSeeThing(RegistryEntry<StatusEffect> original, String id, StatusEffect statusEffect) {
        if ("blindness".equals(id))
            statusEffect.addAttributeModifier(ModEntityAttributes.ACCURACY, Mamy.id("effect.blindness_reduce_accuracy"), -40.0, EntityAttributeModifier.Operation.ADD_VALUE); // 10%
        if ("darkness".equals(id))
            statusEffect.addAttributeModifier(ModEntityAttributes.ACCURACY, Mamy.id("effect.darkness_reduce_accuracy"), -80.0, EntityAttributeModifier.Operation.ADD_VALUE); // 20%
        return original;
    }
}