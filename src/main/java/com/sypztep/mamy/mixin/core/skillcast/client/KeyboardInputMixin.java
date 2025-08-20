package com.sypztep.mamy.mixin.core.skillcast.client;

import com.sypztep.mamy.common.system.skill.SkillCastingManager;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends Input {

    @Inject(method = "tick", at = @At("TAIL"))
    private void lockMovementDuringCast(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        if (SkillCastingManager.getInstance().shouldLockMovement()) {
            pressingForward = false;
            pressingBack = false;
            pressingLeft = false;
            pressingRight = false;
            movementForward = 0;
            movementSideways = 0;
        }
    }
}