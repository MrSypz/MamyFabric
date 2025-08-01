package com.sypztep.mamy.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sypztep.mamy.client.util.WorldRenderUtil;
import com.sypztep.mamy.common.entity.BaseSkillEntity;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class SkillBoundingRenderer {
    private boolean isDebug = false;
    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(new SkillBoundingRenderer()::renderDebugHitboxes);
    }
    private void renderDebugHitboxes(WorldRenderContext context) {
        if (!isDebug) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        Vec3d camera = context.camera().getPos();

        // Debug: print when we find BloodLust entities
        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof BaseSkillEntity baseSkillEntity) {
                renderBloodLustHitbox(context, camera, baseSkillEntity);
            }
        }
    }

    private void renderBloodLustHitbox(WorldRenderContext context, Vec3d camera, BaseSkillEntity bloodLust) {
        Vec3d pos = bloodLust.getPos();
        double x = pos.x - camera.x;
        double y = pos.y - camera.y;
        double z = pos.z - camera.z;

        // Setup rendering state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(x, y, z);

        // Get boxes
        Box entityBox = bloodLust.getBoundingBox().offset(-pos.x, -pos.y, -pos.z);
        Box hitDetectionBox = bloodLust.getHitDetectionBox().offset(-pos.x, -pos.y, -pos.z);

        // Get vertex consumer
        VertexConsumerProvider.Immediate immediate = (VertexConsumerProvider.Immediate) context.consumers();
        if (immediate == null) {
            immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        }
        VertexConsumer buffer = immediate.getBuffer(RenderLayer.getDebugQuads());

        // Render entity collision box (yellow)
        WorldRenderUtil.drawBox(matrices, buffer, entityBox, 1.0f, 1.0f, 0.0f, 0.8f);

        // Render hit detection area (red for simple, blue for custom)
        boolean isCustom = bloodLust.skillConfig.useCustomBox();
        if (isCustom) {
            WorldRenderUtil.drawBox(matrices, buffer, hitDetectionBox, 0.0f, 0.5f, 1.0f, 0.4f); // Blue
        } else {
            WorldRenderUtil.drawBox(matrices, buffer, hitDetectionBox, 1.0f, 0.0f, 0.0f, 0.3f); // Red
        }

        immediate.draw();

        // Debug text
        matrices.translate(0, entityBox.getLengthY() + 0.5, 0);
        matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        String[] lines;
        if (isCustom) {
            lines = new String[]{
                    "§eBloodlust",
                    "§fMode: §bCustom Box",
                    String.format("§fEntity: §a%.1fx%.1fx%.1f",
                            entityBox.getLengthX(), entityBox.getLengthY(), entityBox.getLengthZ()),
                    String.format("§fHit Box: §b%.1fx%.1fx%.1f",
                            hitDetectionBox.getLengthX(), hitDetectionBox.getLengthY(), hitDetectionBox.getLengthZ()),
                    String.format("§fDimensions: §6%.1f/%.1f/%.1f",
                            bloodLust.skillConfig.hitWidth(), bloodLust.skillConfig.hitHeight(), bloodLust.skillConfig.hitDepth())
            };
        } else {
            lines = new String[]{
                    "§eBloodlust",
                    "§fMode: §cSimple Range",
                    String.format("§fEntity: §a%.1fx%.1fx%.1f",
                            entityBox.getLengthX(), entityBox.getLengthY(), entityBox.getLengthZ()),
                    String.format("§fHit Box: §c%.1fx%.1fx%.1f",
                            hitDetectionBox.getLengthX(), hitDetectionBox.getLengthY(), hitDetectionBox.getLengthZ()),
                    String.format("§fRange: §6%.1f", bloodLust.skillConfig.hitRange())
            };
        }

        // Render each line
        for (int i = 0; i < lines.length; i++) {
            MinecraftClient.getInstance().textRenderer.draw(
                    lines[i],
                    -MinecraftClient.getInstance().textRenderer.getWidth(lines[i]) / 2f,
                    i * 10,
                    0xFFFFFFFF,
                    false,
                    matrices.peek().getPositionMatrix(),
                    immediate,
                    net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL,
                    0,
                    0xF000F0
            );
        }

        immediate.draw();

        matrices.pop();

        // Restore state
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
