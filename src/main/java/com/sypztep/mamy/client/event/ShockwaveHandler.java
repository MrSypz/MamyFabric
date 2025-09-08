package com.sypztep.mamy.client.event;

import com.sypztep.mamy.client.screen.CameraShakeManager;
import com.sypztep.mamy.common.init.ModSoundEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ShockwaveHandler {
    private static final double SOUND_SPEED = 123.0;
    private static final List<DelayedShake> pendingShakes = new ArrayList<>();

    public static class DelayedShake {
        public final double x, y, z;
        public final double time, radius, amplitude;
        public int ticksRemaining;

        public DelayedShake(double x, double y, double z, double time, double radius, double amplitude, int delay) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.time = time;
            this.radius = radius;
            this.amplitude = amplitude;
            this.ticksRemaining = delay;
        }
    }

    public static void handleShockwave(double x, double y, double z, double time, double radius, double amplitude) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        Vec3d playerPos = client.player.getPos();
        double distance = playerPos.distanceTo(new Vec3d(x, y, z));

        double travelTimeSeconds = distance / SOUND_SPEED;
        int delayTicks = (int) Math.round(travelTimeSeconds * 20);

        if (delayTicks <= 0) {
            CameraShakeManager.getInstance().startShake(time, radius, amplitude, x, y, z);
        } else {
            pendingShakes.add(new DelayedShake(x, y, z, time, radius, amplitude, delayTicks));
        }
    }

    public static void tick() {
        if (pendingShakes.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        for (int i = pendingShakes.size() - 1; i >= 0; i--) {
            DelayedShake shake = pendingShakes.get(i);
            shake.ticksRemaining--;

            if (shake.ticksRemaining <= 0) {
                CameraShakeManager.getInstance().startShake(shake.time, shake.radius, shake.amplitude, shake.x, shake.y, shake.z);
                float volume = (float) Math.clamp((shake.amplitude / 10.0), 0.1, 1);
                float pitch = 0.6f + (float) (Math.random() * 0.3f);
                client.world.playSound(client.player, client.player.getBlockPos(), ModSoundEvents.ENTITY_GENERIC_SHOCKWAVE, SoundCategory.AMBIENT, volume, pitch);

                pendingShakes.remove(i);
            }
        }
    }
}