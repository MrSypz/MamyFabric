package com.sypztep.mamy.client.event.passive.dexterity;

import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;

import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import net.minecraft.util.math.MathHelper;

public class BowItemEvent implements ClientTickEvents.EndTick, HudRenderCallback {
    public static final BowItemEvent INSTANCE = new BowItemEvent();

    // Breath cooldown constants
    private static final float BREATH_COOLDOWN_TIME = 60.0f; // 3 seconds at 20 TPS

    // Core state management
    private float swayTimer = 0.0f;
    private float breathStamina = 100.0f;
    private boolean isHoldingBreath = false;
    private boolean isBreathOnCooldown = false;
    private float breathCooldownTimer = 0.0f;
    private float armStrength = 100.0f;
    private AimStance currentStance = AimStance.STANDING;

    // Sway calculation components
    private float naturalSway = 0.0f;
    private float fatigueSway = 0.0f;
    private float stressSway = 0.0f;

    // Feedback systems
    private float steadyWindow = 0.0f;
    private boolean wasInSteadyWindow = false;
    private boolean isUsingBow = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(INSTANCE);
        HudRenderCallback.EVENT.register(INSTANCE);
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        ItemStack activeItem = player.getActiveItem();
        isUsingBow = !activeItem.isEmpty() &&
                activeItem.getItem() instanceof BowItem &&
                player.isUsingItem();

        if (isUsingBow) {
            updatePlayerStance(player);
            updateStaminaSystems(player);
            updateSwayComponents(player);
            applyAimingEffects(player);
            checkSteadyWindow(player);
        } else {
            resetAimingState();
        }
    }
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !isUsingBow) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Position bars
        int barWidth = 120;
        int barHeight = 8;
        int barX = screenWidth - barWidth - 20;
        int breathBarY = screenHeight / 2 - 50;
        int armBarY = screenHeight / 2 - 35;
        int bowBarY = screenHeight / 2 - 20; // New bow draw bar

        // Render breath stamina bar
        renderStaminaBar(drawContext, barX, breathBarY, barWidth, barHeight,
                breathStamina, 100.0f, "Breath",
                isBreathOnCooldown ? 0xFF4444 : 0x4488FF,
                isHoldingBreath);

        // Render arm strength bar
        renderStaminaBar(drawContext, barX, armBarY, barWidth, barHeight,
                armStrength, 100.0f, "Arm Strength",
                0x44FF44, false);

        // Render bow draw progress bar
        int useTime = client.player.getItemUseTime();
        float bowPower = BowItem.getPullProgress(useTime);
        float minPowerForSteady = 0.8f; // Show when steady is possible

        renderStaminaBar(drawContext, barX, bowBarY, barWidth, barHeight,
                bowPower * 100.0f, 100.0f, "Draw Power",
                bowPower >= minPowerForSteady ? 0xFFAA00 : 0x888888, // Orange when ready for steady
                false);

        // Render steady window indicator - only when conditions are met
        if (steadyWindow > 0.5f) {
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;
            int indicatorSize = 4;

            // Pulsing green dot when in steady window
            int alpha = (int) (255 * Math.sin(swayTimer * 8.0f) * 0.3f + 127);
            int color = (alpha << 24) | 0x00FF00;

            drawContext.fill(centerX - indicatorSize, centerY - indicatorSize,
                    centerX + indicatorSize, centerY + indicatorSize, color);
        }
    }
    private void renderStaminaBar(DrawContext drawContext, int x, int y, int width, int height,
                                  float current, float max, String label, int color, boolean isActive) {
        // Background
        drawContext.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0x88000000);
        drawContext.fill(x, y, x + width, y + height, 0x44000000);

        // Stamina fill
        float percentage = current / max;
        int fillWidth = (int) (width * percentage);

        // Color intensity based on percentage
        int alpha = isActive ? 0xFF : 0xCC;
        int finalColor = (alpha << 24) | (color & 0xFFFFFF);

        if (fillWidth > 0) {
            drawContext.fill(x, y, x + fillWidth, y + height, finalColor);
        }

        // Text label and percentage
        String text = label + ": " + (int) current + "%";
        int textColor = isActive ? 0xFFFFFF : 0xCCCCCC;

        drawContext.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                text, x, y - 12, textColor);

        // Cooldown indicator
        if (label.equals("Breath") && isBreathOnCooldown) {
            String cooldownText = "RECOVERING...";
            drawContext.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    cooldownText, x, y + height + 2, 0xFF4444);
        }
    }

    private void updatePlayerStance(ClientPlayerEntity player) {
        boolean isCrouching = player.isSneaking();
        boolean isMoving = player.getVelocity().horizontalLength() > 0.01;

        if (isCrouching && !isMoving) {
            currentStance = AimStance.CROUCHED;
        } else if (!isMoving) {
            currentStance = AimStance.STANDING;
        } else {
            currentStance = AimStance.MOVING;
        }
    }


    private void updateStaminaSystems(ClientPlayerEntity player) {
        int useTime = player.getItemUseTime();
        float dexterity = ModEntityComponents.LIVINGLEVEL.get(player).getStatValue(StatTypes.DEXTERITY);

        // Breath control system - DEX affects breath efficiency
        boolean breathKeyPressed = player.isSneaking();
        float breathEfficiency = Math.max(0.5f, 1.0f + (dexterity / 100.0f)); // Higher DEX = better breath control

        // Breath control logic with cooldown system
        if (breathKeyPressed && breathStamina > 10.0f && !isHoldingBreath && !isBreathOnCooldown) {
            isHoldingBreath = true;
        } else if (!breathKeyPressed || breathStamina <= 0.0f) {
            if (isHoldingBreath && breathStamina <= 0.0f) {
                // Player exhausted their breath - enter cooldown
                isBreathOnCooldown = true;
                breathCooldownTimer = BREATH_COOLDOWN_TIME;
            }
            isHoldingBreath = false;
        }

        // Handle breath recovery and cooldown
        if (isHoldingBreath) {
            float breathDrain = 2.5f / breathEfficiency; // Higher DEX drains breath slower
            breathStamina -= breathDrain;
        } else {
            if (isBreathOnCooldown) {
                // During cooldown, recover faster but can't use breath hold
                breathStamina = Math.min(100.0f, breathStamina + (breathEfficiency * 2.0f));
                breathCooldownTimer -= 1.0f;

                // Exit cooldown when fully recovered
                if (breathStamina >= 100.0f && breathCooldownTimer <= 0.0f) {
                    isBreathOnCooldown = false;
                }
            } else {
                // Normal recovery
                breathStamina = Math.min(100.0f, breathStamina + breathEfficiency);
            }
        }

        // Arm strength system - DEX dramatically affects fatigue rate
        float baseFatigueRate = 2.0f;
        float dexterityMultiplier = Math.max(0.2f, baseFatigueRate - (dexterity / 25.0f)); // Similar to your original scaling
        float armStrainRate = dexterityMultiplier * currentStance.getStabilityMultiplier();

        armStrength = Math.max(0.0f, armStrength - armStrainRate);
        if (useTime == 0) {
            float recovery = 3.0f + (dexterity / 50.0f); // Higher DEX recovers arm strength faster
            armStrength = Math.min(100.0f, armStrength + recovery);
        }
    }

    private void updateSwayComponents(ClientPlayerEntity player) {
        swayTimer += 0.05f;
        float dexterity = ModEntityComponents.LIVINGLEVEL.get(player).getStatValue(StatTypes.DEXTERITY);

        // Natural breathing sway - heavily influenced by DEX (like your original)
        float breathCycle = (float) Math.sin(swayTimer * 0.8f);
        float breathIntensity = isHoldingBreath ? 0.1f : 1.0f;

        // DEX scaling similar to your original: 0 DEX = 3.0x sway, 50 DEX = 1.5x, 100 DEX = 0.5x
        float dexSwayReduction = Math.max(0.2f, 3.0f - (dexterity / 25.0f));
        naturalSway = breathCycle * breathIntensity * dexSwayReduction * 0.4f;

        // Fatigue sway - also affected by DEX
        float fatigueIntensity = (100.0f - armStrength) / 100.0f;
        float heartbeatRate = 1.5f + fatigueIntensity * 2.0f;
        float dexFatigueReduction = Math.max(0.3f, 2.0f - (dexterity / 50.0f));
        fatigueSway = (float) Math.sin(swayTimer * heartbeatRate) * fatigueIntensity * dexFatigueReduction * 0.8f;

        // Stress sway (health, hunger, movement) - DEX provides some resistance
        float healthPercent = player.getHealth() / player.getMaxHealth();
        float hungerPercent = player.getHungerManager().getFoodLevel() / 20.0f;
        float stressLevel = (1.0f - healthPercent) + (1.0f - hungerPercent);

        // Movement penalty similar to your original
        double velocity = player.getVelocity().horizontalLength();
        float movementPenalty = 1.0f;
        if (velocity > 0.15) {
            movementPenalty = 2.5f;
        } else if (velocity > 0.05) {
            movementPenalty = 1.8f;
        } else if (velocity > 0.01) {
            movementPenalty = 1.3f;
        }

        float dexStressReduction = Math.max(0.4f, 1.5f - (dexterity / 80.0f));
        stressSway = (float) (Math.random() - 0.5) * stressLevel * movementPenalty * dexStressReduction * 0.6f;
    }

    private void applyAimingEffects(ClientPlayerEntity player) {
        float dexterity = ModEntityComponents.LIVINGLEVEL.get(player).getStatValue(StatTypes.DEXTERITY);

        // Combine all sway components
        float totalSwayX = (naturalSway + fatigueSway + stressSway) * currentStance.getSwayMultiplier();
        float totalSwayY = (naturalSway * 0.7f + fatigueSway * 0.5f + stressSway * 0.8f) * currentStance.getSwayMultiplier();

        // Apply steady aim passive if available
        boolean hasSteadyAim = PassiveAbilityManager.isActive(player, ModPassiveAbilities.STEADY_AIM);
        if (hasSteadyAim) {
            totalSwayX *= 0.25f; // Same as your original 75% reduction
            totalSwayY *= 0.25f;
        }

        // Breath hold dramatically reduces sway - effectiveness scales with DEX
        if (isHoldingBreath) {
            float breathSteadiness = Math.max(0.1f, 0.3f - (dexterity / 500.0f)); // Higher DEX = steadier breath hold
            totalSwayX *= breathSteadiness;
            totalSwayY *= breathSteadiness;
        }

        // Apply camera movement with smooth interpolation
        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();

        // Clamp sway - max sway also affected by DEX
        float maxSway = Math.max(0.5f, 3.0f - (dexterity / 35.0f));
        totalSwayX = MathHelper.clamp(totalSwayX, -maxSway, maxSway);
        totalSwayY = MathHelper.clamp(totalSwayY, -maxSway * 0.75f, maxSway * 0.75f);

        player.setYaw(currentYaw + totalSwayX);
        player.setPitch(MathHelper.clamp(currentPitch + totalSwayY, -90.0f, 90.0f));
    }

    private void checkSteadyWindow(ClientPlayerEntity player) {
        float dexterity = ModEntityComponents.LIVINGLEVEL.get(player).getStatValue(StatTypes.DEXTERITY);
        int useTime = player.getItemUseTime();

        // Calculate bow draw power (same as Minecraft's bow mechanics)
        float bowPower = BowItem.getPullProgress(useTime);
        float minPowerForSteady = 0.8f - (dexterity / 500.0f); // Higher DEX allows steady at lower draw
        minPowerForSteady = Math.max(0.5f, minPowerForSteady); // Never less than 50% draw

        boolean bowDrawnEnough = bowPower >= minPowerForSteady;

        // Calculate if player is in a "perfect shot" window
        float totalSway = Math.abs(naturalSway) + Math.abs(fatigueSway) + Math.abs(stressSway);
        float steadyThreshold = Math.max(0.1f, 0.5f - (dexterity / 200.0f));

        // All conditions must be met for steady window
        boolean inSteadyWindow = bowDrawnEnough && // Must be drawn enough
                totalSway < steadyThreshold &&
                isHoldingBreath &&
                armStrength > 20.0f;

        if (inSteadyWindow && !wasInSteadyWindow) {
            steadyWindow = 1.0f;
        } else if (inSteadyWindow) {
            steadyWindow = Math.min(2.0f, steadyWindow + 0.1f);
        } else {
            steadyWindow = Math.max(0.0f, steadyWindow - 0.2f);
        }

        wasInSteadyWindow = inSteadyWindow;
    }


    private void resetAimingState() {
        naturalSway = 0.0f;
        fatigueSway = 0.0f;
        stressSway = 0.0f;
        steadyWindow = 0.0f;
        swayTimer = 0.0f;

        // Don't reset breath cooldown when stopping bow use
        // Let it continue recovering naturally

        // Gradual recovery when not aiming
        if (!isBreathOnCooldown) {
            breathStamina = Math.min(100.0f, breathStamina + 2.0f);
        }
        armStrength = Math.min(100.0f, armStrength + 1.5f);
        isHoldingBreath = false;
        currentStance = AimStance.STANDING;
    }

    public enum AimStance {
        CROUCHED(0.4f, 0.6f),
        STANDING(1.0f, 1.0f),
        MOVING(2.5f, 1.8f);

        private final float swayMultiplier;
        private final float stabilityMultiplier;

        AimStance(float swayMultiplier, float stabilityMultiplier) {
            this.swayMultiplier = swayMultiplier;
            this.stabilityMultiplier = stabilityMultiplier;
        }

        public float getSwayMultiplier() { return swayMultiplier; }
        public float getStabilityMultiplier() { return stabilityMultiplier; }
    }
}