package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.component.living.PlayerStanceComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.PlayerClassManager;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;


public class SkillManager {
    // Track player skill cooldowns
    private static final Map<String, Map<String, Long>> PLAYER_COOLDOWNS = new HashMap<>();

    public static void useSkill(PlayerEntity player, String skillId) {
        if (!(player instanceof ServerPlayerEntity)) return;

        // Check if in combat stance
        PlayerStanceComponent stanceComponent = ModEntityComponents.PLAYERSTANCE.get(player);
        if (!stanceComponent.isInCombatStance()) {
            player.sendMessage(Text.literal("You must be in combat stance to use skills!")
                    .formatted(Formatting.RED), true);
            return;
        }

        Skill skill = SkillRegistry.getSkill(skillId);
        if (skill == null) {
            player.sendMessage(Text.literal("Unknown skill: " + skillId)
                    .formatted(Formatting.RED), true);
            return;
        }

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager classManager = classComponent.getClassManager();
        PlayerClass currentClass = classManager.getCurrentClass();

        // Check if player can use this skill
        if (!skill.isAvailableForClass(currentClass)) {
            player.sendMessage(Text.literal("Your class (" + currentClass.getDisplayName() + ") cannot use this skill!")
                    .formatted(Formatting.RED), true);
            return;
        }

        // Check cooldown
        if (isOnCooldown(player, skillId)) {
            long remainingCooldown = getRemainingCooldown(player, skillId);
            player.sendMessage(Text.literal("Skill on cooldown: " + (remainingCooldown / 20) + "s")
                    .formatted(Formatting.YELLOW), true);
            return;
        }

        // Check resource cost - FIXED
        float resourceCost = skill.getResourceCost();
        float currentResource = classManager.getCurrentResource();

        if (currentResource < resourceCost) {
            player.sendMessage(Text.literal(String.format("Not enough %s! Need %.1f, have %.1f",
                            classManager.getResourceType().getDisplayName(), resourceCost, currentResource))
                    .formatted(Formatting.RED), true);
            return;
        }

        // Check if skill can be used (additional conditions)
        if (!skill.canUse(player)) {
            player.sendMessage(Text.literal("Cannot use skill right now!")
                    .formatted(Formatting.RED), true);
            return;
        }

        // Consume resource - FIXED
        if (!classComponent.useResource(resourceCost)) {
            player.sendMessage(Text.literal("Failed to consume resource!")
                    .formatted(Formatting.RED), true);
            return;
        }

        // Use the skill
        try {
            skill.use(player, 1); // Level 1 for now

            // Set cooldown
            setCooldown(player, skillId, skill.getCooldown());

            // Success message
            player.sendMessage(Text.literal(String.format("Used: %s (-%s %.1f %s)",
                            skill.getName(),
                            classManager.getResourceType().getDisplayName(),
                            resourceCost,
                            classManager.getResourceType().getDisplayName()))
                    .formatted(Formatting.GREEN), true);

        } catch (Exception e) {
            // If skill fails, refund resource
//            classManager.addResource(resourceCost);
            player.sendMessage(Text.literal("Skill failed to execute!")
                    .formatted(Formatting.RED), true);
        }
    }

    private static boolean isOnCooldown(PlayerEntity player, String skillId) {
        Map<String, Long> playerCooldowns = PLAYER_COOLDOWNS.get(player.getUuidAsString());
        if (playerCooldowns == null) return false;

        Long cooldownEnd = playerCooldowns.get(skillId);
        if (cooldownEnd == null) return false;

        return player.getWorld().getTime() < cooldownEnd;
    }

    private static long getRemainingCooldown(PlayerEntity player, String skillId) {
        Map<String, Long> playerCooldowns = PLAYER_COOLDOWNS.get(player.getUuidAsString());
        if (playerCooldowns == null) return 0;

        Long cooldownEnd = playerCooldowns.get(skillId);
        if (cooldownEnd == null) return 0;

        return Math.max(0, cooldownEnd - player.getWorld().getTime());
    }

    private static void setCooldown(PlayerEntity player, String skillId, int cooldownTicks) {
        String playerId = player.getUuidAsString();
        PLAYER_COOLDOWNS.computeIfAbsent(playerId, k -> new HashMap<>());
        PLAYER_COOLDOWNS.get(playerId).put(skillId, player.getWorld().getTime() + cooldownTicks);
    }

    public static void cleanupPlayer(String playerId) {
        PLAYER_COOLDOWNS.remove(playerId);
    }

    // Get current resource info for debugging
    public static String getResourceInfo(PlayerEntity player) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager classManager = classComponent.getClassManager();

        return String.format("%s: %.1f/%.1f",
                classManager.getResourceType().getDisplayName(),
                classManager.getCurrentResource(),
                classManager.getMaxResource());
    }
}

