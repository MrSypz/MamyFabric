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
        PlayerClass currentClass = classManager.getCurrentClass();

        if (!skill.isAvailableForClass(currentClass)) {
            player.sendMessage(Text.literal("Your class (" + currentClass.getDisplayName() + ") cannot use this skill!")
                    .formatted(Formatting.RED), true);
            return;
        }

        if (isOnCooldown(player, skillId)) return;


        float resourceCost = skill.getResourceCost();
        float currentResource = classManager.getCurrentResource();

        if (currentResource < resourceCost) {
            player.sendMessage(Text.literal(String.format("Not enough %s! Need %.1f, have %.1f",
                            classManager.getResourceType().getDisplayName(), resourceCost, currentResource))
                    .formatted(Formatting.RED), true);
            return;
        }

        if (!skill.canUse(player)) {
            player.sendMessage(Text.literal("Cannot use skill right now!")
                    .formatted(Formatting.RED), true);
            return;
        }

        if (!classComponent.useResource(resourceCost)) {
            player.sendMessage(Text.literal("Failed to consume resource!")
                    .formatted(Formatting.RED), true);
            return;
        }

        try {
            skill.use(player, 1);
            setCooldown(player, skillId, skill.getCooldown());
        } catch (Exception e) {
            player.sendMessage(Text.literal("Skill failed to execute!")
                    .formatted(Formatting.RED), true);
        }
    }

    private static boolean isOnCooldown(PlayerEntity player, Identifier skillId) {
        Map<Identifier, Long> playerCooldowns = PLAYER_COOLDOWNS.get(player.getUuidAsString());
        if (playerCooldowns == null) return false;

        Long cooldownEnd = playerCooldowns.get(skillId);
        if (cooldownEnd == null) return false;

        return player.getWorld().getTime() < cooldownEnd;
    }

    private static long getRemainingCooldown(PlayerEntity player, Identifier skillId) {
        Map<Identifier, Long> playerCooldowns = PLAYER_COOLDOWNS.get(player.getUuidAsString());
        if (playerCooldowns == null) return 0;

        Long cooldownEnd = playerCooldowns.get(skillId);
        if (cooldownEnd == null) return 0;

        return Math.max(0, cooldownEnd - player.getWorld().getTime());
    }

    public static float getRemainingCooldownSeconds(PlayerEntity player, Identifier skillId) {
        return getRemainingCooldown(player, skillId) / 20.0f;
    }

    private static void setCooldown(PlayerEntity player, Identifier skillId, float cooldownSeconds) {
        String playerId = player.getUuidAsString();
        PLAYER_COOLDOWNS.computeIfAbsent(playerId, k -> new HashMap<>());
        long cooldownTicks = (long)(cooldownSeconds * 20);
        PLAYER_COOLDOWNS.get(playerId).put(skillId, player.getWorld().getTime() + cooldownTicks);
    }

    public static void cleanupPlayer(String playerId) {
        PLAYER_COOLDOWNS.remove(playerId);
    }
}