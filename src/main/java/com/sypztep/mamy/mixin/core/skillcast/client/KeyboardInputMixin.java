package com.sypztep.mamy.mixin.core.skillcast.client;

import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.skill.SkillCastingManager;
import com.sypztep.mamy.common.api.entity.MovementLock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends Input {

    @Inject(method = "tick", at = @At("TAIL"))
    private void lockMovementDuringCast(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        boolean shouldLock = SkillCastingManager.getInstance().shouldLockMovement() || hasMovementLockingEffect(player);
        //TODO : using interface for easy than hardcode. 9/10/2025 5:42AM
        if (shouldLock || ModEntityComponents.HIDING.get(player).getHiddingPos() != null) {
            pressingForward = false;
            pressingBack = false;
            pressingLeft = false;
            pressingRight = false;
            jumping = false;
            movementForward = 0;
            movementSideways = 0;
        }
    }
    @Unique
    private boolean hasMovementLockingEffect(LivingEntity entity) {
        for (StatusEffectInstance effectInstance : entity.getStatusEffects()) {
            StatusEffect effect = effectInstance.getEffectType().value();
            if (effect instanceof MovementLock movementLock && movementLock.shouldLockMovement()) return true;
        }
        return false;
    }
}