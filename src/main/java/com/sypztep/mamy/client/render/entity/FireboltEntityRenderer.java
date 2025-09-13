package com.sypztep.mamy.client.render.entity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.render.VertexContext;
import com.sypztep.mamy.common.entity.skill.FireboltEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class FireboltEntityRenderer extends EntityRenderer<FireboltEntity> {
    private static final Identifier TEXTURE = Mamy.id("textures/entity/empty.png");
    private static final float HEIGHT = 1.0f; // Shorter than fireball

    public FireboltEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(FireboltEntity entity) {
        return TEXTURE;
    }

    private static final RenderLayer FLAME_ALPHA_LAYER = createFlameAlphaLayer();

    private static RenderLayer createFlameAlphaLayer() {
        return RenderLayer.of(
                "firebolt_trail",
                VertexFormats.POSITION_COLOR_TEXTURE_LIGHT,
                VertexFormat.DrawMode.QUADS,
                1536,
                false,
                true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.POSITION_COLOR_TEXTURE_LIGHTMAP_PROGRAM)
                        .texture(new RenderLayer.Texture(Mamy.id("textures/vfx/flame_alpha.png"), false, false))
                        .transparency(RenderLayer.ADDITIVE_TRANSPARENCY)
                        .depthTest(RenderLayer.LEQUAL_DEPTH_TEST)
                        .cull(RenderLayer.DISABLE_CULLING)
                        .writeMaskState(RenderLayer.COLOR_MASK)
                        .lightmap(RenderLayer.ENABLE_LIGHTMAP)
                        .target(RenderLayer.MAIN_TARGET)
                        .build(false)
        );
    }

    @Override
    public void render(FireboltEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        float lerpedYaw = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
        float lerpedPitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(lerpedYaw - 90.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(lerpedPitch + 90.0f));

        renderFireBolt(matrices, vertexConsumers, entity, tickDelta);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void renderFireBolt(MatrixStack matrices, VertexConsumerProvider vertexConsumers, FireboltEntity entity, float tickDelta) {
        matrices.push();

        VertexContext context = new VertexContext(matrices, vertexConsumers);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float smoothTime = (entity.age + tickDelta) * 0.12f; // Faster rotation for smaller projectile
        float rotation = smoothTime * 2.5f * (float) Math.PI; // Single rotation speed

        VertexConsumer consumer = vertexConsumers.getBuffer(FLAME_ALPHA_LAYER);

        // Single layer - just the inner core
        float alpha = 1.0f; // Full opacity for single layer
        float endAlpha = 0.0f;

        // Bright orange-yellow color for the single layer
        float r = 1.0f, g = 0.7f, b = 0.2f;

        // Draw single layer - smaller than fireball's inner layer
        drawRotatingBeamSides(context, consumer, matrix, 0.05f, HEIGHT,
                r, g, b, alpha, endAlpha, rotation);

        matrices.pop();
    }

    private void drawRotatingBeamSides(VertexContext context, VertexConsumer consumer, Matrix4f matrix,
                                       float halfWidth, float height,
                                       float r, float g, float b,
                                       float alpha, float endAlpha, float rotation) {
        float sin = (float) Math.sin(rotation);
        float cos = (float) Math.cos(rotation);

        float x1 = -halfWidth * cos - (-halfWidth) * sin;
        float z1 = -halfWidth * sin + (-halfWidth) * cos;
        float x2 = halfWidth * cos - (-halfWidth) * sin;
        float z2 = halfWidth * sin + (-halfWidth) * cos;
        float x3 = halfWidth * cos - halfWidth * sin;
        float z3 = halfWidth * sin + halfWidth * cos;
        float x4 = -halfWidth * cos - halfWidth * sin;
        float z4 = -halfWidth * sin + halfWidth * cos;

        drawBeamSide(context, consumer, matrix, x1, z1, x2, z2, height, r, g, b, alpha, endAlpha);
        drawBeamSide(context, consumer, matrix, x3, z3, x4, z4, height, r, g, b, alpha, endAlpha);
        drawBeamSide(context, consumer, matrix, x4, z4, x1, z1, height, r, g, b, alpha, endAlpha);
        drawBeamSide(context, consumer, matrix, x2, z2, x3, z3, height, r, g, b, alpha, endAlpha);
    }

    private void drawBeamSide(VertexContext context, VertexConsumer consumer, Matrix4f matrix,
                              float x1, float z1, float x2, float z2, float height,
                              float r, float g, float b, float alpha, float endAlpha) {
        float u1 = 0, v1 = 0.0f;
        float u2 = 1.0f / 8, v2 = 1.0f;

        context.fillGradientWithTexture(consumer, matrix,
                x1, 0, z1, u1, v2, r, g, b, alpha,
                x1, height, z1, u1, v1, r, g, b, endAlpha,
                x2, height, z2, u2, v1, r, g, b, endAlpha,
                x2, 0, z2, u2, v2, r, g, b, alpha);
    }
}