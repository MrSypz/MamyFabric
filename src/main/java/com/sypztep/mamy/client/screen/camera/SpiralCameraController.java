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
    private static final float RETURN_DURATION = 2.0f; // 2 seconds for return

    // Animation states
    private enum AnimationState {
        IDLE,
        SPIRALING_OUT,  // Moving away from player
        SPIRALING_IN    // Returning to player
    }

    private AnimationState currentState = AnimationState.IDLE;
    private float animationTime = 0.0f;
    private Vec3d playerStartPos;
    private float playerStartYaw;
    private float playerStartPitch;
    private Perspective originalPerspective;

    // Camera position data
    private Vec3d currentCameraPos;
    private float currentYaw;
    private float currentPitch;

    // Return animation data
    private Vec3d returnStartPos;
    private float returnStartYaw;
    private float returnStartPitch;

    private SpiralCameraController() {
        reset();
    }

    public static SpiralCameraController getInstance() {
        if (INSTANCE == null) INSTANCE = new SpiralCameraController();
        return INSTANCE;
    }

    /**
     * Start the spiral camera animation (moving away from player)
     */
    public void startSpiralAnimation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Store original perspective to restore later
        this.originalPerspective = client.options.getPerspective();

        this.currentState = AnimationState.SPIRALING_OUT;
        this.animationTime = 0.0f;
        this.playerStartPos = client.player.getEyePos();
        this.playerStartYaw = client.player.getYaw();
        this.playerStartPitch = client.player.getPitch();

        // Start at player eye position
        this.currentCameraPos = playerStartPos;
        this.currentYaw = playerStartYaw;
        this.currentPitch = playerStartPitch;
    }

    /**
     * Start spiral return animation (moving back to player)
     */
    public void startSpiralReturn() {
        if (currentState != AnimationState.SPIRALING_OUT) {
            // If not spiraling out, just stop immediately
            stopSpiralAnimation();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            stopSpiralAnimation();
            return;
        }

        // Store current position as return start
        this.returnStartPos = this.currentCameraPos;
        this.returnStartYaw = this.currentYaw;
        this.returnStartPitch = this.currentPitch;

        // Switch to return state
        this.currentState = AnimationState.SPIRALING_IN;
        this.animationTime = 0.0f;

        // Update player target position
        this.playerStartPos = client.player.getEyePos();
        this.playerStartYaw = client.player.getYaw();
        this.playerStartPitch = client.player.getPitch();
    }

    /**
     * Stop the animation immediately and return camera to normal
     */
    public void stopSpiralAnimation() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Restore original perspective
        if (originalPerspective != null) {
            client.options.setPerspective(originalPerspective);
        }

        this.currentState = AnimationState.IDLE;
        this.animationTime = 0.0f;
        this.originalPerspective = null;
        reset();
    }

    /**
     * Update the camera animation
     */
    public void update(float deltaTime) {
        if (currentState == AnimationState.IDLE) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            stopSpiralAnimation();
            return;
        }

        animationTime += deltaTime;

        switch (currentState) {
            case SPIRALING_OUT:
                updateSpiralOut();
                break;
            case SPIRALING_IN:
                updateSpiralIn();
                break;
        }
    }

    /**
     * Update spiral out animation (moving away from player)
     */
    private void updateSpiralOut() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if animation is complete
        if (animationTime >= ANIMATION_DURATION) {
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
        double worldTheta = currentTheta + playerYawRadians + Math.PI;

        // Convert polar to cartesian coordinates
        double offsetX = radius * Math.cos(worldTheta);
        double offsetZ = radius * Math.sin(worldTheta);

        Vec3d playerEyePos = client.player.getEyePos();
        this.currentCameraPos = new Vec3d(
                playerEyePos.x + offsetX,
                playerEyePos.y,
                playerEyePos.z + offsetZ
        );

        // Calculate camera rotation to look at player
        Vec3d lookDirection = playerEyePos.subtract(currentCameraPos).normalize();
        this.currentYaw = (float) Math.toDegrees(Math.atan2(-lookDirection.x, lookDirection.z));
        this.currentPitch = (float) Math.toDegrees(Math.asin(-lookDirection.y));
        this.currentPitch = Math.max(-90.0f, Math.min(90.0f, this.currentPitch));
    }

    /**
     * Update spiral return animation (moving back to player)
     */
    private void updateSpiralIn() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if return animation is complete
        if (animationTime >= RETURN_DURATION) {
            // Animation complete, stop everything
            stopSpiralAnimation();
            return;
        }

        // Calculate return progress with ease in
        float progress = Math.min(animationTime / RETURN_DURATION, 1.0f);
        float easedProgress = easeIn(progress);

        // Get current player position (for tracking)
        Vec3d currentPlayerPos = client.player.getEyePos();
        float currentPlayerYaw = client.player.getYaw();
        float currentPlayerPitch = client.player.getPitch();

        // Spiral back to player with tracking
        // Create a curved path from current position to player

        // Method 1: Direct interpolation with spiral motion

        // Add some spiral motion during return
        double returnTheta = (1.0f - easedProgress) * Math.PI * 2; // 2 full rotations during return
        double returnRadius = (1.0f - easedProgress) * 1.0; // Shrinking spiral

        double spiralX = returnRadius * Math.cos(returnTheta);
        double spiralZ = returnRadius * Math.sin(returnTheta);

        // Interpolate position with spiral offset
        this.currentCameraPos = returnStartPos.lerp(currentPlayerPos, easedProgress)
                .add(spiralX, 0, spiralZ);

        // Smooth rotation interpolation to player direction
        this.currentYaw = lerpAngle(returnStartYaw, currentPlayerYaw, easedProgress);
        this.currentPitch = lerp(returnStartPitch, currentPlayerPitch, easedProgress);

        // Clamp pitch
        this.currentPitch = Math.max(-90.0f, Math.min(90.0f, this.currentPitch));
    }

    // Helper method for angle interpolation (handles wrapping)
    private float lerpAngle(float start, float end, float progress) {
        float diff = end - start;

        // Handle angle wrapping
        if (diff > 180) diff -= 360;
        else if (diff < -180) diff += 360;

        return start + diff * progress;
    }

    // Helper method for linear interpolation
    private float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    // Easing functions
    private float easeOut(float t) {
        return 1.0f - (float) Math.pow(1.0 - t, 3.0);
    }

    private float easeIn(float t) {
        return t * t * t;
    }

    private void reset() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            this.currentCameraPos = client.player.getEyePos();
            this.currentYaw = client.player.getYaw();
            this.currentPitch = client.player.getPitch();
            client.gameRenderer.setRenderHand(true);
        }
    }

    // Getters for mixin
    public boolean isAnimating() {
        return currentState != AnimationState.IDLE;
    }

    public boolean isSpirallingOut() {
        return currentState == AnimationState.SPIRALING_OUT;
    }

    public boolean isSpirallingIn() {
        return currentState == AnimationState.SPIRALING_IN;
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
        if (currentState == AnimationState.IDLE) return 0.0f;

        float duration = (currentState == AnimationState.SPIRALING_OUT)
                ? ANIMATION_DURATION
                : RETURN_DURATION;

        return Math.min(animationTime / duration, 1.0f);
    }

    public String getAnimationState() {
        return switch (currentState) {
            case SPIRALING_OUT -> "OUT";
            case SPIRALING_IN -> "IN";
            default -> "IDLE";
        };
    }
}