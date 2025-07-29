package com.sypztep.mamy.mixin.core.damage;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.sypztep.mamy.common.util.DamageUtil.calculateFinalDamage;

@Mixin(DamageUtil.class)
public class DamageUtilMixin {
    @ModifyReturnValue(method = "getDamageLeft", at = @At("RETURN"))
    private static float replaceArmorCalculation(float originalReturn,
                                                 LivingEntity armorWearer, float damageAmount, DamageSource damageSource,
                                                 float armor, float armorToughness) {
        return calculateFinalDamage(armorWearer, damageAmount, damageSource, armor);
    }
}
