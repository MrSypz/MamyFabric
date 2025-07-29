package com.sypztep.mamy.client.screen.camera;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.Vec3d;

public final class SpiralCameraController {
    private static SpiralCameraController INSTANCE;

    private static final double SPIRAL_A = 0.0; // Center at player position
    private static final double SPIRAL_B = 0.4; // Distance scaling factor
    private static final double MAX_THETA = 4.7123889;

    private static final float ANIMATION_DURATION = 3.0f; // 3 seconds

    private boolean isAnimating = false;
    private float animationTime = 0.0f;
    private Vec3d playerStartPos;
    private float playerStartYaw;
    private float playerStartPitch;
    private Perspective originalPerspective; // Store original perspective

    // Camera position data
    private Vec3d currentCameraPos;
    private float currentYaw;
    private float currentPitch;

    private SpiralCameraController() {
        reset();
    }

    public static SpiralCameraController getInstance() {
        if (INSTANCE == null) INSTANCE = new SpiralCameraController();

        return INSTANCE;
    }

    /**
     * Start the spiral camera animation
     */
    public void startSpiralAnimation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Store original perspective to restore later
        this.originalPerspective = client.options.getPerspective();

        this.isAnimating = true;
        this.animationTime = 0.0f;
        this.playerStartPos = client.player.getEyePos(); // Start at eye position
        this.playerStartYaw = client.player.getYaw(); // Lock in the yaw direction for spiral
        this.playerStartPitch = client.player.getPitch();

        // Start at player eye position
        this.currentCameraPos = playerStartPos;
        this.currentYaw = playerStartYaw;
        this.currentPitch = playerStartPitch;
    }

    /**
     * Stop the animation and return camera to normal
     */
    public void stopSpiralAnimation() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Restore original perspective
        if (originalPerspective != null) {
            client.options.setPerspective(originalPerspective);
        }

        this.isAnimating = false;
        this.animationTime = 0.0f;
        this.originalPerspective = null;
        reset();
    }

    /**
     * Update the camera animation
     */
    public void update(float deltaTime) {
        if (!isAnimating) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            stopSpiralAnimation();
            return;
        }

        animationTime += deltaTime;

        // Check if animation is complete
        if (animationTime >= ANIMATION_DURATION) {
            // Keep camera at final position
            animationTime = ANIMATION_DURATION;
        }

        // Calculate animation progress with ease out
        float progress = Math.min(animationTime / ANIMATION_DURATION, 1.0f);
        float easedProgress = easeOut(progress);

        // Calculate current theta based on eased progress
        double currentTheta = easedProgress * MAX_THETA;

        // Calculate spiral radius: r = a + b*θ
        double radius = SPIRAL_A + SPIRAL_B * currentTheta;

        // Get player's yaw at animation start (locked direction)
        double playerYawRadians = Math.toRadians(this.playerStartYaw);

        // Calculate spiral position relative to player's facing direction at animation start
        // θ=0 starts from behind player, θ=π ends in front of player
        double worldTheta = currentTheta + playerYawRadians + Math.PI; // +π to start from behind

        // Convert polar to cartesian coordinates (rotated by player yaw)
        double offsetX = radius * Math.cos(worldTheta);
        double offsetZ = radius * Math.sin(worldTheta);

        Vec3d playerEyePos = client.player.getEyePos(); // Use current player position
        this.currentCameraPos = new Vec3d(
                playerEyePos.x + offsetX,
                playerEyePos.y,
                playerEyePos.z + offsetZ
        );

        // Calculate camera rotation to look at player
        Vec3d lookDirection = playerEyePos.subtract(currentCameraPos).normalize();

        // Convert direction to yaw/pitch
        this.currentYaw = (float) Math.toDegrees(Math.atan2(-lookDirection.x, lookDirection.z));
        this.currentPitch = (float) Math.toDegrees(Math.asin(-lookDirection.y));

        // Clamp pitch to reasonable values
        this.currentPitch = Math.max(-90.0f, Math.min(90.0f, this.currentPitch));
    }

    private float easeOut(float t) {
        return 1.0f - (float) Math.pow(1.0 - t, 3.0);
    }

    private void reset() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            this.currentCameraPos = client.player.getEyePos(); // Use eye position
            this.currentYaw = client.player.getYaw();
            this.currentPitch = client.player.getPitch();
            client.gameRenderer.setRenderHand(true);
        }
    }

    // Getters for mixin
    public boolean isAnimating() {
        return isAnimating;
    }

    public Vec3d getCurrentCameraPos() {
        return currentCameraPos != null ? currentCameraPos : Vec3d.ZERO;
    }

    public float getCurrentYaw() {
        return currentYaw;
    }

    public float getCurrentPitch() {
        return currentPitch;
    }

    public float getAnimationProgress() {
        if (!isAnimating) return 0.0f;
        return Math.min(animationTime / ANIMATION_DURATION, 1.0f);
    }
}