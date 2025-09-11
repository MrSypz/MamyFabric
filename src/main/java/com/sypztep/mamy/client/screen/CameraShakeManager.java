package com.sypztep.mamy.client.screen;

import com.sypztep.mamy.client.event.ShockwaveHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public class CameraShakeManager {
    private static CameraShakeManager instance;

    // Shake parameters
    private double shakeTime = 0;
    private double shakeRadius = 0;
    private double shakeAmplitude = 0;
    private double[] shakePos = {0, 0, 0};
    private double shakeType = 0;

    public static CameraShakeManager getInstance() {
        if (instance == null) instance = new CameraShakeManager();
        return instance;
    }

    /**
     * Start a camera shake effect
     *
     * @param time      Duration of the shake
     * @param radius    Maximum distance from shake source where effect is felt
     * @param amplitude Intensity of the shake
     * @param x         X position of shake source
     * @param y         Y position of shake source
     * @param z         Z position of shake source
     */
    public void startShake(double time, double radius, double amplitude, double x, double y, double z) {
        this.shakeTime = time;
        this.shakeRadius = radius;
        this.shakeAmplitude = amplitude * MathHelper.RADIANS_PER_DEGREE; // Convert to radians
        this.shakePos[0] = x;
        this.shakePos[1] = y;
        this.shakePos[2] = z;
        this.shakeType = 2 * (Math.random() - 0.5); // Random direction multiplier
    }

    /**
     * Simplified version for local shakes (explosion at player position)
     */
    public void startShake(float intensity, float duration) {
        // Convert old parameters to new system
        double amplitude = intensity * 2.0; // Scale up the amplitude
        startShake(duration, 10.0, amplitude, 0, 0, 0);
    }

    public void tick(float deltaTime) {
        shakeTime = MathHelper.lerp(0.02f * deltaTime, shakeTime, 0);
    }

    public float getShakeOffsetX() {
        return getShakeOffset(true);
    }

    public float getShakeOffsetY() {
        return getShakeOffset(false);
    }

    /**
     * Calculate shake offset based on the reference implementation
     */
    private float getShakeOffset(boolean isYaw) {
        if (shakeTime <= 0) return 0;
        double shakeValue = shakeTime * Math.sin(0.5 * Math.PI * shakeTime) * shakeAmplitude * shakeType;

        if (isYaw) {
            return shakeType > 0 ? (float) shakeValue : (float) -shakeValue;
        } else {
            return shakeType > 0 ? (float) -shakeValue : (float) shakeValue;
        }
    }

    /**
     * Get shake offset with distance-based amplitude scaling
     */
    public float getShakeOffsetWithDistance(boolean isYaw, double playerX, double playerY, double playerZ) {
        if (shakeTime <= 0) return 0;

        // Calculate distance-based amplitude
        double distance = Math.sqrt(Math.pow(playerX - shakePos[0], 2) + Math.pow(playerY - shakePos[1], 2) + Math.pow(playerZ - shakePos[2], 2));

        double shakeRadiusAmplitude = MathHelper.clamp(1 - distance / shakeRadius, 0, 1);

        // Calculate shake using the reference formula
        double shakeValue = shakeTime * Math.sin(0.5 * Math.PI * shakeTime) * shakeAmplitude * shakeRadiusAmplitude * shakeType;

        // Apply direction based on whether this is yaw or pitch
        if (isYaw) {
            return shakeType > 0 ? (float) shakeValue : (float) -shakeValue;
        } else {
            return shakeType > 0 ? (float) -shakeValue : (float) shakeValue;
        }
    }

    public boolean isShaking() {
        return shakeTime > 0;
    }

    public double getShakeTime() {
        return shakeTime;
    }
    public static class Event {
        public static void register(MinecraftClient client) {
            if (client.player != null) {
                instance.tick(0.05f);
                ShockwaveHandler.tick();
            }
        }
    }
}