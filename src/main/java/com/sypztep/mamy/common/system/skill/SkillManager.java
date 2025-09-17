package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.common.init.ModClassesSkill;
import com.sypztep.mamy.common.network.client.SkillCooldownPayloadS2C;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.ClassSkillManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SkillManager {
    private static final Map<String, Map<Identifier, Long>> PLAYER_COOLDOWNS = new HashMap<>();

    public static void useSkill(PlayerEntity player, Identifier skillId) {
        if (!(player instanceof ServerPlayerEntity)) return;

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        ClassSkillManager skillManager = classComponent.getClassManager().getSkillManager();
        int skillLevel = skillManager.getSkillLevel(skillId);

        // Use centralized validation
        SkillUsabilityChecker.UsabilityCheck usabilityCheck = SkillUsabilityChecker.checkServerUsability(player, skillId, skillLevel);

        if (!usabilityCheck.isUsable()) {
            SkillUsabilityChecker.sendUsabilityFeedback(player, usabilityCheck);
            return;
        }

        // Additional server-side cooldown check (since client might be out of sync)
        if (isOnCooldown(player, skillId)) return; // Don't send message, already handled by client

        Skill skill = ModClassesSkill.getSkill(skillId);
        if (skill == null) return; // Already validated, but keep for safety

        // Try to use resources
        float resourceCost = skill.getResourceCost(skillLevel);
        if (!classComponent.useResource(resourceCost)) return; // Resource usage failed (already validated, but server state might differ)

        // Execute the skill
        boolean skillExecuted = skill.use(player, skillLevel);

        if (skillExecuted) {
            // Apply cooldown
            float cooldown = skill.getCooldown(skillLevel);
            setCooldown(player, skillId, cooldown);
        } else classComponent.addResource(resourceCost);
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
            if (playerCooldowns.isEmpty()) PLAYER_COOLDOWNS.remove(playerId);
            return false;
        }
        return true;
    }

    private static void setCooldown(PlayerEntity player, Identifier skillId, float cooldownSeconds) {
        String playerId = player.getUuidAsString();
        long currentTime = System.currentTimeMillis();
        long cooldownMillis = Math.round(cooldownSeconds * 1000);
        long endTime = currentTime + cooldownMillis;

        PLAYER_COOLDOWNS.computeIfAbsent(playerId, k -> new HashMap<>()).put(skillId, endTime);

        if (player instanceof ServerPlayerEntity serverPlayer)
            SkillCooldownPayloadS2C.send(serverPlayer, skillId, endTime);
    }

    public static void cleanupPlayer(String playerId) {
        PLAYER_COOLDOWNS.remove(playerId);
    }
}