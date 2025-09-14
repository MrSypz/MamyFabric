package com.sypztep.mamy.client.render.entity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.render.VertexContext;
import com.sypztep.mamy.common.entity.skill.ThunderSphereEntity;
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
public class ThunderSphereEntityRenderer extends EntityRenderer<ThunderSphereEntity> {
    private static final Identifier TEXTURE = Mamy.id("textures/entity/empty.png");

    public ThunderSphereEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(ThunderSphereEntity entity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        float lerpedYaw = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
        float lerpedPitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(lerpedYaw - 90.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(lerpedPitch + 90.0f));

        renderElectricSphere(matrices, vertexConsumers, entity, tickDelta);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void renderElectricSphere(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                      ThunderSphereEntity entity, float tickDelta) {
        matrices.push();

        VertexContext context = new VertexContext(matrices, vertexConsumers);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float smoothTime = (entity.age + tickDelta) * 0.1f;

        VertexConsumer consumer = vertexConsumers.getBuffer(getLightningRenderLayer());

        // Electric sphere with multiple rotating lightning rings
        renderLightningRings(context, consumer, matrix, entity, smoothTime);

        // Central electric core
        renderElectricCore(context, consumer, matrix, smoothTime);

        matrices.pop();
    }

    private void renderLightningRings(VertexContext context, VertexConsumer consumer, Matrix4f matrix,
                                      ThunderSphereEntity entity, float smoothTime) {
        // Multiple concentric lightning rings rotating at different speeds
        for (int ring = 0; ring < 3; ring++) {
            float ringRadius = 1.5f + (ring * 0.8f);
            float ringRotation = smoothTime * (1.0f + ring * 0.5f) * (float) Math.PI;
            float ringTilt = ring * 60.0f; // Different tilt for each ring

            // Lightning ring colors - from bright white core to electric blue edge
            float r, g, b, alpha;
            switch (ring) {
                case 0: // Inner ring - bright white
                    r = 1.0f; g = 1.0f; b = 1.0f; alpha = 0.9f;
                    break;
                case 1: // Middle ring - electric cyan
                    r = 0.4f; g = 0.9f; b = 1.0f; alpha = 0.7f;
                    break;
                default: // Outer ring - deep blue
                    r = 0.2f; g = 0.6f; b = 1.0f; alpha = 0.5f;
                    break;
            }

            // Create lightning arcs around the ring
            int arcsInRing = 12 + (ring * 4); // More arcs in outer rings
            for (int arc = 0; arc < arcsInRing; arc++) {
                float arcAngle = (float) ((arc / (float) arcsInRing) * 2 * Math.PI + ringRotation);

                // Position on the ring
                float x = (float) (Math.cos(arcAngle) * ringRadius);
                float z = (float) (Math.sin(arcAngle) * ringRadius);
                float y = (float) (Math.sin(arcAngle + ringTilt * Math.PI / 180.0f) * 0.3f);

                // Next position for arc
                float nextArcAngle = (float) (((arc + 1) / (float) arcsInRing) * 2 * Math.PI + ringRotation);
                float nextX = (float) (Math.cos(nextArcAngle) * ringRadius);
                float nextZ = (float) (Math.sin(nextArcAngle) * ringRadius);
                float nextY = (float) (Math.sin(nextArcAngle + ringTilt * Math.PI / 180.0f) * 0.3f);

                // Draw lightning arc between points
                drawLightningArc(context, consumer, matrix,
                        x, y, z, nextX, nextY, nextZ,
                        0.05f, r, g, b, alpha);
            }
        }
    }

    private void renderElectricCore(VertexContext context, VertexConsumer consumer, Matrix4f matrix, float smoothTime) {
        // Pulsing electric core at the center
        float coreIntensity = 0.8f + 0.2f * (float) Math.sin(smoothTime * 8);
        float coreRadius = 0.3f + 0.1f * (float) Math.sin(smoothTime * 6);

        // Bright electric core - pure white with electric blue edge
        float coreR = 1.0f, coreG = 1.0f, coreB = 1.0f;
        float edgeR = 0.3f, edgeG = 0.8f, edgeB = 1.0f;

        // Draw core sphere using multiple rotating beams
        for (int i = 0; i < 8; i++) {
            float rotation = (float) (smoothTime * 4.0f * (float) Math.PI + (i * Math.PI / 4.0f));

            drawRotatingBeamSides(context, consumer, matrix, coreRadius, 0.6f,
                    coreR, coreG, coreB, coreIntensity, 0.0f, rotation);
        }

        // Edge glow
        for (int i = 0; i < 6; i++) {
            float rotation = (float) (smoothTime * -3.0f * (float) Math.PI + (i * Math.PI / 3.0f));

            drawRotatingBeamSides(context, consumer, matrix, coreRadius * 1.5f, 0.8f,
                    edgeR, edgeG, edgeB, coreIntensity * 0.4f, 0.0f, rotation);
        }
    }

    private void drawLightningArc(VertexContext context, VertexConsumer consumer, Matrix4f matrix,
                                  float x1, float y1, float z1, float x2, float y2, float z2,
                                  float width, float r, float g, float b, float alpha) {
        float halfWidth = width * 0.5f;

        // Calculate perpendicular vector for width
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (length < 0.001f) return; // Avoid division by zero

        // Normalize direction
        dx /= length;
        dz /= length;

        // Create perpendicular vector
        float px = -dz * halfWidth;
        float pz = dx * halfWidth;

        // Draw quad for the lightning arc
        context.fillGradientWithTexture(consumer, matrix,
                x1 - px, y1, z1 - pz, 0, 0, r, g, b, alpha,
                x1 + px, y1, z1 + pz, 0.125f, 0, r, g, b, alpha,
                x2 + px, y2, z2 + pz, 0.125f, 1, r, g, b, alpha,
                x2 - px, y2, z2 - pz, 0, 1, r, g, b, alpha);
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

    @Override
    public Identifier getTexture(ThunderSphereEntity entity) {
        return TEXTURE;
    }
    // Shared Lightning Render Layer - can be used by other thunder entities
    public static RenderLayer getLightningRenderLayer() {
        return RenderLayer.of(
                "lightning_beam",
                VertexFormats.POSITION_COLOR_TEXTURE_LIGHT,
                VertexFormat.DrawMode.QUADS,
                1536,
                false,
                true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.POSITION_COLOR_TEXTURE_LIGHTMAP_PROGRAM)
                        .texture(new RenderLayer.Texture(Mamy.id("textures/vfx/lightning_alpha.png"), false, false))
                        .transparency(RenderLayer.ADDITIVE_TRANSPARENCY)
                        .depthTest(RenderLayer.LEQUAL_DEPTH_TEST)
                        .cull(RenderLayer.DISABLE_CULLING)
                        .writeMaskState(RenderLayer.COLOR_MASK)
                        .lightmap(RenderLayer.ENABLE_LIGHTMAP)
                        .target(RenderLayer.MAIN_TARGET)
                        .build(false)
        );
    }

}