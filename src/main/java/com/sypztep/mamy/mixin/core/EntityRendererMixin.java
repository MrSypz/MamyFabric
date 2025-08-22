package com.sypztep.mamy.mixin.core;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.event.living.MobSpawnStatsEvent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Shadow
    @Final
    protected EntityRenderDispatcher dispatcher;

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void renderLvl(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity))
            renderMonsterInfoIfPresent(entity, matrices, vertexConsumers, light, tickDelta);
    }

    @Unique
    protected void renderMonsterInfoIfPresent(T entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        double d = this.dispatcher.getSquaredDistanceToCamera(entity);
        if (d > 1024.0) return;
        if (entity != this.dispatcher.targetedEntity) return;

        Vec3d vec3d = entity.getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, entity.getYaw(tickDelta));
        if (vec3d == null) return;

        boolean bl = !entity.isSneaky();
        matrices.push();
        matrices.translate(vec3d.x, vec3d.y + 0.5, vec3d.z);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(0.025f, -0.025f, 0.025f);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float f = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f);
        int j = (int) (f * 255.0f) << 24;
        TextRenderer textRenderer = this.getTextRenderer();

        // Get monster info
        LivingEntity livingEntity = (LivingEntity) entity;
        LivingLevelComponent levelComp = ModEntityComponents.LIVINGLEVEL.getNullable(livingEntity);

        if (levelComp == null) {
            matrices.pop();
            return;
        }

        // Get level and rarity info
        int level = levelComp.getLevel();
        int rarityLevel = MobSpawnStatsEvent.getRarityLevel(livingEntity);

        // Create enhanced level display
        Text levelText = createEnhancedLevelDisplay(level, rarityLevel);

        // Get monster name (without the level prefix if it exists)
        String fullName = entity.getDisplayName().getString();
        String monsterName = cleanMonsterName(fullName);

        // Create name text with rarity color
        Text nameText = Text.literal(monsterName).formatted(getRarityNameColor(rarityLevel));

        // Combine level and name
        Text combinedText = Text.empty().append(levelText).append(" ").append(nameText);

        float g = (float) -textRenderer.getWidth(combinedText) / 2;

        // Render the combined text
        textRenderer.draw(combinedText, g, (float) -10, Colors.WHITE, false, matrix4f, vertexConsumers,
                bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, j, light);

        if (bl) {
            textRenderer.draw(combinedText, g, (float)-10, Colors.WHITE, false, matrix4f, vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL, 0, light);
        }

        matrices.pop();
    }

    @Unique
    private Text createEnhancedLevelDisplay(int level, int rarityLevel) {
        // Choose level bracket style and color based on rarity
        String prefix, suffix;
        Formatting levelColor;

        switch (rarityLevel) {
            case 0 -> { // Common
                prefix = "[Lv.";
                suffix = "]";
                levelColor = Formatting.GREEN;
            }
            case 1 -> { // Uncommon
                prefix = "⟨Lv.";
                suffix = "⟩";
                levelColor = Formatting.YELLOW;
            }
            case 2 -> { // Rare
                prefix = "⟪Lv.";
                suffix = "⟫";
                levelColor = Formatting.BLUE;
            }
            case 3 -> { // Epic
                prefix = "◆Lv.";
                suffix = "◆";
                levelColor = Formatting.LIGHT_PURPLE;
            }
            case 4 -> { // Legendary
                prefix = "★Lv.";
                suffix = "★";
                levelColor = Formatting.GOLD;
            }
            case 5 -> { // Mythic
                prefix = "⚡Lv.";
                suffix = "⚡";
                levelColor = Formatting.RED;
            }
            default -> {
                prefix = "[Lv.";
                suffix = "]";
                levelColor = Formatting.WHITE;
            }
        }

        return Text.literal(prefix + level + suffix).formatted(levelColor, Formatting.BOLD);
    }

    @Unique
    private String cleanMonsterName(String fullName) {
        // Remove level prefix if it exists (e.g., "[Lv.15] Zombie" → "Zombie")
        if (fullName.contains("] ")) {
            return fullName.substring(fullName.indexOf("] ") + 2);
        } else if (fullName.contains("⟩ ")) {
            return fullName.substring(fullName.indexOf("⟩ ") + 2);
        } else if (fullName.contains("⟫ ")) {
            return fullName.substring(fullName.indexOf("⟫ ") + 2);
        } else if (fullName.contains("◆ ") && fullName.indexOf("◆ ") > 0) {
            return fullName.substring(fullName.indexOf("◆ ") + 2);
        } else if (fullName.contains("★ ") && fullName.indexOf("★ ") > 0) {
            return fullName.substring(fullName.indexOf("★ ") + 2);
        } else if (fullName.contains("⚡ ") && fullName.indexOf("⚡ ") > 0) {
            return fullName.substring(fullName.indexOf("⚡ ") + 2);
        }
        return fullName;
    }

    @Unique
    private Formatting getRarityNameColor(int rarityLevel) {
        return switch (rarityLevel) {
            case 0 -> Formatting.WHITE;         // Common
            case 1 -> Formatting.GREEN;         // Uncommon
            case 2 -> Formatting.BLUE;          // Rare
            case 3 -> Formatting.LIGHT_PURPLE;  // Epic
            case 4 -> Formatting.GOLD;          // Legendary
            case 5 -> Formatting.RED;           // Mythic
            default -> Formatting.WHITE;
        };
    }
}