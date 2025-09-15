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
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class ThunderSphereEntityRenderer extends EntityRenderer<ThunderSphereEntity> {
    private static final Identifier TEXTURE = Mamy.id("textures/entity/empty.png");
    private static final float SPHERE_RADIUS = 1.0f;

    public ThunderSphereEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(ThunderSphereEntity entity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        renderElectricSphere(matrices, vertexConsumers, entity, tickDelta);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void renderElectricSphere(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                      ThunderSphereEntity entity, float tickDelta) {
        matrices.push();

        VertexContext context = new VertexContext(matrices, vertexConsumers);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float smoothTime = (entity.age + tickDelta) * 0.05f;

        VertexConsumer consumer = vertexConsumers.getBuffer(getLightningRenderLayer());

        // Render main electric sphere
        renderElectricCore(context, consumer, matrix, smoothTime);

        // Render rotating lightning arcs around the sphere
        renderLightningArcs(context, consumer, matrix, smoothTime);

        matrices.pop();
    }

    private void renderElectricCore(VertexContext context, VertexConsumer consumer, Matrix4f matrix, float smoothTime) {
        float coreIntensity = 0.8f + 0.2f * (float) Math.sin(smoothTime * 8);
        float radius = SPHERE_RADIUS * (0.9f + 0.1f * (float) Math.sin(smoothTime * 6));

        float coreR = 0.4f, coreG = 0.8f, coreB = 1.0f;
        float edgeR = 1.0f, edgeG = 1.0f, edgeB = 1.0f;

        int faces = 12; // Number of faces to create sphere illusion
        for (int i = 0; i < faces; i++) {
            float rotationY = smoothTime * 2.0f * (float)Math.PI + i * (float)Math.PI / faces;
            float rotationX = smoothTime * 1.5f * (float)Math.PI + i * (float)Math.PI / (faces * 0.5f);

            drawSphereFace(context, consumer, matrix, radius,
                    coreR, coreG, coreB, edgeR, edgeG, edgeB,
                    coreIntensity, rotationY, rotationX);
        }
    }

    private void drawSphereFace(VertexContext context, VertexConsumer consumer, Matrix4f matrix,
                                float radius,
                                float coreR, float coreG, float coreB,
                                float edgeR, float edgeG, float edgeB,
                                float intensity, float rotationY, float rotationX) {

        float sin = (float) Math.sin(rotationY);
        float cos = (float) Math.cos(rotationY);
        float sinX = (float) Math.sin(rotationX);
        float cosX = (float) Math.cos(rotationX);

        // Create quad vertices for sphere face
        float halfSize = radius * 0.7f;

        float x1 = -halfSize * cos * cosX;
        float y1 = -halfSize * sinX;
        float z1 = -halfSize * sin * cosX;

        float x2 = halfSize * cos * cosX;
        float y2 = -halfSize * sinX;
        float z2 = halfSize * sin * cosX;

        float x3 = halfSize * cos * cosX;
        float y3 = halfSize * sinX;
        float z3 = halfSize * sin * cosX;

        float x4 = -halfSize * cos * cosX;
        float y4 = halfSize * sinX;
        float z4 = -halfSize * sin * cosX;

        // UV coordinates for sphere mapping
        float u1 = 0.0f, v1 = 0.0f;
        float u2 = 0.125f, v2 = 1.0f;

        // Draw the face with gradient from core to edge
        context.fillGradientWithTexture(consumer, matrix,
                x1, y1, z1, u1, v1, edgeR, edgeG, edgeB, intensity * 0.6f,
                x2, y2, z2, u2, v1, edgeR, edgeG, edgeB, intensity * 0.6f,
                x3, y3, z3, u2, v2, coreR, coreG, coreB, intensity,
                x4, y4, z4, u1, v2, coreR, coreG, coreB, intensity);
    }

    private void renderLightningArcs(VertexContext context, VertexConsumer consumer, Matrix4f matrix,
                                     float smoothTime) {
        int arcCount = 6;
        int segments = 16; // fewer needed with jitter

        float arcRadius = SPHERE_RADIUS * 1.5f;

        // Global spin for all arcs
        float globalSpin = smoothTime * 5f;
        float cosSpin = (float) Math.cos(globalSpin);
        float sinSpin = (float) Math.sin(globalSpin);

        for (int arc = 0; arc < arcCount; arc++) {
            // Unique tilt per arc
            float tilt = (arc * (float)Math.PI / arcCount) + 0.6f;
            float cosTilt = (float)Math.cos(tilt);
            float sinTilt = (float)Math.sin(tilt);

            // Color variation per arc
            float r = 0.4f + 0.6f * (float)Math.sin(smoothTime * 3 + arc);
            float g = 0.7f + 0.3f * (float)Math.cos(smoothTime * 2 + arc);
            float b = 1.0f;
            float alpha = 0.5f + 0.5f * (float)Math.sin(smoothTime * 4 + arc);

            float prevX = 0, prevY = 0, prevZ = 0;
            boolean first = true;

            for (int i = 0; i <= segments; i++) {
                float t = (float)i / segments;

                // Base circle around sphere
                float phi = t * (float)Math.PI * 2;
                float x = (float)(arcRadius * Math.cos(phi));
                float y = 0;
                float z = (float)(arcRadius * Math.sin(phi));

                // Apply tilt (rotate around X)
                float ty = cosTilt * y - sinTilt * z;
                float tz = sinTilt * y + cosTilt * z;
                y = ty; z = tz;

                // Apply global spin (rotate around Y)
                float rx = cosSpin * x + sinSpin * z;
                float rz = -sinSpin * x + cosSpin * z;
                x = rx; z = rz;

                // Add jitter for lightning feel
                float jitter = 0.2f * (float)Math.sin(smoothTime * 10 + t * 12.0f + arc * 5);
                x += jitter * (float)Math.cos(phi * 3 + arc);
                y += jitter * (float)Math.sin(phi * 5 + arc);

                if (!first) {
                    drawLightningSegment(context, consumer, matrix,
                            prevX, prevY, prevZ,
                            x, y, z,
                            0.06f, r, g, b, alpha);
                }
                prevX = x;
                prevY = y;
                prevZ = z;
                first = false;
            }
        }
    }

    private void drawLightningSegment(VertexContext context, VertexConsumer consumer, Matrix4f matrix,
                                      float x1, float y1, float z1, float x2, float y2, float z2,
                                      float width, float r, float g, float b, float alpha) {
        float halfWidth = width * 0.5f;

        // Calculate perpendicular vector for width
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (length < 0.001f) return;

        // Normalize direction
        dx /= length;
        dz /= length;

        // Create perpendicular vector
        float px = -dz * halfWidth;
        float pz = dx * halfWidth;

        // Draw quad for the lightning segment
        context.fillGradientWithTexture(consumer, matrix,
                x1 - px, y1, z1 - pz, 0, 0, r, g, b, alpha,
                x1 + px, y1, z1 + pz, 0.125f, 0, r, g, b, alpha,
                x2 + px, y2, z2 + pz, 0.125f, 1, r, g, b, alpha * 0.6f,
                x2 - px, y2, z2 - pz, 0, 1, r, g, b, alpha * 0.6f);
    }

    @Override
    public Identifier getTexture(ThunderSphereEntity entity) {
        return TEXTURE;
    }

    // Lightning Render Layer using cleck.png texture like magic arrow
    public static RenderLayer getLightningRenderLayer() {
        return RenderLayer.of(
                "thunder_sphere",
                VertexFormats.POSITION_COLOR_TEXTURE_LIGHT,
                VertexFormat.DrawMode.QUADS,
                1536,
                false,
                true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.POSITION_COLOR_TEXTURE_LIGHTMAP_PROGRAM)
                        .texture(new RenderLayer.Texture(Mamy.id("textures/vfx/cleck.png"), false, false))
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