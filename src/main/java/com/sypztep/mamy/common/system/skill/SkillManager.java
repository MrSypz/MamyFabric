package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.component.living.PlayerStanceComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.ClassSkillManager;
import com.sypztep.mamy.common.system.classes.PlayerClassManager;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SkillManager {
    private static final Map<String, Map<Identifier, Long>> PLAYER_COOLDOWNS = new HashMap<>();

    public static void useSkill(PlayerEntity player, Identifier skillId) {
        if (!(player instanceof ServerPlayerEntity)) return;

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
        ClassSkillManager skillManager = classManager.getSkillManager();
        PlayerClass currentClass = classManager.getCurrentClass();

        if (!skill.isAvailableForClass(currentClass)) {
            player.sendMessage(Text.literal("Your class (" + currentClass.getDisplayName() + ") cannot use this skill!")
                    .formatted(Formatting.RED), true);
            return;
        }

        // Get the skill level
        int skillLevel = skillManager.getSkillLevel(skillId);
        if (skillLevel == 0) {
            player.sendMessage(Text.literal("You haven't learned this skill!")
                    .formatted(Formatting.RED), true);
            return;
        }

        if (isOnCooldown(player, skillId)) return;

        // Use level-based resource cost
        float resourceCost = skill.getResourceCost(skillLevel);
        float currentResource = classManager.getCurrentResource();

        if (currentResource < resourceCost) {
            player.sendMessage(Text.literal(String.format("Not enough %s! Need %.1f, have %.1f",
                            classManager.getResourceType().getDisplayName(), resourceCost, currentResource))
                    .formatted(Formatting.RED), true);
            return;
        }

        if (!skill.canUse(player, skillLevel)) {
            player.sendMessage(Text.literal("Cannot use skill right now!")
                    .formatted(Formatting.RED), true);
            return;
        }

        if (!classComponent.useResource(resourceCost)) {
            player.sendMessage(Text.literal("Failed to consume resource!")
                    .formatted(Formatting.RED), true);
            return;
        }

        // Use level-based cooldown
        float cooldown = skill.getCooldown(skillLevel);
        setCooldown(player, skillId, cooldown);

        // Execute skill with level
        skill.use(player, skillLevel);

        // Success message with skill level
        player.sendMessage(Text.literal("Used " + skill.getName() + " (Level " + skillLevel + ")")
                .formatted(Formatting.GREEN), true);
    }

    // Helper method to learn skills via commands or UI
    public static boolean learnSkill(PlayerEntity player, Identifier skillId) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager classManager = classComponent.getClassManager();
        ClassSkillManager skillManager = classManager.getSkillManager();

        return skillManager.learnSkill(skillId, classManager);
    }

    // Helper method to upgrade skills
    public static boolean upgradeSkill(PlayerEntity player, Identifier skillId) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager classManager = classComponent.getClassManager();
        ClassSkillManager skillManager = classManager.getSkillManager();

        return skillManager.upgradeSkill(skillId, classManager);
    }
    // Add this method to your SkillManager class:

    public static float getRemainingCooldownSeconds(PlayerEntity player, Identifier skillId) {
        String playerId = player.getUuidAsString();
        Map<Identifier, Long> playerCooldowns = PLAYER_COOLDOWNS.get(playerId);

        if (playerCooldowns == null) return 0.0f;

        Long endTime = playerCooldowns.get(skillId);
        if (endTime == null) return 0.0f;

        long currentTime = System.currentTimeMillis();
        if (currentTime >= endTime) {
            playerCooldowns.remove(skillId);
            return 0.0f;
        }

        return (endTime - currentTime) / 1000.0f;
    }

    // Also add this helper method to check if a skill is on cooldown:
    public static boolean isSkillOnCooldown(PlayerEntity player, Identifier skillId) {
        return getRemainingCooldownSeconds(player, skillId) > 0;
    }

    private static boolean isOnCooldown(PlayerEntity player, Identifier skillId) {
        String playerId = player.getUuidAsString();
        Map<Identifier, Long> playerCooldowns = PLAYER_COOLDOWNS.get(playerId);

        if (playerCooldowns == null) return false;

        Long endTime = playerCooldowns.get(skillId);
        if (endTime == null) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime >= endTime) {
            playerCooldowns.remove(skillId);
            return false;
        }

        long remainingMs = endTime - currentTime;
        player.sendMessage(Text.literal(String.format("Skill on cooldown for %.1f seconds",
                remainingMs / 1000.0f)).formatted(Formatting.YELLOW), true);
        return true;
    }

    private static void setCooldown(PlayerEntity player, Identifier skillId, float cooldownSeconds) {
        String playerId = player.getUuidAsString();
        PLAYER_COOLDOWNS.computeIfAbsent(playerId, k -> new HashMap<>())
                .put(skillId, System.currentTimeMillis() + (long)(cooldownSeconds * 1000));
    }

    public static void cleanupPlayer(String playerId) {
        PLAYER_COOLDOWNS.remove(playerId);
    }
}