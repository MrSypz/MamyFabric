package com.sypztep.mamy.common.system.skill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.registry.entry.RegistryEntry;
import com.sypztep.mamy.common.util.AttributeModification;

public abstract class PassiveSkill extends Skill {
    protected final List<AttributeModification> attributeModifications;
    protected final List<PassiveEffect> passiveEffects;

    // Main constructor with prerequisites
    public PassiveSkill(Identifier id, String name, String description,
                        int maxSkillLevel,
                        boolean isDefaultSkill, Identifier icon, List<SkillRequirement> prerequisites) {
        super(id, name, description, 0.0f, 0.0f, 1,
                1, maxSkillLevel, isDefaultSkill, icon, prerequisites);
        this.attributeModifications = new ArrayList<>();
        this.passiveEffects = new ArrayList<>();
        initializePassiveEffects();
    }

    // Constructor without prerequisites (defaults to empty list)
    public PassiveSkill(Identifier id, String name, String description,
                        int maxSkillLevel,
                        boolean isDefaultSkill, Identifier icon) {
        this(id, name, description,
                maxSkillLevel, isDefaultSkill, icon, null);
    }

    // Constructor with icon but without isDefaultSkill (defaults to false)
    public PassiveSkill(Identifier id, String name, String description,
                        int maxSkillLevel,
                        Identifier icon) {
        this(id, name, description,
                maxSkillLevel, false, icon, null);
    }

    // Constructor with icon and prerequisites but without isDefaultSkill (defaults to false)
    public PassiveSkill(Identifier id, String name, String description,
                        int maxSkillLevel,
                        Identifier icon, List<SkillRequirement> prerequisites) {
        this(id, name, description,
                maxSkillLevel, false, icon, prerequisites);
    }

    /**
     * Initialize the passive effects this skill provides
     * Override this to define what the skill does when learned
     */
    protected abstract void initializePassiveEffects();

    /**
     * Add an attribute modification that will be applied when skill is learned
     */
    protected void addAttributeModification(AttributeModification modification) {
        attributeModifications.add(modification);
    }

    /**
     * Add a custom passive effect
     */
    protected void addPassiveEffect(PassiveEffect effect) {
        passiveEffects.add(effect);
    }

    /**
     * Apply all passive effects when skill is learned or upgraded
     */
    public void applyPassiveEffects(PlayerEntity player, int skillLevel) {
        // Apply attribute modifications
        for (AttributeModification modification : attributeModifications) {
            double value = modification.effectFunction().applyAsDouble((double) skillLevel);
            EntityAttributeModifier modifier = new EntityAttributeModifier(
                    modification.modifierId(),
                    value,
                    modification.operation()
            );

            if (player.getAttributeInstance(modification.attribute()) != null) {
                // Remove existing modifier if present
                Optional.ofNullable(player.getAttributeInstance(modification.attribute()))
                        .ifPresent(attr -> {
                            attr.removeModifier(modification.modifierId());
                            attr.addPersistentModifier(modifier);
                        });
            }
        }

        // Apply custom passive effects
        for (PassiveEffect effect : passiveEffects) {
            effect.apply(player, skillLevel);
        }
    }

    /**
     * Remove all passive effects when skill is unlearned
     */
    public void removePassiveEffects(PlayerEntity player) {
        for (AttributeModification modification : attributeModifications) {
            if (player.getAttributeInstance(modification.attribute()) != null) {
                Optional.ofNullable(player.getAttributeInstance(modification.attribute()))
                        .ifPresent(attr -> attr.removeModifier(modification.modifierId()));
            }
        }

        // Remove custom passive effects
        for (PassiveEffect effect : passiveEffects) {
            effect.remove(player);
        }
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        return new SkillTooltipData();
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        SkillTooltipData data = getSkillTooltipData(player, skillLevel);
        
        // Configure tooltip data for passive skills
        data.hideDamage = true;
        data.hideResourceCost = true;
        data.hideCooldown = true;
        
        // Add attribute modifications as effects
        for (AttributeModification modification : attributeModifications) {
            double value = modification.effectFunction().applyAsDouble((double) skillLevel);
            String attributeName = getAttributeDisplayName(modification.attribute());
            String valueText = formatAttributeValue(value, modification.operation());
            data.effects.add(attributeName + ": " + valueText);
        }

        List<Text> tooltip = SkillTooltipRenderer.render(player, skillLevel, isLearned, context, name, description, maxSkillLevel, data);

        // Add passive indicator to header
        if (!tooltip.isEmpty()) {
            Text originalHeader = tooltip.get(0);
            if (isLearned) {
                tooltip.set(0, Text.literal(name).formatted(Formatting.WHITE, Formatting.BOLD)
                        .append(Text.literal(" (PASSIVE)").formatted(Formatting.GREEN))
                        .append(Text.literal(" (Level " + skillLevel + "/" + maxSkillLevel + ")")
                                .formatted(Formatting.GRAY)));
            } else {
                tooltip.set(0, Text.literal(name).formatted(Formatting.GRAY, Formatting.BOLD)
                        .append(Text.literal(" (PASSIVE)").formatted(Formatting.DARK_GREEN))
                        .append(Text.literal(" (Not Learned)").formatted(Formatting.DARK_GRAY)));
            }
        }

        // Context-specific info (no resource cost/cooldown for passives)
        addContextInfo(tooltip, player, skillLevel, isLearned, context);

        return tooltip;
    }

    // Add a helper method to add context info since we need it
    private void addContextInfo(List<Text> tooltip, PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        switch (context) {
            case LEARNING_SCREEN -> addLearningInfo(tooltip, player, skillLevel, isLearned);
            case BINDING_SCREEN -> addBindingInfo(tooltip, isLearned);
            case BINDING_SLOT -> addBindingSlotInfo(tooltip);
        }
    }

    private void addLearningInfo(List<Text> tooltip, PlayerEntity player, int skillLevel, boolean isLearned) {
        tooltip.add(Text.literal(""));

        if (isLearned) {
            if (!isDefaultSkill) {
                tooltip.add(Text.literal("Right-click to unlearn").formatted(Formatting.GRAY));
            }
        } else {
            tooltip.add(Text.literal("Learn Cost: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.valueOf(baseClassPointCost)).formatted(Formatting.YELLOW))
                    .append(Text.literal(" points").formatted(Formatting.GRAY)));
        }
    }

    private void addBindingInfo(List<Text> tooltip, boolean isLearned) {
        // Passive skills cannot be bound, so we show this info
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("Passive skills cannot be bound").formatted(Formatting.GRAY));
    }

    private void addBindingSlotInfo(List<Text> tooltip) {
        // This shouldn't happen for passive skills, but just in case
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("Passive skills are always active").formatted(Formatting.GRAY));
    }

    private String getAttributeDisplayName(RegistryEntry<EntityAttribute> attribute) {
        String name = attribute.value().getTranslationKey();
        return name.substring(name.lastIndexOf('.') + 1).replace('_', ' ');
    }

    private String formatAttributeValue(double value, EntityAttributeModifier.Operation operation) {
        return switch (operation) {
            case ADD_VALUE -> String.format("+%.1f", value);
            case ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL -> String.format("+%.0f%%", value * 100);
        };
    }

    public interface PassiveEffect {
        void apply(PlayerEntity player, int skillLevel);
        void remove(PlayerEntity player);
    }
}