package com.sypztep.mamy.client.particle;

import com.sypztep.mamy.client.event.tooltip.ElementalTooltipHelper;
import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.system.damage.ElementType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class ElementalDamageParticle extends Particle {
    private static final float INITIAL_GRAVITY = 0.05f;

    private final String damageText;
    private final ElementType elementType;
    private final int textColor;
    private float initialGrav = INITIAL_GRAVITY;
    private final float weight;

    public ElementalDamageParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, String damageText, ElementType elementType, int color) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.collidesWithWorld = true;
        this.maxAge = 45;
        this.weight = 0.25f;
        this.gravityStrength = 0.981f;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;

        this.damageText = damageText;
        this.elementType = elementType;
        this.textColor = color;
        this.red = 1f;
        this.green = 1f;
        this.blue = 1f;
        this.alpha = 1f;
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

            this.alpha -= 0.5f;
        if (this.age >= 20) {
            this.velocityY -= gravityStrength * weight;
        } else if (this.velocityY > 0) {
            this.velocityY -= initialGrav;
            initialGrav += 0.01f;
            if (this.velocityY <= 0) {
                this.velocityY = -0.03;
            }
        }

        this.move(this.velocityX, this.velocityY, this.velocityZ);

        if (this.ascending && this.y == this.prevPosY) {
            this.velocityX *= 1.1;
            this.velocityZ *= 1.1;
        }

        this.velocityX *= this.velocityMultiplier - 0.15;
        this.velocityY *= this.velocityMultiplier;
        this.velocityZ *= this.velocityMultiplier - 0.15;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3d cameraPos = camera.getPos();
        float particleX = (float) (MathHelper.lerp(tickDelta, prevPosX, x) - cameraPos.x);
        float particleY = (float) (MathHelper.lerp(tickDelta, prevPosY, y) - cameraPos.y);
        float particleZ = (float) (MathHelper.lerp(tickDelta, prevPosZ, z) - cameraPos.z);

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
        // Idk testing in benchmark and it 3 time faster
        Matrix4f matrix = new Matrix4f()
                .translation(particleX, particleY, particleZ)
                .rotate(camera.getRotation())
                .rotate((float) Math.PI, 0.0F, 1.0F, 0.0F)
                .scale(-0.03f, -0.03f, 0.03f);

        // Create separate components
        MutableText iconText = ElementalTooltipHelper.createIconText(elementType.icon);
        MutableText damageTextComponent = Text.literal(damageText);

        // Calculate positioning
        float iconWidth = textRenderer.getWidth(iconText);
        float damageWidth = textRenderer.getWidth(damageTextComponent);
        float totalWidth = iconWidth + damageWidth;
        float startX = totalWidth / -2.0f;

        textRenderer.draw(iconText, startX + 1, 1,              // Shadow offset +1,+1
                TextRenderer.tweakTransparency(0x404040), // Dark gray shadow
                false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL,  // Shadow on NORMAL layer
                0, 0xF000F0);

        textRenderer.draw(iconText, startX, 0,                  // Normal position
                TextRenderer.tweakTransparency(0xFFFFFF), // White icon
                false, matrix, vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET,  // Main text on POLYGON_OFFSET layer
                0, 0xF000F0);

        int shadowColor = DrawContextUtils.darkenColor(textColor, 0.25f);

        textRenderer.draw(damageTextComponent, startX + iconWidth + 1, 1,  // Shadow offset +1,+1
                TextRenderer.tweakTransparency(shadowColor), // Darkened element color
                false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL,  // Shadow on NORMAL layer
                0, 0xF000F0);

        textRenderer.draw(damageTextComponent, startX + iconWidth, 0,      // Normal position
                TextRenderer.tweakTransparency(textColor), // Element color
                false, matrix, vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET,  // Main text on POLYGON_OFFSET layer
                0, 0xF000F0);

        vertexConsumers.draw();
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }
}