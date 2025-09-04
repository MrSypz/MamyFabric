package com.sypztep.mamy.client.render.entity;

import com.sypztep.mamy.common.entity.entity.skill.ArrowRainEntity;
import com.sypztep.mamy.common.init.ModParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class ArrowRainEntityRenderer<T extends ArrowRainEntity> extends EntityRenderer<T> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/projectiles/arrow.png");
    private static final int ARROW_COUNT = 8; // Number of arrows to render per tick
    private static final float MAX_HEIGHT = 12.0f; // Maximum starting height
    private static final float MIN_HEIGHT = 0.0f; // Minimum starting height

    public ArrowRainEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(T entity) {
        return TEXTURE;
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        matrixStack.push();
        float areaSize = entity.getSkillLevel() >= 6 ? 5.0f : 3.0f;
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutout(getTexture(entity)));


        // Render multiple arrows falling within the area
        for (int i = 0; i < ARROW_COUNT; i++) {
            matrixStack.push();

            // Random offset within the area
            double offsetX = (entity.getWorld().random.nextDouble() - 0.5) * areaSize;
            double offsetZ = (entity.getWorld().random.nextDouble() - 0.5) * areaSize;
            float initialHeight = MIN_HEIGHT + entity.getWorld().random.nextFloat() * (MAX_HEIGHT - MIN_HEIGHT);

            // Gravity-based fall: y = initialHeight - 0.5 * g * t^2
            float yOffset = Math.max(0.0f, initialHeight - 0.5f * (tickDelta) * (tickDelta));

            if (yOffset < 0.1f && entity.getWorld().isClient) {
                entity.getWorld().addParticle(ModParticles.ARROW_IMPACT,
                        entity.getX() + offsetX, entity.getY() + 0.1, entity.getZ() + offsetZ,
                        0.0, 0.0, 0.0); // Static, no velocity
            }

            // Translate to the arrow's position
            matrixStack.translate(offsetX, yOffset, offsetZ);

            // Random yaw and downward pitch to simulate falling arrows
            float arrowYaw = entity.getWorld().random.nextFloat() * 360.0f;
            float arrowPitch = -90.0f; // Point downward
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arrowYaw - 90.0f));
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(arrowPitch));

            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0f));
            matrixStack.scale(0.05625f, 0.05625f, 0.05625f);
            matrixStack.translate(-4.0, 0.0, 0.0);

            // Render the arrow
            MatrixStack.Entry entry = matrixStack.peek();
            vertex(entry, vertexConsumer, -7, -2, -2, 0.0f, 0.15625f, -1, 0, 0, light);
            vertex(entry, vertexConsumer, -7, -2, 2, 0.15625f, 0.15625f, -1, 0, 0, light);
            vertex(entry, vertexConsumer, -7, 2, 2, 0.15625f, 0.3125f, -1, 0, 0, light);
            vertex(entry, vertexConsumer, -7, 2, -2, 0.0f, 0.3125f, -1, 0, 0, light);
            vertex(entry, vertexConsumer, -7, 2, -2, 0.0f, 0.15625f, 1, 0, 0, light);
            vertex(entry, vertexConsumer, -7, 2, 2, 0.15625f, 0.15625f, 1, 0, 0, light);
            vertex(entry, vertexConsumer, -7, -2, 2, 0.15625f, 0.3125f, 1, 0, 0, light);
            vertex(entry, vertexConsumer, -7, -2, -2, 0.0f, 0.3125f, 1, 0, 0, light);

            for (int u = 0; u < 4; ++u) {
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
                vertex(entry, vertexConsumer, -8, -2, 0, 0.0f, 0.0f, 0, 1, 0, light);
                vertex(entry, vertexConsumer, 8, -2, 0, 0.5f, 0.0f, 0, 1, 0, light);
                vertex(entry, vertexConsumer, 8, 2, 0, 0.5f, 0.15625f, 0, 1, 0, light);
                vertex(entry, vertexConsumer, -8, 2, 0, 0.0f, 0.15625f, 0, 1, 0, light);
            }

            matrixStack.pop();
        }

        matrixStack.pop();
        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
    }

    private void vertex(MatrixStack.Entry matrix, VertexConsumer vertexConsumer, int x, int y, int z, float u, float v, int normalX, int normalZ, int normalY, int light) {
        vertexConsumer.vertex(matrix, (float) x, (float) y, (float) z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(matrix, (float) normalX, (float) normalY, (float) normalZ);
    }
}