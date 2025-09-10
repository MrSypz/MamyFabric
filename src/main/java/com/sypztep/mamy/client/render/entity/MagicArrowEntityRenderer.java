package com.sypztep.mamy.client.render.entity;

import com.sypztep.mamy.client.render.VertexContext;
import com.sypztep.mamy.common.entity.skill.MagicArrowEntity;
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
public class MagicArrowEntityRenderer extends EntityRenderer<MagicArrowEntity> {
    private static final Identifier MAGIC_TEXTURE = Identifier.ofVanilla("textures/entity/end_crystal/end_crystal_beam.png");

    public MagicArrowEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(MagicArrowEntity entity) {
        return MAGIC_TEXTURE;
    }

    private static RenderLayer createBeamColorLayer(boolean affectsOutline) {
        RenderLayer.MultiPhaseParameters params = RenderLayer.MultiPhaseParameters.builder()
                .program(RenderLayer.COLOR_PROGRAM)
                .transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
                .writeMaskState(affectsOutline ? RenderLayer.COLOR_MASK : RenderLayer.ALL_MASK)
                .cull(RenderLayer.DISABLE_CULLING)
                .lightmap(RenderLayer.ENABLE_LIGHTMAP)
                .build(false);

        return RenderLayer.of(
                "translucent_beam",
                VertexFormats.POSITION_COLOR,
                VertexFormat.DrawMode.QUADS,
                1536,
                false,
                true,
                params
        );
    }
    private static final RenderLayer BEAM_NORMAL_LAYER = createBeamColorLayer(true);

    @Override
    public void render(MagicArrowEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // Rotate based on entity's yaw and pitch (like trident)
        float lerpedYaw = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
        float lerpedPitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(lerpedYaw - 90.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(lerpedPitch + 90.0f));

        renderMagicBeam(matrices, vertexConsumers, entity);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void renderMagicBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, MagicArrowEntity entity) {
        matrices.push();

        float width = 0.2f;
        float height = 1.5f; // Length of the arrow beam
        float halfWidth = width / 2.0f;
        VertexContext context = new VertexContext(matrices, vertexConsumers);

        float alpha = 0.8f;
        float endAlpha = 0.0f;

        // Get vertex consumer and matrix
        VertexConsumer consumer = vertexConsumers.getBuffer(BEAM_NORMAL_LAYER);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Rotation based on velocity speed
        float speed = (float) entity.getVelocity().length();
        float outerRotation = (entity.age / 20.0f) * speed * 2.0f * (float) Math.PI;
        float innerRotation = (entity.age / 20.0f) * speed * 3.0f * (float) Math.PI;

        // Magic colors - purple/blue
        float r = 0.5f, g = 0.2f, b = 1.0f;

        // Draw the outer beam
        drawRotatingBeamSides(context, consumer, matrix, halfWidth, height,
                r, g, b, alpha, endAlpha, outerRotation);

        // Draw the inner beam with the same color but slightly dimmer
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

        float x1 = -halfWidth * cos - (-halfWidth) * sin;  // front-left
        float z1 = -halfWidth * sin + (-halfWidth) * cos;

        float x2 = halfWidth * cos - (-halfWidth) * sin;   // front-right
        float z2 = halfWidth * sin + (-halfWidth) * cos;

        float x3 = halfWidth * cos - halfWidth * sin;      // back-right
        float z3 = halfWidth * sin + halfWidth * cos;

        float x4 = -halfWidth * cos - halfWidth * sin;     // back-left
        float z4 = -halfWidth * sin + halfWidth * cos;

        // Draw the four sides of the beam - your exact pattern
        drawBeamSide(context, consumer, matrix, x1, z1, x2, z2, height, r, g, b, alpha, endAlpha);
        drawBeamSide(context, consumer, matrix, x3, z3, x4, z4, height, r, g, b, alpha, endAlpha);
        drawBeamSide(context, consumer, matrix, x4, z4, x1, z1, height, r, g, b, alpha, endAlpha);
        drawBeamSide(context, consumer, matrix, x2, z2, x3, z3, height, r, g, b, alpha, endAlpha);
    }

    private void drawBeamSide(VertexContext context, VertexConsumer consumer, Matrix4f matrix,
                              float x1, float z1, float x2, float z2, float height,
                              float r, float g, float b, float alpha, float endAlpha) {
        context.fillGradient(consumer, matrix,
                x1, 0, z1, r, g, b, alpha,
                x1, height, z1, r, g, b, endAlpha,
                x2, height, z2, r, g, b, endAlpha,
                x2, 0, z2, r, g, b, alpha);
    }
}