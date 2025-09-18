package com.sypztep.mamy.mixin.core.classes.theif.hiding.client;

import com.sypztep.mamy.common.init.ModEntityComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemFeatureRenderer.class)
@Environment(EnvType.CLIENT)
public class HeldItemFeatureRendererMixin {
    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    private void skipRenderIfHidden(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (livingEntity instanceof PlayerEntity player) {
            if (ModEntityComponents.HIDING.get(player).getHiddingPos() != null) {
                ci.cancel();
            }
        }
    }
}
