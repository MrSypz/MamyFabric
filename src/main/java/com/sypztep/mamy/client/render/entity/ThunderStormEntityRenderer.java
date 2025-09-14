package com.sypztep.mamy.client.render.entity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.skill.ThunderStormEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ThunderStormEntityRenderer extends EntityRenderer<ThunderStormEntity> {
    private static final Identifier TEXTURE = Mamy.id("textures/entity/empty.png");

    public ThunderStormEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(ThunderStormEntity entity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        // ไม่ต้อง render อะไรเพิ่ม เพราะใช้ SparkParticleEffect แทนแล้ว
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(ThunderStormEntity entity) {
        return TEXTURE;
    }
}