package com.sypztep.mamy.mixin.core.damage;

import com.sypztep.mamy.common.system.damage.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 2000)
public class LivingEntityMixin {

    @Inject(method = "applyArmorToDamage", at = @At("HEAD"), cancellable = true)
    private void replaceArmorCalculation(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(DamageUtil.getDamageAfterArmor((LivingEntity) (Object) this, source, amount));
    }

    // Inject code after get Enchantment Protection
    @Inject(method = "modifyAppliedDamage", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
    private void replaceModifyAppliedDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(DamageUtil.damageResistanceModifier((LivingEntity) (Object) this, cir.getReturnValue(), source));
    }
}