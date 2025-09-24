package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModClassesSkill;
import com.sypztep.mamy.common.system.classes.PlayerClassManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Centralized skill usability validation for both client and server
 * Reduces code redundancy and provides consistent validation logic
 */
public class SkillUsabilityChecker {

    public enum SkillUsabilityResult {
        USABLE, ON_COOLDOWN, INSUFFICIENT_RESOURCE, NOT_LEARNED, WRONG_CLASS,
        ALREADY_CASTING, IN_CAST_DELAY, UNKNOWN_SKILL, CUSTOM_CONDITION_FAILED
    }

    public static class UsabilityCheck {
        public final SkillUsabilityResult result;

        private UsabilityCheck(SkillUsabilityResult result) {
            this.result = result;
        }

        public static UsabilityCheck of(SkillUsabilityResult result) {
            return new UsabilityCheck(result);
        }

        public boolean isUsable() {
            return result == SkillUsabilityResult.USABLE;
        }

        public boolean shouldDimInUI() {
            return switch (result) {
                case ON_COOLDOWN, INSUFFICIENT_RESOURCE, NOT_LEARNED, IN_CAST_DELAY -> true;
                default -> false;
            };
        }
    }

    private static UsabilityCheck condition(PlayerEntity player, Skill skill, int skillLevel) {
        if (skill == null) return UsabilityCheck.of(SkillUsabilityResult.UNKNOWN_SKILL);

        PlayerClassManager classManager = ModEntityComponents.PLAYERCLASS.get(player).getClassManager();

        if (skillLevel == 0) return UsabilityCheck.of(SkillUsabilityResult.NOT_LEARNED);

        if (!skill.isAvailableForClass(classManager.getCurrentClass()))
            return UsabilityCheck.of(SkillUsabilityResult.WRONG_CLASS);

        float resourceCost = skill.getResourceCost(skillLevel);
        if (classManager.getCurrentResource() < resourceCost)
            return UsabilityCheck.of(SkillUsabilityResult.INSUFFICIENT_RESOURCE);

        return null; // means "passed base checks"
    }

    @Environment(EnvType.CLIENT)
    public static UsabilityCheck checkClientUsability(PlayerEntity player, Identifier skillId, int skillLevel) {
        Skill skill = ModClassesSkill.getSkill(skillId);
        UsabilityCheck base = condition(player, skill, skillLevel);
        if (base != null) return base;

        if (SkillCastDelayManager.getInstance().isInCastDelay())
            return UsabilityCheck.of(SkillUsabilityResult.IN_CAST_DELAY);

        if (ClientSkillCooldowns.getRemaining(skillId) > 0)
            return UsabilityCheck.of(SkillUsabilityResult.ON_COOLDOWN);

        SkillCastingManager castingManager = SkillCastingManager.getInstance();
        if (castingManager.isCasting()) {
            Identifier current = castingManager.getCurrentCastingSkill();
            if (current != null && !current.equals(skillId))
                return UsabilityCheck.of(SkillUsabilityResult.ALREADY_CASTING);
        }

        return UsabilityCheck.of(SkillUsabilityResult.USABLE);
    }

    public static UsabilityCheck checkServerUsability(PlayerEntity player, Identifier skillId, int skillLevel) {
        Skill skill = ModClassesSkill.getSkill(skillId);
        UsabilityCheck base = condition(player, skill, skillLevel);
        if (base != null) return base;

        if (!skill.canUse(player, skillLevel))
            return UsabilityCheck.of(SkillUsabilityResult.CUSTOM_CONDITION_FAILED);

        return UsabilityCheck.of(SkillUsabilityResult.USABLE);
    }

    @Environment(EnvType.CLIENT)
    public static boolean shouldDimSkillInUI(PlayerEntity player, Identifier skillId, int skillLevel) {
        return skillId == null || checkClientUsability(player, skillId, skillLevel).shouldDimInUI();
    }
}
