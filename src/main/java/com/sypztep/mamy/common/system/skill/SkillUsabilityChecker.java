package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModClassesSkill;
import com.sypztep.mamy.common.system.classes.PlayerClassManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * Centralized skill usability validation for both client and server
 * Reduces code redundancy and provides consistent validation logic
 */
public class SkillUsabilityChecker {

    public enum SkillUsabilityResult {
        USABLE, ON_COOLDOWN, INSUFFICIENT_RESOURCE, NOT_LEARNED, WRONG_CLASS, ALREADY_CASTING, UNKNOWN_SKILL, CUSTOM_CONDITION_FAILED
    }

    public static class UsabilityCheck {
        public final SkillUsabilityResult result;
        public final String message;
        public final boolean canShowInUI;

        private UsabilityCheck(SkillUsabilityResult result, String message, boolean canShowInUI) {
            this.result = result;
            this.message = message;
            this.canShowInUI = canShowInUI;
        }

        public boolean isUsable() {
            return result == SkillUsabilityResult.USABLE;
        }

        public boolean shouldDimInUI() {
            return result == SkillUsabilityResult.ON_COOLDOWN || result == SkillUsabilityResult.INSUFFICIENT_RESOURCE || result == SkillUsabilityResult.NOT_LEARNED;
        }
    }

    /**
     * Client-side usability check (for UI and casting validation)
     */
    @Environment(EnvType.CLIENT)
    public static UsabilityCheck checkClientUsability(PlayerEntity player, Identifier skillId, int skillLevel) {
        // Check if skill exists
        Skill skill = ModClassesSkill.getSkill(skillId);
        if (skill == null) {
            return new UsabilityCheck(SkillUsabilityResult.UNKNOWN_SKILL, "Unknown skill: " + skillId, false);
        }

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager classManager = classComponent.getClassManager();

        // Check if skill is learned
        if (skillLevel == 0) {
            return new UsabilityCheck(SkillUsabilityResult.NOT_LEARNED, "You haven't learned this skill!", true);
        }

        // Check class compatibility
        if (!skill.isAvailableForClass(classManager.getCurrentClass())) {
            return new UsabilityCheck(SkillUsabilityResult.WRONG_CLASS, "Your class cannot use this skill!", false);
        }

        // Check cooldown
        float remainingCooldown = ClientSkillCooldowns.getRemaining(skillId);
        if (remainingCooldown > 0) {
            return new UsabilityCheck(SkillUsabilityResult.ON_COOLDOWN, String.format("On cooldown (%.1fs)", remainingCooldown), true);
        }

        // Check if already casting a DIFFERENT skill
        SkillCastingManager castingManager = SkillCastingManager.getInstance();
        if (castingManager.isCasting()) {
            Identifier currentCastingSkill = castingManager.getCurrentCastingSkill();
            if (currentCastingSkill != null && !currentCastingSkill.equals(skillId)) {
                return new UsabilityCheck(SkillUsabilityResult.ALREADY_CASTING, "Already casting another skill", false);
            }
            // If casting the same skill, allow it (for cancellation)
        }

        // Check resource cost
        float resourceCost = skill.getResourceCost(skillLevel);
        float currentResource = classManager.getCurrentResource();
        if (currentResource < resourceCost) {
            return new UsabilityCheck(SkillUsabilityResult.INSUFFICIENT_RESOURCE, String.format("Not enough %s (%.1f/%.1f)", classManager.getResourceType().getDisplayName(), currentResource, resourceCost), true);
        }

        return new UsabilityCheck(SkillUsabilityResult.USABLE, "", true);
    }

    /**
     * Server-side usability check (for actual skill usage)
     */
    public static UsabilityCheck checkServerUsability(PlayerEntity player, Identifier skillId, int skillLevel) {
        // Check if skill exists
        Skill skill = ModClassesSkill.getSkill(skillId);
        if (skill == null) {
            return new UsabilityCheck(SkillUsabilityResult.UNKNOWN_SKILL, "Unknown skill: " + skillId, false);
        }

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClassManager classManager = classComponent.getClassManager();

        // Check if skill is learned
        if (skillLevel == 0) {
            return new UsabilityCheck(SkillUsabilityResult.NOT_LEARNED, "You haven't learned this skill!", false);
        }

        // Check class compatibility
        if (!skill.isAvailableForClass(classManager.getCurrentClass())) {
            return new UsabilityCheck(SkillUsabilityResult.WRONG_CLASS, "Your class (" + classManager.getCurrentClass().getDisplayName() + ") cannot use this skill!", false);
        }

        // Check resource cost
        float resourceCost = skill.getResourceCost(skillLevel);
        float currentResource = classManager.getCurrentResource();
        if (currentResource < resourceCost) {
            return new UsabilityCheck(SkillUsabilityResult.INSUFFICIENT_RESOURCE, "Not enough " + classManager.getResourceType().getDisplayName(), false);
        }

        // Check custom skill conditions
        if (!skill.canUse(player, skillLevel)) {
            return new UsabilityCheck(SkillUsabilityResult.CUSTOM_CONDITION_FAILED, skill.condition().getString(), false);
        }

        return new UsabilityCheck(SkillUsabilityResult.USABLE, "", false);
    }

    /**
     * Quick client-side check for UI dimming
     */
    @Environment(EnvType.CLIENT)
    public static boolean shouldDimSkillInUI(PlayerEntity player, Identifier skillId, int skillLevel) {
        if (skillId == null) return true;

        UsabilityCheck check = checkClientUsability(player, skillId, skillLevel);
        return check.shouldDimInUI();
    }

    /**
     * Send user feedback for failed skill usage attempts
     */
    public static void sendUsabilityFeedback(PlayerEntity player, UsabilityCheck check) {
        if (!check.message.isEmpty() && player != null) {
            Formatting color = switch (check.result) {
                case ON_COOLDOWN -> Formatting.BLUE;
                case INSUFFICIENT_RESOURCE -> Formatting.GOLD;
                case NOT_LEARNED, WRONG_CLASS -> Formatting.RED;
                default -> Formatting.GRAY;
            };

            player.sendMessage(Text.literal(check.message).formatted(color), true);
        }
    }
}