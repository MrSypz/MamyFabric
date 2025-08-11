package com.sypztep.mamy.client.util;

import com.sypztep.mamy.client.particle.TextParticle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.function.Supplier;

public final class TextParticleProvider {
    private static final TextParticleProvider[] REGISTRY = new TextParticleProvider[16];
    private static int nextId = 0;

    private final int id;
    private final Text text;
    private final Color color;
    private final float maxSize;
    private final float yPos;
    private final Supplier<Boolean> configSupplier;

    public static class Builder {
        private final Text text;
        private Color color = Color.WHITE;
        private float maxSize = -0.045f;
        private float yPos = 0f;
        private Supplier<Boolean> configSupplier = () -> true;

        public Builder(Text text) {
            this.text = text;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public Builder maxSize(float maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public Builder yPos(float yPos) {
            this.yPos = yPos;
            return this;
        }

        public Builder config(Supplier<Boolean> configSupplier) {
            this.configSupplier = configSupplier != null ? configSupplier : () -> true;
            return this;
        }

        public TextParticleProvider build() {
            return new TextParticleProvider(text, color, maxSize, yPos, configSupplier);
        }
    }

    private TextParticleProvider(Text text, Color color, float maxSize, float yPos, Supplier<Boolean> configSupplier) {
        if (nextId >= REGISTRY.length) throw new IllegalStateException("Too many TextParticleProviders registered! Max: " + REGISTRY.length + "Go increasing the size in array");
        this.id = nextId++;
        this.text = text;
        this.color = color;
        this.maxSize = maxSize;
        this.yPos = yPos;
        this.configSupplier = configSupplier;
        REGISTRY[this.id] = this;
    }

    public static Builder builder(Text text) {
        return new Builder(text);
    }

    public static TextParticleProvider register(Text text) {
        return builder(text).build();
    }

    public static TextParticleProvider register(Text text, float maxSize) {
        return builder(text).maxSize(maxSize).build();
    }

    public static TextParticleProvider register(Text text, Color color, float maxSize) {
        return builder(text).color(color).maxSize(maxSize).build();
    }

    public static TextParticleProvider register(Text text, Color color, float maxSize, float yPos) {
        return builder(text).color(color).maxSize(maxSize).yPos(yPos).build();
    }

    public static TextParticleProvider register(Text text, Color color, float maxSize, float yPos, Supplier<Boolean> configSupplier) {
        return builder(text).color(color).maxSize(maxSize).yPos(yPos).config(configSupplier).build();
    }

    public int getId() {
        return id;
    }

    public static void handleParticle(Entity entity, int id) {
        if (id >= 0 && id < REGISTRY.length) {
            TextParticleProvider particle = REGISTRY[id];
            if (particle != null && particle.configSupplier.get()) {
                spawnTextParticle(entity, particle.text, particle.color, particle.maxSize, particle.yPos);
            }
        }
    }

    private static void spawnTextParticle(Entity target, Text text, Color color, float maxSize, float yPos) {
        if (target.getWorld().isClient()) {
            TextParticleClient.spawnParticle(target, text.getString(), color, maxSize, yPos);
        }
    }
    @Environment(EnvType.CLIENT)
    public static final class TextParticleClient {
        public static void spawnParticle(Entity target, String text, Color color, float maxSize, float yPos) {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientWorld world = client.world;
            if (world == null || !world.isClient()) return;

            Vec3d particlePos = target.getPos().add(0.0, target.getHeight() + 0.95 + yPos, 0.0);
            TextParticle particle = new TextParticle(world, particlePos.x, particlePos.y, particlePos.z);
            particle.setText(text);
            particle.setColor(color.getRed(), color.getGreen(), color.getBlue());
            particle.setMaxSize(maxSize);
            client.particleManager.addParticle(particle);
        }
    }
}