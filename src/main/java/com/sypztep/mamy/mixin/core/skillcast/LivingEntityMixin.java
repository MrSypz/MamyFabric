package com.sypztep.mamy.mixin.core.skillcast;

import com.sypztep.mamy.common.api.entity.MovementLock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private float lockedBodyYaw = 0f;
    @Unique
    private boolean isBodyYawLocked = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void preventYawInput(CallbackInfo ci) {
        boolean shouldLock = hasYawLockingEffect((LivingEntity) (Object) this);
        LivingEntity entity = (LivingEntity) (Object) this;

        if (shouldLock) {
            if (!isBodyYawLocked) {
                lockedBodyYaw = entity.bodyYaw;
                isBodyYawLocked = true;
            }
        } else {
            isBodyYawLocked = false;
        }
    }

    @Inject(method = "turnHead", at = @At("HEAD"), cancellable = true)
    private void cancelTurnHead(float bodyRotation, float headRotation, CallbackInfoReturnable<Float> cir) {
        if (isBodyYawLocked) {
            LivingEntity entity = (LivingEntity) (Object) this;
            // Keep body yaw locked and return the head rotation leave animation hnadle it
            entity.prevBodyYaw = lockedBodyYaw;
            entity.bodyYaw = lockedBodyYaw;
            cir.setReturnValue(headRotation);
        }
    }

    @Unique
    private boolean hasYawLockingEffect(LivingEntity entity) {
        for (StatusEffectInstance effectInstance : entity.getStatusEffects()) {
            StatusEffect effect = effectInstance.getEffectType().value();
            if (effect instanceof MovementLock movementLock && movementLock.shouldLockYaw()) {
                return true;
            }
        }
        return false;
    }
}