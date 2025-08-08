package com.sypztep.mamy.mixin.vanilla.passive.client;

import com.sypztep.mamy.common.component.living.HeadShotEntityComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.passive.SquidEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {

    @Shadow
    protected M model;

    protected LivingEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context);
    }

    @Inject(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD")
    )
    public void render(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        boolean isHeadExploding = ModEntityComponents.HEADSHOT.maybeGet(livingEntity)
                .map(HeadShotEntityComponent::isHeadShot)
                .orElse(false);

        if (this.model instanceof BipedEntityModel<?> bipedModel) {
            bipedModel.head.visible = !isHeadExploding;

            // Optionally hide other headwear layers if present, e.g. hat for players
            if (bipedModel instanceof PlayerEntityModel<?> playerModel) {
                playerModel.hat.visible = !isHeadExploding;
            }
        } else if (this.model instanceof SinglePartEntityModel<?> singlePartEntityModel) {
            String partName = (livingEntity instanceof SquidEntity || livingEntity instanceof GhastEntity) ? "body" : "head";
            Optional<ModelPart> optionalPart = singlePartEntityModel.getChild(partName);
            optionalPart.ifPresent(part -> part.visible = !isHeadExploding);
        }

    }
}
