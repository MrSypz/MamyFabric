package com.sypztep.mamy.mixin.core.feature;

import com.sypztep.mamy.client.render.entity.feature.MaxLevelPlayerFeatureRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;renderShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/Entity;FFLnet/minecraft/world/WorldView;F)V")
    )
    private void replacePlayerShadowWithGlow(Entity entity, double x, double y, double z, float yaw,
                                             float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                             int light, CallbackInfo ci) {
        if (entity instanceof PlayerEntity player) {
            MaxLevelPlayerFeatureRenderer.render(matrices, vertexConsumers, player, light);
        }
    }
}