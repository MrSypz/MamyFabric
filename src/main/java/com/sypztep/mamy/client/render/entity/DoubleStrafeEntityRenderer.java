package com.sypztep.mamy.client.render.entity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.skill.DoubleStrafeEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class DoubleStrafeEntityRenderer<T extends DoubleStrafeEntity> extends EntityRenderer<T> {
    private static final Identifier TEXTURE = Mamy.id("textures/entity/empty.png");

    public DoubleStrafeEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(T entity) {
        return TEXTURE;
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        matrixStack.push();
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90.0F));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch())));

        float shake = entity.shake - tickDelta;
        if (shake > 0.0F) {
            float shakeRotation = -MathHelper.sin(shake * 3.0F) * shake;
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(shakeRotation));
        }

        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0F));
        matrixStack.scale(0.05625F, 0.05625F, 0.05625F);
        matrixStack.translate(-4.0F, 0.0F, 0.0F);

        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutout(getTexture(entity)));
        MatrixStack.Entry entry = matrixStack.peek();

        renderArrow(matrixStack, entry, vertexConsumer, light);

        if (entity.getTicksAlive() > 1) {
            float progress = 0.0F;

            if (entity.getGroundTime() > 0)
                progress = MathHelper.clamp(entity.getGroundTime() / 5.0F, 0.0F, 1.0F);

            if (progress < 1.0F) {
                float xOffset = MathHelper.lerp(progress, -64.0F, 0.0F);
                matrixStack.translate(xOffset, 0.0F, 0.0F);
                MatrixStack.Entry secondEntry = matrixStack.peek();
                renderArrow(matrixStack, secondEntry, vertexConsumer, light);
            }
        }

        matrixStack.pop();
        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
    }

    private void renderArrow(MatrixStack matrixStack, MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light) {
        vertex(entry, vertexConsumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, light);

        matrixStack.push();
        for (int u = 0; u < 4; u++) {
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            MatrixStack.Entry rotatedEntry = matrixStack.peek();
            vertex(rotatedEntry, vertexConsumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, light);
            vertex(rotatedEntry, vertexConsumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, light);
            vertex(rotatedEntry, vertexConsumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, light);
            vertex(rotatedEntry, vertexConsumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, light);
        }
        matrixStack.pop();
    }

    private void vertex(MatrixStack.Entry matrix, VertexConsumer vertexConsumer, int x, int y, int z, float u, float v, int normalX, int normalZ, int normalY, int light) {
        vertexConsumer.vertex(matrix, (float)x, (float)y, (float)z)
                .color(Colors.WHITE)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(matrix, normalX, normalY, normalZ);
    }
}