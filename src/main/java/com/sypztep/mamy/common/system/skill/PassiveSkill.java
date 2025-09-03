package com.sypztep.mamy.common.system.skill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import com.sypztep.mamy.common.system.classes.PlayerClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import net.minecraft.registry.entry.RegistryEntry;
import com.sypztep.mamy.common.util.AttributeModification;

public abstract class PassiveSkill extends Skill {
    protected final List<AttributeModification> attributeModifications;
    protected final List<PassiveEffect> passiveEffects;

    // Constructor with prerequisites
    public PassiveSkill(Identifier id, String name, String description, PlayerClass requiredClass,
                        int baseClassPointCost, int upgradeClassPointCost, int maxSkillLevel,
                        boolean isDefaultSkill, Identifier icon, List<SkillRequirement> prerequisites) {
        super(id, name, description, 0.0f, 0.0f, requiredClass, baseClassPointCost,
                upgradeClassPointCost, maxSkillLevel, isDefaultSkill, icon, prerequisites);
        this.attributeModifications = new ArrayList<>();
        this.passiveEffects = new ArrayList<>();
        initializePassiveEffects();
    }

    // Constructor without prerequisites (defaults to empty list)
    public PassiveSkill(Identifier id, String name, String description, PlayerClass requiredClass,
                        int baseClassPointCost, int upgradeClassPointCost, int maxSkillLevel,
                        boolean isDefaultSkill, Identifier icon) {
        this(id, name, description, requiredClass, baseClassPointCost, upgradeClassPointCost,
                maxSkillLevel, isDefaultSkill, icon, null);
    }

    // Constructor with icon but without isDefaultSkill (defaults to false)
    public PassiveSkill(Identifier id, String name, String description, PlayerClass requiredClass,
                        int baseClassPointCost, int upgradeClassPointCost, int maxSkillLevel,
                        Identifier icon) {
        this(id, name, description, requiredClass, baseClassPointCost, upgradeClassPointCost,
                maxSkillLevel, false, icon, null);
    }

    // Constructor with icon and prerequisites but without isDefaultSkill (defaults to false)
    public PassiveSkill(Identifier id, String name, String description, PlayerClass requiredClass,
                        int baseClassPointCost, int upgradeClassPointCost, int maxSkillLevel,
                        Identifier icon, List<SkillRequirement> prerequisites) {
        this(id, name, description, requiredClass, baseClassPointCost, upgradeClassPointCost,
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
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return true;
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        return false;
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();
        populatePassiveTooltipData(data, player, skillLevel);
        return data;
    }

    /**
     * Override this to populate tooltip data for passive effects
     */
    protected void populatePassiveTooltipData(SkillTooltipData data, PlayerEntity player, int skillLevel) {}

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        List<Text> tooltip = new ArrayList<>();

        // Skill name with level - add PASSIVE indicator
        addPassiveSkillHeader(tooltip, skillLevel, isLearned);

        // Empty line for spacing
        tooltip.add(Text.literal(""));

        // Passive effects description
        addPassiveEffectsDescription(tooltip, skillLevel);

        // Context-specific info (no resource cost/cooldown for passives)
        addContextInfo(tooltip, player, skillLevel, isLearned, context);

        return tooltip;
    }

    private void addPassiveSkillHeader(List<Text> tooltip, int skillLevel, boolean isLearned) {
        if (isLearned) {
            tooltip.add(Text.literal(name).formatted(Formatting.WHITE, Formatting.BOLD)
                    .append(Text.literal(" (PASSIVE)").formatted(Formatting.GREEN))
                    .append(Text.literal(" (Level " + skillLevel + "/" + maxSkillLevel + ")")
                            .formatted(Formatting.GRAY)));
        } else {
            tooltip.add(Text.literal(name).formatted(Formatting.GRAY, Formatting.BOLD)
                    .append(Text.literal(" (PASSIVE)").formatted(Formatting.DARK_GREEN))
                    .append(Text.literal(" (Not Learned)").formatted(Formatting.DARK_GRAY)));
        }
    }

    /**
     * Override this to add custom passive effects description to tooltip
     */
    protected void addPassiveEffectsDescription(List<Text> tooltip, int skillLevel) {
        tooltip.add(Text.literal(description).formatted(Formatting.GRAY));

        // Add attribute modifications to tooltip
        for (AttributeModification modification : attributeModifications) {
            double value = modification.effectFunction().applyAsDouble((double) skillLevel);
            String attributeName = getAttributeDisplayName(modification.attribute());
            String valueText = formatAttributeValue(value, modification.operation());

            tooltip.add(Text.literal("• " + attributeName + ": ").formatted(Formatting.GRAY)
                    .append(Text.literal(valueText).formatted(Formatting.YELLOW)));
        }
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