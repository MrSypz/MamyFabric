package com.sypztep.mamy.mixin.vanilla.coyotytime;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Unique
    private static final int COYOTE_TIME_TICKS = 20;

    @Unique
    private int coyoteTimeTicks = 0;

    @Unique
    private boolean wasOnGroundLastTick = false;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void updateCoyoteTime(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!(entity instanceof PlayerEntity)) return;

        boolean currentlyOnGround = entity.isOnGround();

        if (wasOnGroundLastTick && !currentlyOnGround) coyoteTimeTicks = COYOTE_TIME_TICKS;
         else if (currentlyOnGround) coyoteTimeTicks = 0;
         else if (coyoteTimeTicks > 0) coyoteTimeTicks--;


        wasOnGroundLastTick = currentlyOnGround;
    }

    @ModifyExpressionValue(
            method = "tickMovement",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isOnGround()Z")
    )
    private boolean allowCoyoteTimeJump(boolean originalOnGround) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!(entity instanceof PlayerEntity)) {
            return originalOnGround;
        }

        return originalOnGround || coyoteTimeTicks > 0;
    }

    @Inject(method = "jump", at = @At("HEAD"))
    private void consumeCoyoteTime(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!(entity instanceof PlayerEntity)) return;

        if (!entity.isOnGround() && coyoteTimeTicks > 0) coyoteTimeTicks = 0;
    }
}