package com.sypztep.mamy.mixin.vanilla.coyotytime;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Unique
    private static final int COYOTE_TIME_TICKS = 20; // 0.5 seconds
    @Unique
    private int coyoteTimeTicks = 0;

    @Unique
    private boolean wasOnGroundLastTick = false;

    @Unique
    private boolean shouldAllowCoyoteJump = false;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void updateCoyoteTime(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Only apply coyote time to players
        if (!(entity instanceof PlayerEntity)) return;

        boolean currentlyOnGround = entity.isOnGround();

        if (wasOnGroundLastTick && !currentlyOnGround) {
            // Just left ground - start coyote time
            coyoteTimeTicks = COYOTE_TIME_TICKS;
        } else if (currentlyOnGround) {
            // On ground - reset coyote time
            coyoteTimeTicks = 0;
        } else if (coyoteTimeTicks > 0) {
            // In air and coyote time active - count down
            coyoteTimeTicks--;
        }

        wasOnGroundLastTick = currentlyOnGround;

        // Set flag for whether coyote jump should be allowed this tick
        shouldAllowCoyoteJump = (entity instanceof PlayerEntity) && coyoteTimeTicks > 0;
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isOnGround()Z", ordinal = 2))
    private boolean redirectJumpGroundCheck(LivingEntity entity) {
        boolean actuallyOnGround = entity.isOnGround();

        if (!(entity instanceof PlayerEntity)) {
            return actuallyOnGround;
        }

        return actuallyOnGround || shouldAllowCoyoteJump;
    }

    // Consume coyote time when jump is actually performed
    @Inject(method = "jump", at = @At("HEAD"))
    private void consumeCoyoteTime(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!(entity instanceof PlayerEntity)) return;

        if (!entity.isOnGround() && shouldAllowCoyoteJump) {
            coyoteTimeTicks = 0; // Consume all remaining coyote time
            shouldAllowCoyoteJump = false;
        }
    }
}