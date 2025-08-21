package com.sypztep.mamy.mixin.core;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.StatTypes;
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

        // Calculate monster info
        LivingEntity livingEntity = (LivingEntity) entity;
        LivingLevelComponent levelComp = ModEntityComponents.LIVINGLEVEL.getNullable(livingEntity);

        String monsterName = entity.getDisplayName().getString();
        String levelText = "";
        String gearscoreText = "";
        int gearscoreColor = Colors.WHITE;

        if (levelComp != null) {
            int level = levelComp.getLevel();
            int gearscore = calculateMonsterGearscore(levelComp);
            gearscoreColor = getMonsterGearscoreColor(gearscore);

            levelText = " [Lv." + level + "]";
            gearscoreText = " (GS: " + formatGearscore(gearscore) + ")";
        }

        // Create display texts
        Text nameText = Text.literal(monsterName).formatted(Formatting.RED);
        Text levelDisplay = Text.literal(levelText).formatted(Formatting.YELLOW);
        Text gearscoreDisplay = Text.literal(gearscoreText).withColor(gearscoreColor);

        // Combine texts
        Text combinedText = Text.empty().append(nameText).append(levelDisplay).append(gearscoreDisplay);

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
    private int calculateMonsterGearscore(LivingLevelComponent levelComp) {
        int totalScore = 0;

        // Calculate base stat score (same formula as player)
        for (StatTypes statType : StatTypes.values()) {
            int statValue = levelComp.getStatValue(statType);
            int statScore = statValue * getStatMultiplier(statType);
            totalScore += statScore;
        }

        return totalScore;
    }

    @Unique
    private int getStatMultiplier(StatTypes statType) {
        return switch (statType) {
            case STRENGTH -> 12;     // High damage impact
            case DEXTERITY -> 10;    // Accuracy + projectile damage
            case VITALITY -> 8;      // Survivability
            case INTELLIGENCE -> 11; // Magic damage + resources
            case AGILITY -> 9;       // Evasion + attack speed
            case LUCK -> 7;          // Crit chance + hybrid bonuses
        };
    }

    @Unique
    private String formatGearscore(int gearscore) {
        if (gearscore >= 1000) {
            return String.format("%,d", gearscore);
        }
        return String.valueOf(gearscore);
    }

    @Unique
    private int getMonsterGearscoreColor(int gearscore) {
        if (gearscore < 500) return 0xFF808080;      // Gray - Weak
        if (gearscore < 1500) return 0xFFFFFFFF;     // White - Normal
        if (gearscore < 3000) return 0xFF00FF00;     // Green - Strong
        if (gearscore < 5000) return 0xFF0080FF;     // Blue - Elite
        if (gearscore < 8000) return 0xFF9932CC;     // Purple - Boss
        return 0xFFFFD700;                           // Gold - Legendary
    }
}