package com.sypztep.mamy.client.render.entity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.skill.HealingLightEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class HealingLightEntityRenderer extends EntityRenderer<HealingLightEntity> {
    private static final Identifier HEALING_GRADIENT_TEXTURE = Mamy.id("textures/entity/healing_gradient.png");

    private static final RenderLayer HEALING_CYLINDER = RenderLayer.of(
            "healing_cylinder_translucent_emissive",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            1536,
            true,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM)
                    .texture(new RenderPhase.Texture(HEALING_GRADIENT_TEXTURE, false, false))
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .cull(RenderPhase.DISABLE_CULLING)
                    .writeMaskState(RenderPhase.COLOR_MASK)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .lineWidth(RenderPhase.FULL_LINE_WIDTH)
                    .build(false)
    );

    private static final float SQUARE_SIZE = 1.0f;
    private static final float SQUARE_HEIGHT = 2.0f;

    private float currentHeight = 0.0f;

    public HealingLightEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(HealingLightEntity entity) {
        return HEALING_GRADIENT_TEXTURE;
    }

    @Override
    public void render(HealingLightEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {

        matrices.push();

        float intensity = entity.getIntensity();

        float targetHeight = SQUARE_HEIGHT * intensity;

        currentHeight = MathHelper.lerp(0.05f, currentHeight, targetHeight);

        if (intensity <= 0.0f || entity.isRemoved()) {
            currentHeight = MathHelper.lerp(0.7f, currentHeight, 0.0f);
        }

        if (currentHeight >= 0.01f) {
            renderSquares(matrices, vertexConsumers, entity, tickDelta, intensity);
        }

        matrices.pop();
    }

    private void renderSquares(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                               HealingLightEntity entity, float tickDelta, float intensity) {

        float time = (entity.age + tickDelta) * 0.1f;

        float outerRotation = -time * 0.05f;
        renderSquare(matrices, vertexConsumers, SQUARE_SIZE, outerRotation, intensity);

        float innerRotation = time * 3.0f;
        renderSquare(matrices, vertexConsumers, 0.5f, innerRotation, intensity * 0.8f);
    }

    private void renderSquare(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                              float size, float rotation, float intensity) {

        VertexConsumer buffer = vertexConsumers.getBuffer(HEALING_CYLINDER);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        int color = getHealingColor(intensity);
        int light = 0xF000F0; // Full brightness for emissive effect
        int overlay = 0;

        float sin = MathHelper.sin(rotation);
        float cos = MathHelper.cos(rotation);
        float halfSize = size * 0.5f;

        // Calculate the four corners of the rotated square
        float x1 = -halfSize * cos + halfSize * sin;
        float z1 = -halfSize * sin - halfSize * cos;
        float x2 = halfSize * cos + halfSize * sin;
        float z2 = halfSize * sin - halfSize * cos;
        float x3 = halfSize * cos - halfSize * sin;
        float z3 = halfSize * sin + halfSize * cos;
        float x4 = -halfSize * cos - halfSize * sin;
        float z4 = -halfSize * sin + halfSize * cos;

        // Render the four sides of the square
        renderSquareSide(buffer, matrix, x1, z1, x2, z2, color, light, overlay);
        renderSquareSide(buffer, matrix, x2, z2, x3, z3, color, light, overlay);
        renderSquareSide(buffer, matrix, x3, z3, x4, z4, color, light, overlay);
        renderSquareSide(buffer, matrix, x4, z4, x1, z1, color, light, overlay);
    }

    private void renderSquareSide(VertexConsumer buffer, Matrix4f matrix,
                                  float x1, float z1, float x2, float z2,
                                  int color, int light, int overlay) {

        // Calculate normal (pointing outward from the side)
        float dx = x2 - x1;
        float dz = z2 - z1;
        float length = MathHelper.sqrt(dx * dx + dz * dz);
        float nx = -dz / length; // Perpendicular to the side
        float nz = dx / length;

        // Bottom vertices (full opacity)
        buffer.vertex(matrix, x1, 0, z1)
                .color(color)
                .texture(0.0f, 1.0f) // Bottom of gradient texture
                .overlay(overlay)
                .light(light)
                .normal(nx, 0, nz);

        buffer.vertex(matrix, x2, 0, z2)
                .color(color)
                .texture(0.125f, 1.0f) // Bottom of gradient texture
                .overlay(overlay)
                .light(light)
                .normal(nx, 0, nz);

        // Top vertices (nearly transparent)
        buffer.vertex(matrix, x2, currentHeight, z2)
                .color(color)
                .texture(0.125f, 0.02f) // Nearly transparent
                .overlay(overlay)
                .light(light)
                .normal(nx, 0, nz);

        buffer.vertex(matrix, x1, currentHeight, z1)
                .color(color)
                .texture(0.0f, 0.02f) // Nearly transparent
                .overlay(overlay)
                .light(light)
                .normal(nx, 0, nz);
    }

    private int getHealingColor(float intensity) {
        int red = 25;
        int green = 204;
        int blue = 255;
        int alpha = (int)(MathHelper.clamp(intensity, 0.0f, 1.0f) * 255);

        return ColorHelper.Argb.getArgb(alpha, red, green, blue);
    }
}