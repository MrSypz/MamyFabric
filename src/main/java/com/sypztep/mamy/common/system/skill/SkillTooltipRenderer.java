package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.ResourceType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Universal tooltip renderer for skills that provides consistent, structured, and extensible tooltip generation.
 * This utility class centralizes all tooltip formatting logic to ensure consistency across all skills.
 */
public final class SkillTooltipRenderer {

    /**
     * Renders a complete tooltip for a skill using the provided tooltip data and context.
     * 
     * @param skill The skill instance
     * @param data The skill tooltip data containing all information to render
     * @param player The player for context-specific information
     * @param skillLevel The current skill level
     * @param isLearned Whether the skill is learned
     * @param context The tooltip context (learning screen, binding, etc.)
     * @return A complete list of tooltip text components
     */
    public static List<Text> render(Skill skill, Skill.SkillTooltipData data, PlayerEntity player, 
                                  int skillLevel, boolean isLearned, Skill.TooltipContext context) {
        List<Text> tooltip = new ArrayList<>();

        // Header
        addSkillHeader(tooltip, skill, skillLevel, isLearned, data.isPassive);
        tooltip.add(Text.literal(""));

        // Main skill effects
        addSkillEffects(tooltip, data);
        
        // Status effects
        addStatusEffects(tooltip, data);
        
        // Special description
        addSpecialDescription(tooltip, data);
        
        tooltip.add(Text.literal(""));
        
        // Target and range information
        addTargetInfo(tooltip, data);
        
        // Resource and cooldown information
        addResourceInfo(tooltip, skill, player, skillLevel, data);
        
        // Context-specific information
        addContextInfo(tooltip, skill, player, skillLevel, isLearned, context, data);

        return tooltip;
    }

    private static void addSkillHeader(List<Text> tooltip, Skill skill, int skillLevel, boolean isLearned, boolean isPassive) {
        String skillTypeText = isPassive ? " (PASSIVE)" : "";
        Formatting skillTypeColor = isPassive ? Formatting.GREEN : Formatting.AQUA;
        
        if (isLearned) {
            tooltip.add(Text.literal(skill.getName()).formatted(Formatting.WHITE, Formatting.BOLD)
                    .append(Text.literal(skillTypeText).formatted(skillTypeColor))
                    .append(Text.literal(" (Level " + skillLevel + "/" + skill.getMaxSkillLevel() + ")")
                            .formatted(Formatting.GRAY)));
        } else {
            tooltip.add(Text.literal(skill.getName()).formatted(Formatting.GRAY, Formatting.BOLD)
                    .append(Text.literal(skillTypeText).formatted(isPassive ? Formatting.DARK_GREEN : Formatting.DARK_AQUA))
                    .append(Text.literal(" (Not Learned)").formatted(Formatting.DARK_GRAY)));
        }
    }

    private static void addSkillEffects(List<Text> tooltip, Skill.SkillTooltipData data) {
        // Damage effects
        if (data.baseDamage > 0 || data.damagePercentage > 0) {
            tooltip.add(buildDamageText(data.damageType, data.damagePercentage, data.baseDamage, data.maxHits));
        }

        // Secondary damage effects
        for (Skill.SecondaryDamage secondary : data.secondaryDamages) {
            tooltip.add(buildDamageText(secondary.damageType(), secondary.damagePercentage(), 
                                     secondary.baseDamage(), secondary.maxHits()));
        }

        // Recovery effects
        if (data.healthPerHit > 0) {
            tooltip.add(Text.literal("Recovery ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.1f", data.healthPerHit)).formatted(Formatting.YELLOW))
                    .append(Text.literal(" HP per hit").formatted(Formatting.GRAY)));
        }

        if (data.resourcePerHit > 0) {
            tooltip.add(Text.literal("Recovery ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.1f", data.resourcePerHit)).formatted(Formatting.YELLOW))
                    .append(Text.literal(" Resource per hit").formatted(Formatting.GRAY)));
        }

        // Additional effects
        for (String effect : data.additionalEffects) {
            tooltip.add(Text.literal("• " + effect).formatted(Formatting.GRAY));
        }
    }

    private static void addStatusEffects(List<Text> tooltip, Skill.SkillTooltipData data) {
        // Status effects removed
        if (!data.statusEffectsRemoved.isEmpty()) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Removes:").formatted(Formatting.GOLD));
            for (String effect : data.statusEffectsRemoved) {
                tooltip.add(Text.literal("• " + effect).formatted(Formatting.GREEN));
            }
        }

        // Status effects applied
        if (!data.statusEffectsApplied.isEmpty()) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Applies:").formatted(Formatting.GOLD));
            for (String effect : data.statusEffectsApplied) {
                tooltip.add(Text.literal("• " + effect).formatted(Formatting.YELLOW));
            }
        }
    }

    private static void addSpecialDescription(List<Text> tooltip, Skill.SkillTooltipData data) {
        if (!data.specialDescription.isEmpty()) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal(data.specialDescription).formatted(Formatting.GRAY));
        }
    }

    private static void addTargetInfo(List<Text> tooltip, Skill.SkillTooltipData data) {
        // Target type
        if (!data.targetType.isEmpty()) {
            tooltip.add(Text.literal("Target: ").formatted(Formatting.GRAY)
                    .append(Text.literal(data.targetType).formatted(Formatting.YELLOW)));
        }

        // Target range
        if (data.targetRange > 0) {
            tooltip.add(Text.literal("Range: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.0f blocks", data.targetRange)).formatted(Formatting.YELLOW)));
        }
    }

    private static void addResourceInfo(List<Text> tooltip, Skill skill, PlayerEntity player, int skillLevel, Skill.SkillTooltipData data) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        ResourceType resourceType = classComponent.getClassManager().getResourceType();

        // Resource cost
        if (data.overrideResourceCost) {
            tooltip.add(Text.literal("Require Resource: ").formatted(Formatting.GRAY)
                    .append(Text.literal(data.customResourceCostText).formatted(Formatting.YELLOW)));
        } else {
            float cost = skill.getResourceCost(skillLevel);
            tooltip.add(Text.literal("Require Resource: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.0f", cost)).formatted(Formatting.YELLOW))
                    .append(Text.literal(" " + resourceType.getDisplayName()).formatted(Formatting.GRAY)));
        }

        // Cooldown
        if (data.overrideCooldown) {
            tooltip.add(Text.literal("Cooldown: ").formatted(Formatting.GRAY)
                    .append(Text.literal(data.customCooldownText).formatted(Formatting.YELLOW)));
        } else {
            float cooldown = skill.getCooldown(skillLevel);
            String cooldownText = cooldown > 0 ? String.format("%.1f sec", cooldown) : "None";
            tooltip.add(Text.literal("Cooldown: ").formatted(Formatting.GRAY)
                    .append(Text.literal(cooldownText).formatted(Formatting.YELLOW)));
        }
    }

    private static void addContextInfo(List<Text> tooltip, Skill skill, PlayerEntity player, int skillLevel, 
                                     boolean isLearned, Skill.TooltipContext context, Skill.SkillTooltipData data) {
        switch (context) {
            case LEARNING_SCREEN -> addLearningInfo(tooltip, skill, player, skillLevel, isLearned, data);
            case BINDING_SCREEN -> addBindingInfo(tooltip, isLearned);
            case BINDING_SLOT -> addBindingSlotInfo(tooltip);
        }
    }

    private static void addLearningInfo(List<Text> tooltip, Skill skill, PlayerEntity player, int skillLevel, 
                                      boolean isLearned, Skill.SkillTooltipData data) {
        tooltip.add(Text.literal(""));

        if (isLearned) {
            PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
            int availablePoints = classComponent.getClassManager().getClassStatPoints();
            boolean canUpgrade = skillLevel < skill.getMaxSkillLevel() && availablePoints >= skill.getUpgradeClassPointCost();

            if (canUpgrade) {
                tooltip.add(Text.literal("Upgrade Cost: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.valueOf(skill.getUpgradeClassPointCost())).formatted(Formatting.YELLOW))
                        .append(Text.literal(" points").formatted(Formatting.GRAY)));
            }

            if (!skill.isDefaultSkill()) {
                tooltip.add(Text.literal("Right-click to unlearn").formatted(Formatting.GRAY));
            }
        } else {
            tooltip.add(Text.literal("Learn Cost: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.valueOf(skill.getBaseClassPointCost())).formatted(Formatting.YELLOW))
                    .append(Text.literal(" points").formatted(Formatting.GRAY)));
        }

        // Context-sensitive tip
        if (!data.contextTip.isEmpty()) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("💡 Tip: ").formatted(Formatting.YELLOW)
                    .append(Text.literal(data.contextTip).formatted(Formatting.GRAY)));
        }
    }

    private static void addBindingInfo(List<Text> tooltip, boolean isLearned) {
        if (isLearned) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Click to select for binding").formatted(Formatting.GRAY));
        }
    }

    private static void addBindingSlotInfo(List<Text> tooltip) {
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("Right-click to unbind").formatted(Formatting.GRAY));
    }

    private static Text buildDamageText(Skill.DamageTypeRef damageType, float damagePercentage, float baseDamage, int maxHits) {
        MutableText text = Text.literal(getDamageTypeText(damageType) + " ").formatted(Formatting.GRAY);

        if (damagePercentage > 0) {
            text.append(Text.literal(String.format("%.1f%%", damagePercentage * 100)).formatted(Formatting.YELLOW));
            if (baseDamage > 0) {
                text.append(Text.literal(" + ").formatted(Formatting.GRAY))
                    .append(Text.literal(String.format("%.1f", baseDamage)).formatted(Formatting.YELLOW));
            }
        } else if (baseDamage > 0) {
            text.append(Text.literal(String.format("%.1f", baseDamage)).formatted(Formatting.YELLOW));
        }

        if (maxHits > 1) {
            text.append(Text.literal(", " + maxHits + " hits").formatted(Formatting.YELLOW));
        }

        return text;
    }

    private static String getDamageTypeText(Skill.DamageTypeRef damageType) {
        return switch (damageType) {
            case MELEE -> "Melee";
            case MAGIC -> "Magic";
            case PHYSICAL -> "Physical";
            case HEAL -> "Heal";
            case ELEMENT -> "Element";
            default -> "Attack";
        };
    }
}