package com.sypztep.mamy.client.render.entity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.render.VertexContext;
import com.sypztep.mamy.common.entity.skill.MagicArrowEntity;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderer;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class MagicArrowEntityRenderer extends EntityRenderer<MagicArrowEntity> {
    private static final Identifier MAGIC_TEXTURE = Identifier.ofVanilla("textures/entity/end_crystal/end_crystal_beam.png");
    private static final Map<UUID, PointLightData> ACTIVE_LIGHTS = new HashMap<>();

    public MagicArrowEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(MagicArrowEntity entity) {
        return MAGIC_TEXTURE;
    }

    private static final RenderLayer FLAME_ALPHA_LAYER = createFlameAlphaLayer();

    private static RenderLayer createFlameAlphaLayer() {
        return RenderLayer.of(
                "flame_alpha_trail",
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
    public void render(MagicArrowEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        LightRenderer lightRenderer = VeilRenderSystem.renderer().getLightRenderer();

        PointLightData arrowLight = ACTIVE_LIGHTS.get(entity.getUuid());

        if (entity.isAlive() && !entity.isRemoved()) {
            if (arrowLight == null) {
                // create once
                arrowLight = new PointLightData();
                arrowLight.setColor(0xFF0FDDC9);
                arrowLight.setBrightness(2.0f);
                arrowLight.setRadius(8.0f);
                lightRenderer.addLight(arrowLight);

                ACTIVE_LIGHTS.put(entity.getUuid(), arrowLight);
            }

            arrowLight.setPosition(
                    (float) entity.getX(),
                    (float) entity.getY(),
                    (float) entity.getZ()
            );

        }

        matrices.push();

        float lerpedYaw = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
        float lerpedPitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(lerpedYaw - 90.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(lerpedPitch + 90.0f));

        renderMagicBeam(matrices, vertexConsumers, entity, tickDelta);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void renderMagicBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, MagicArrowEntity entity, float tickDelta) {
        matrices.push();

        float width = 0.2f;
        float height = 1.5f;
        float halfWidth = width / 2.0f;
        VertexContext context = new VertexContext(matrices, vertexConsumers);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float smoothTime = (entity.age + tickDelta) * 0.05f;
        float outerRotation = smoothTime * -2.0f * (float) Math.PI;
        float innerRotation = smoothTime * 3.0f * (float) Math.PI;

        VertexConsumer consumer = vertexConsumers.getBuffer(FLAME_ALPHA_LAYER);
        float alpha = 0.8f;
        float endAlpha = 0.0f;
        float r = 0.0588f, g = 0.8667f, b = 0.7882f;

        drawRotatingBeamSides(context, consumer, matrix, halfWidth, height,
                r, g, b, alpha, endAlpha, outerRotation);

        float innerWidth = halfWidth * 0.5f;
        drawRotatingBeamSides(context, consumer, matrix, innerWidth, height,
                r, g, b, alpha * 0.8f, endAlpha, innerRotation);

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
    public static void cleanupLight(UUID uuid) {
        PointLightData light = ACTIVE_LIGHTS.remove(uuid);
        if (light != null) VeilRenderSystem.renderer().getLightRenderer().free();
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