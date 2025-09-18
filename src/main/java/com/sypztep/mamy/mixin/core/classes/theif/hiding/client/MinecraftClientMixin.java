package com.sypztep.mamy.mixin.core.classes.theif.hiding.client;

import com.sypztep.mamy.common.init.ModEntityComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
@Environment(EnvType.CLIENT)
public abstract class MinecraftClientMixin {
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void preventAttackWhileHidden(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && ModEntityComponents.HIDING.get(player).getHiddingPos() != null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void preventUseWhileHidden(CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && ModEntityComponents.HIDING.get(player).getHiddingPos() != null) {
            ci.cancel();
        }
    }
}