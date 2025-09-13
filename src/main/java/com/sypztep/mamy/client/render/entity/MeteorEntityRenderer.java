package com.sypztep.mamy.client.render.entity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.render.VertexContext;
import com.sypztep.mamy.common.entity.skill.MeteorEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class MeteorEntityRenderer extends EntityRenderer<MeteorEntity> {
    private static final Identifier TEXTURE = Mamy.id("textures/entity/empty.png");

    public MeteorEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    private static final RenderLayer FLAME_ALPHA_LAYER = createFlameAlphaLayer();

    private static RenderLayer createFlameAlphaLayer() {
        return RenderLayer.of(
                "meteor_flame_trail",
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
    public void render(MeteorEntity entity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // Render magma block
        renderMagmaBlock(entity, matrices, vertexConsumers, light);

        // Render fire effects around the meteor
        renderFireEffects(entity, tickDelta, matrices, vertexConsumers);

        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void renderMagmaBlock(MeteorEntity entity, MatrixStack matrices,
                                  VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        float scale = 8f; // Large meteor size
        matrices.scale(scale, scale, scale);
        matrices.translate(-0.5f, 0.0f, -0.5f);

        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        blockRenderManager.renderBlockAsEntity(
                Blocks.MAGMA_BLOCK.getDefaultState(),
                matrices,
                vertexConsumers,
                light,
                0
        );

        matrices.pop();
    }

    private void renderFireEffects(MeteorEntity entity, float tickDelta,
                                   MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        matrices.push();

        VertexContext context = new VertexContext(matrices, vertexConsumers);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float smoothTime = (entity.age + tickDelta) * 0.05f;
        float outerRotation = smoothTime * -1.5f * (float) Math.PI;
        float innerRotation = smoothTime * 1.5f * (float) Math.PI;

        VertexConsumer consumer = vertexConsumers.getBuffer(FLAME_ALPHA_LAYER);

        // Fire aura around meteor - much larger than firebolt
        float alpha = 0.8f;
        float endAlpha = 0.0f;

        // Outer fire layer - deep red/orange
        float outerR = 1.0f, outerG = 0.4f, outerB = 0.0f;

        // Inner fire layer - bright orange/yellow
        float innerR = 1.0f, innerG = 0.8f, innerB = 0.2f;

        // Large outer flame aura
        drawRotatingBeamSides(context, consumer, matrix, 6.0f, 20.0f,
                outerR, outerG, outerB, alpha * 0.6f, endAlpha, outerRotation);

        // Medium inner flame layer
        drawRotatingBeamSides(context, consumer, matrix, 4.0f, 18.0f,
                innerR, innerG, innerB, alpha * 0.8f, endAlpha, innerRotation);

        // Bright center core
        drawRotatingBeamSides(context, consumer, matrix, 2.0f, 16.0f,
                1.0f, 1.0f, 0.6f, alpha, endAlpha, innerRotation * 1.2f);

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
        float u1 = 0, v1 = -0.15f;
        float u2 = 1.0f, v2 = 1.0f;

        context.fillGradientWithTexture(consumer, matrix,
                x1, 0, z1, u1, v2, r, g, b, alpha,
                x1, height, z1, u1, v1, r, g, b, endAlpha,
                x2, height, z2, u2, v1, r, g, b, endAlpha,
                x2, 0, z2, u2, v2, r, g, b, alpha);
    }

    @Override
    public Identifier getTexture(MeteorEntity entity) {
        return TEXTURE;
    }
}