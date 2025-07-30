package com.sypztep.mamy.common.system.passive;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.AttributeModification;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class PassiveAbility {
    protected final String id;
    protected final Map<StatTypes, Integer> requirements;
    protected final List<AttributeModification> attributeModifications;

    public PassiveAbility(String id, Map<StatTypes, Integer> requirements) {
        this.id = id;
        this.requirements = requirements;
        this.attributeModifications = new ArrayList<>();
        initializeEffects();
    }

    /**
     * Override this to define what attribute effects this ability provides
     */
    protected abstract void initializeEffects();

    /**
     * Check if player meets the stat requirements for this ability
     */
    public boolean meetsRequirements(PlayerEntity player) {
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.get(player);

        for (var entry : requirements.entrySet()) {
            StatTypes statType = entry.getKey();
            int requiredValue = entry.getValue();
            int currentValue = levelComponent.getStatValue(statType);

            if (currentValue < requiredValue) {
                return false;
            }
        }
        return true;
    }

    /**
     * Apply all attribute effects to the player
     */
    public void applyEffects(PlayerEntity player) {
        for (AttributeModification modification : attributeModifications) {
            EntityAttributeInstance attributeInstance = player.getAttributeInstance(modification.attribute());
            if (attributeInstance != null) {
                double baseValue = player.getAttributeBaseValue(modification.attribute());
                double effectValue = modification.effectFunction().applyAsDouble(baseValue);

                // Remove existing modifier if present
                EntityAttributeModifier existingModifier = attributeInstance.getModifier(modification.modifierId());
                if (existingModifier != null) {
                    attributeInstance.removeModifier(existingModifier);
                }

                // Apply new modifier
                EntityAttributeModifier modifier = new EntityAttributeModifier(
                        modification.modifierId(), effectValue, modification.operation()
                );
                attributeInstance.addPersistentModifier(modifier);
            }
        }

        // Call custom application logic
        onApply(player);
    }

    /**
     * Remove all attribute effects from the player
     */
    public void removeEffects(PlayerEntity player) {
        for (AttributeModification modification : attributeModifications) {
            EntityAttributeInstance attributeInstance = player.getAttributeInstance(modification.attribute());
            if (attributeInstance != null) {
                EntityAttributeModifier existingModifier = attributeInstance.getModifier(modification.modifierId());
                if (existingModifier != null) {
                    attributeInstance.removeModifier(existingModifier);
                }
            }
        }

        // Call custom removal logic
        onRemove(player);
    }

    /**
     * Override for custom application logic (non-attribute effects)
     */
    protected void onApply(PlayerEntity player) {
        // Default: do nothing
    }

    /**
     * Override for custom removal logic
     */
    protected void onRemove(PlayerEntity player) {
        // Default: do nothing
    }

    /**
     * Add an attribute modification to this ability
     */
    protected void addAttributeEffect(AttributeModification modification) {
        this.attributeModifications.add(modification);
    }

    /**
     * Get the display name for this ability
     */
    public abstract Text getDisplayName();

    /**
     * Get the description for this ability
     */
    public abstract Text getDescription();

    /**
     * Get the requirements as a formatted text
     */
    public List<Text> getRequirementText() {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.literal("Requirements:").formatted(Formatting.GRAY));

        for (var entry : requirements.entrySet()) {
            StatTypes statType = entry.getKey();
            int required = entry.getValue();

            lines.add(Text.literal("  " + statType.getAka() + ": " + required)
                    .formatted(Formatting.WHITE));
        }

        return lines;
    }

    /**
     * Get full tooltip information
     */
    public List<Text> getTooltip(PlayerEntity player) {
        List<Text> tooltip = new ArrayList<>();

        // Name and status
        boolean unlocked = meetsRequirements(player);
        tooltip.add(getDisplayName().copy()
                .formatted(unlocked ? Formatting.GREEN : Formatting.RED)
                .append(Text.literal(unlocked ? " ✓" : " ✗").formatted(Formatting.GRAY)));

        // Description
        tooltip.add(Text.literal(""));
        tooltip.add(getDescription().copy().formatted(Formatting.GRAY));

        // Requirements
        tooltip.add(Text.literal(""));
        tooltip.addAll(getRequirementText());

        // Current stats
        if (!unlocked) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Current Stats:").formatted(Formatting.DARK_GRAY));

            var levelComponent = com.sypztep.mamy.common.init.ModEntityComponents.LIVINGLEVEL.get(player);
            for (var entry : requirements.entrySet()) {
                StatTypes statType = entry.getKey();
                int required = entry.getValue();
                int current = levelComponent.getStatValue(statType);

                Formatting color = current >= required ? Formatting.GREEN : Formatting.RED;
                tooltip.add(Text.literal("  " + statType.getAka() + ": " + current + "/" + required)
                        .formatted(color));
            }
        }

        return tooltip;
    }

    // Getters
    public String getId() { return id; }
    public Map<StatTypes, Integer> getRequirements() { return Collections.unmodifiableMap(requirements); }
    public List<AttributeModification> getAttributeModifications() { return Collections.unmodifiableList(attributeModifications); }
}