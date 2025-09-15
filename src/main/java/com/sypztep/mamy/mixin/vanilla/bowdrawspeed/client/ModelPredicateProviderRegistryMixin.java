package com.sypztep.mamy.mixin.vanilla.bowdrawspeed.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelPredicateProviderRegistry.class) @Environment(EnvType.CLIENT)
public class ModelPredicateProviderRegistryMixin {
    @ModifyReturnValue(method = "method_27890", at = @At(value = "RETURN", ordinal = 2))
    private static float modelQuickerDraw(float original) {
        ClientPlayerEntity entity = MinecraftClient.getInstance().player;
        assert entity != null;
        float value;
        if (entity.isSneaking()) value = 2; else value = 0;
        return original + value;
    }
}