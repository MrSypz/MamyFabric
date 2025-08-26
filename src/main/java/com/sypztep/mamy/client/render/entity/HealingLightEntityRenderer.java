package com.sypztep.mamy.client.render.entity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.entity.skill.HealingLightEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class HealingLightEntityRenderer extends EntityRenderer<HealingLightEntity> {
    private static final Identifier TEXTURE = Mamy.id("textures/entity/bloodlust.png");
    private static final Identifier HEALING_GRADIENT_TEXTURE = Mamy.id("textures/entity/healing_gradient.png");

    // Custom render layer for healing cylinder with texture support
    private static final RenderLayer HEALING_CYLINDER = RenderLayer.of(
            "healing_cylinder_translucent_emissive",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            1536,
            true,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM)
                    .texture(new RenderPhase.Texture(HEALING_GRADIENT_TEXTURE, true, false))
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .cull(RenderPhase.DISABLE_CULLING)
                    .writeMaskState(RenderPhase.COLOR_MASK)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .build(false)
    );
    private static final float CYLINDER_RADIUS = 1f;
    private static final float CYLINDER_HEIGHT = 2f;
    private static final int CYLINDER_SEGMENTS = 16;

    public HealingLightEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(HealingLightEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(HealingLightEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {

        matrices.push();

        float intensity = entity.getIntensity();

        renderCylinder(matrices, vertexConsumers, intensity, light);

        matrices.pop();
    }

    private void renderCylinder(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float intensity,int light) {
        VertexConsumer buffer = vertexConsumers.getBuffer(HEALING_CYLINDER);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float angleStep = (float) (2 * Math.PI / CYLINDER_SEGMENTS);
        int color = getHealingColor(intensity, 1.0f);
//        int light = 0xF000F0; // Full brightness for emissive effect
        int overlay = 0;

        // Render cylinder wall with squares arranged in circle
        for (int i = 0; i < CYLINDER_SEGMENTS; i++) {
            float angle1 = i * angleStep;
            float angle2 = (i + 1) * angleStep;

            float x1 = CYLINDER_RADIUS * MathHelper.cos(angle1);
            float z1 = CYLINDER_RADIUS * MathHelper.sin(angle1);
            float x2 = CYLINDER_RADIUS * MathHelper.cos(angle2);
            float z2 = CYLINDER_RADIUS * MathHelper.sin(angle2);

            // Calculate texture coordinates for gradient
            float u1 = (float) i / CYLINDER_SEGMENTS;
            float u2 = (float) (i + 1) / CYLINDER_SEGMENTS;

            // Normal pointing outward from cylinder
            float nx1 = MathHelper.cos(angle1);
            float nz1 = MathHelper.sin(angle1);
            float nx2 = MathHelper.cos(angle2);
            float nz2 = MathHelper.sin(angle2);

            // Create rectangular quad for cylinder wall
            // Bottom vertices (v=0, full opacity in texture)
            buffer.vertex(matrix, x1, 0, z1)
                    .color(color)
                    .texture(u1, 1.0f) // Bottom of gradient texture
                    .overlay(overlay)
                    .light(light)
                    .normal(nx1, 0, nz1);

            buffer.vertex(matrix, x2, 0, z2)
                    .color(color)
                    .texture(u2, 1.0f) // Bottom of gradient texture
                    .overlay(overlay)
                    .light(light)
                    .normal(nx2, 0, nz2);

            // Top vertices (v=1, transparent in texture)
            buffer.vertex(matrix, x2, CYLINDER_HEIGHT, z2)
                    .color(color)
                    .texture(u2, 0.0f) // Top of gradient texture
                    .overlay(overlay)
                    .light(light)
                    .normal(nx2, 0, nz2);

            buffer.vertex(matrix, x1, CYLINDER_HEIGHT, z1)
                    .color(color)
                    .texture(u1, 0.0f) // Top of gradient texture
                    .overlay(overlay)
                    .light(light)
                    .normal(nx1, 0, nz1);
        }
    }

    private int getHealingColor(float intensity, float alpha) {
        float r = MathHelper.clamp(0.6f * intensity, 0.0f, 1.0f);
        float g = MathHelper.clamp(1.0f * intensity, 0.0f, 1.0f);
        float b = MathHelper.clamp(0.7f * intensity, 0.0f, 1.0f);
        float a = alpha; // Full alpha, texture will handle gradient

        return ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }
}