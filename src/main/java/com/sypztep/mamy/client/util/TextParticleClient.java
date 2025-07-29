package com.sypztep.mamy.client.util;

import com.sypztep.mamy.client.particle.TextParticle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

@Environment(EnvType.CLIENT)
public final class TextParticleClient {
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