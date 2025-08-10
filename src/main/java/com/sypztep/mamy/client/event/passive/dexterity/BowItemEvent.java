package com.sypztep.mamy.client.event.passive.dexterity;

import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;

public class BowItemEvent implements ClientTickEvents.EndTick {
    public static final BowItemEvent INSTANCE = new BowItemEvent();
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(INSTANCE);
    }
    private float shakeIntensity = 0.0f;
    private float shakeTimer = 0.0f;
    @Override
    public void onEndTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        // Check if player is holding a bow and using it
        ItemStack activeItem = player.getActiveItem();
        boolean isUsingBow = !activeItem.isEmpty() &&
                activeItem.getItem() instanceof BowItem &&
                player.isUsingItem();

        if (isUsingBow) {
            int useTime = player.getItemUseTime();
            shakeIntensity = calculateBaseIntensity(player, useTime);
            shakeTimer += 1.0f;

            // Apply camera shake
            applyCameraShake(player);
        } else {
            // Gradually reduce shake when not using bow
            shakeIntensity *= 0.8f;
            if (shakeIntensity < 0.01f) {
                shakeIntensity = 0.0f;
            }
        }
    }

    private float calculateBaseIntensity(ClientPlayerEntity player, int useTime) {
        float dexterity = ModEntityComponents.LIVINGLEVEL.get(player).getStatValue(StatTypes.DEXTERITY);

        // 0 DEX = 3.0x shake, 50 DEX = 1.5x shake, 100 DEX = 0.5x shake
        float dexMultiplier = Math.max(0.2f, 3.0f - (dexterity / 25.0f));

        boolean hasSteadyAim = PassiveAbilityManager.isActive(player, ModPassiveAbilities.STEADY_AIM);
        float steadyAimMultiplier = hasSteadyAim ? 0.25f : 1.0f; // 75% reduction

        // Movement penalty
        float movementPenalty = 1.0f;
        double velocity = player.getVelocity().horizontalLength();
        if (velocity > 0.15) {
            movementPenalty = 2.5f;
        } else if (velocity > 0.05) {
            movementPenalty = 1.8f;
        } else if (velocity > 0.01) {
            movementPenalty = 1.3f;
        }

        // Health penalty
        float healthPenalty = 1.0f;
        float healthPercent = player.getHealth() / player.getMaxHealth();
        if (healthPercent < 0.3f) {
            healthPenalty = 1.8f; // Injured = shaky
        } else if (healthPercent < 0.6f) {
            healthPenalty = 1.3f;
        }

        // Hunger penalty
        float hungerPenalty = 1.0f;
        int foodLevel = player.getHungerManager().getFoodLevel();
        if (foodLevel < 6) {
            hungerPenalty = 1.6f; // Starving = very shaky
        } else if (foodLevel < 12) {
            hungerPenalty = 1.2f; // Hungry = slightly shaky
        }

        // Time-based intensity (ramp up over time)
        float timeMultiplier;
        if (useTime < 30) {
            timeMultiplier = 0.5f + (useTime / 60.0f); // 0.5 to 1.0 over 1.5 seconds
        } else {
            // Gets worse the longer you hold
            float fatigueTime = useTime - 30.0f;
            timeMultiplier = 1.0f + (fatigueTime / 200.0f); // Slowly increases fatigue
        }

        // Base shake amount
        float baseShake = 1.2f;

        // Combine all factors
        float totalIntensity = baseShake * dexMultiplier * steadyAimMultiplier * movementPenalty * healthPenalty * hungerPenalty * timeMultiplier;

        return Math.max(0.1f, Math.min(8.0f, totalIntensity));
    }

    private void applyCameraShake(ClientPlayerEntity player) {
        if (shakeIntensity <= 0.0f) return;

        // Check for Steady Aim passive - reduces all shake components
        boolean hasSteadyAim = PassiveAbilityManager.isActive(player, ModPassiveAbilities.STEADY_AIM);
        float steadyAimReduction = hasSteadyAim ? 0.3f : 1.0f;

        // Base sine wave shake (your original smooth approach)
        float baseShakeX = (float) (Math.sin(shakeTimer * 0.5f) * shakeIntensity * 0.3f * steadyAimReduction);
        float baseShakeY = (float) (Math.cos(shakeTimer * 0.3f) * shakeIntensity * 0.2f * steadyAimReduction);

        // Breathing shake - reduced with higher DEX
        float dexterity = ModEntityComponents.LIVINGLEVEL.get(player).getStatValue(StatTypes.DEXTERITY);
        float breathingSpeed = Math.max(0.06f, 0.1f - (dexterity / 2000.0f)); // Slower breathing with higher DEX
        float breathingIntensity = Math.max(0.05f, 0.25f - (dexterity / 500.0f)); // Less breathing shake with higher DEX

        if (hasSteadyAim) breathingIntensity *= 0.2f; // Steady aim greatly reduces breathing

        float breathingShakeX = (float) (Math.sin(shakeTimer * breathingSpeed) * shakeIntensity * breathingIntensity);
        float breathingShakeY = (float) (Math.cos(shakeTimer * breathingSpeed * 0.8f) * shakeIntensity * breathingIntensity * 0.7f);

        // Heartbeat - subtle pulse, reduced with higher DEX
        float heartbeatIntensity = Math.max(0.02f, 0.08f - (dexterity / 2000.0f));
        if (hasSteadyAim) heartbeatIntensity *= 0.3f;

        float heartbeat = (float) Math.sin(shakeTimer * 0.25f) * shakeIntensity * heartbeatIntensity;

        // Micro tremors - high frequency, reduced significantly with DEX
        float tremorIntensity = Math.max(0.01f, 0.06f - (dexterity / 1000.0f));
        if (hasSteadyAim) tremorIntensity *= 0.2f;

        float microTremorX = (float) (Math.sin(shakeTimer * 2.1f) * shakeIntensity * tremorIntensity);
        float microTremorY = (float) (Math.cos(shakeTimer * 1.8f) * shakeIntensity * tremorIntensity);

        // Combine all shakes
        float shakeX = baseShakeX + breathingShakeX + heartbeat + microTremorX;
        float shakeY = baseShakeY + breathingShakeY + microTremorY;

        // Random component - greatly reduced with higher DEX and Steady Aim
        float randomIntensity = Math.max(0.01f, 0.1f - (dexterity / 1000.0f));
        if (hasSteadyAim) randomIntensity *= 0.2f;

        shakeX += (float) ((Math.random() - 0.5) * shakeIntensity * randomIntensity);
        shakeY += (float) ((Math.random() - 0.5) * shakeIntensity * randomIntensity);

        // Apply the shake to camera rotation
        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();

        player.setYaw(currentYaw + shakeX);
        player.setPitch(Math.max(-90.0f, Math.min(90.0f, currentPitch + shakeY)));
    }
}
