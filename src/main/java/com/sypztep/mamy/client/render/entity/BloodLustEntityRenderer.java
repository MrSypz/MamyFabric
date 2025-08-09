package com.sypztep.mamy.client.render.entity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.BloodLustEntity;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class BloodLustEntityRenderer<T extends BloodLustEntity> extends EntityRenderer<T> {
    private static final Identifier TEXTURE = Mamy.id( "textures/entity/bloodlust.png");
    public BloodLustEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(T entity) {
        return TEXTURE;
    }


    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider, int light) {
        matrices.push();

        // Rotate entity to match its pitch and yaw
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch())));

        // Slash size
        matrices.scale(0.5F, 0.5F, 0.5F);

        VertexConsumer buffer = provider.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        MatrixStack.Entry entry = matrices.peek();

        float u0 = 0.0F;
        float u1 = 1.0F;
        float v0 = 0.0F;
        float v1 = 1.0F;

        // Quad dimensions (length x thickness)
        float halfLength = 8.0F;  // 16 wide
        float halfHeight = 8.0F;  // 2 thick

        // --- Top face ---
        buffer.vertex(entry.getPositionMatrix(), -halfLength,  halfHeight, 0)
                .color(255, 255, 255, 255)
                .texture(u0, v0)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, 0, 1, 0);
        buffer.vertex(entry.getPositionMatrix(), -halfLength, -halfHeight, 0)
                .color(255, 255, 255, 255)
                .texture(u0, v1)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, 0, 1, 0);
        buffer.vertex(entry.getPositionMatrix(),  halfLength, -halfHeight, 0)
                .color(255, 255, 255, 255)
                .texture(u1, v1)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, 0, 1, 0);
        buffer.vertex(entry.getPositionMatrix(),  halfLength,  halfHeight, 0)
                .color(255, 255, 255, 255)
                .texture(u1, v0)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, 0, 1, 0);

        // --- Bottom face (slightly offset to avoid z-fighting) ---
        float offset = -0.05F;
        buffer.vertex(entry.getPositionMatrix(),  halfLength,  halfHeight + offset, 0)
                .color(255, 255, 255, 255)
                .texture(u1, v0)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, 0, -1, 0);
        buffer.vertex(entry.getPositionMatrix(),  halfLength, -halfHeight + offset, 0)
                .color(255, 255, 255, 255)
                .texture(u1, v1)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, 0, -1, 0);
        buffer.vertex(entry.getPositionMatrix(), -halfLength, -halfHeight + offset, 0)
                .color(255, 255, 255, 255)
                .texture(u0, v1)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, 0, -1, 0);
        buffer.vertex(entry.getPositionMatrix(), -halfLength,  halfHeight + offset, 0)
                .color(255, 255, 255, 255)
                .texture(u0, v0)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, 0, -1, 0);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, provider, light);
    }
}
