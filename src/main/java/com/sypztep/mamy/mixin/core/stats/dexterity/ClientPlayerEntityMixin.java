package com.sypztep.mamy.mixin.core.stats.dexterity;

import com.mojang.authlib.GameProfile;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    @Shadow
    public Input input;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", shift = At.Shift.BY, by = 5))
    private void fasterMoveWhenDrawBow(CallbackInfo ci) {
        if (PassiveAbilityManager.isActive(this, ModPassiveAbilities.STEADY_AIM)) {
            if (getActiveItem().getItem() instanceof BowItem || getActiveItem().getItem() instanceof CrossbowItem) {
                input.movementForward *= 3;
                input.movementSideways *= 3;
            }
        }
    }
}