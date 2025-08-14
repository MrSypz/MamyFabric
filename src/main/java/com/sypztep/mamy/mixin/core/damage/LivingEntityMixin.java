package com.sypztep.mamy.mixin.core.damage;

import com.sypztep.mamy.common.util.DamageUtil;
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
        cir.setReturnValue(DamageUtil.test((LivingEntity) (Object) this, source, amount));
    }

//    @Inject(method = "modifyAppliedDamage", at = @At("HEAD"), cancellable = true)
//    private void replaceModifyAppliedDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
//        cir.setReturnValue(amount);
//    }
}