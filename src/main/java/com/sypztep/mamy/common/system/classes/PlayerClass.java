package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.Mamy;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

public class PlayerClass {
    private final String id;           // e.g., "novice", "swordman", "knight"
    private final int tier;            // 0, 1, 2, 3...
    private final int branch;          // 1, 2, 3... (within same tier)
    private final String displayName;
    private final Formatting color;
    private final Map<RegistryEntry<EntityAttribute>, Double> attributeModifiers;
    private final ResourceType primaryResource;
    private final float maxResource;
    private final String description;
    private final int maxLevel;        // NEW: Max level for this class
    private final boolean isTranscendent; // NEW: Is this a transcendent class

    // Class progression requirements
    private final List<ClassRequirement> requirements;
    private final List<PlayerClass> nextClasses; // Possible evolution paths

    // Constructor with transcendent support
    public PlayerClass(String id, int tier, int branch, String displayName, Formatting color,
                       Map<RegistryEntry<EntityAttribute>, Double> attributeModifiers,
                       ResourceType primaryResource, float maxResource, String description,
                       int maxLevel, boolean isTranscendent) {
        this.id = id;
        this.tier = tier;
        this.branch = branch;
        this.displayName = displayName;
        this.color = color;
        this.attributeModifiers = attributeModifiers;
        this.primaryResource = primaryResource;
        this.maxResource = maxResource;
        this.description = description;
        this.maxLevel = maxLevel;
        this.isTranscendent = isTranscendent;
        this.requirements = new ArrayList<>();
        this.nextClasses = new ArrayList<>();
    }

    public PlayerClass addRequirement(PlayerClass previousClass, int requiredLevel) {
        requirements.add(new ClassRequirement(previousClass, requiredLevel));
        if (previousClass != null) {
            previousClass.nextClasses.add(this);
        }
        return this;
    }

    public PlayerClass addTranscendentRequirement(PlayerClass previousClass, int requiredLevel) {
        requirements.add(new ClassRequirement(previousClass, requiredLevel));
        if (previousClass != null) {
            previousClass.nextClasses.add(this);
        }
        return this;
    }

    public boolean canEvolveFrom(PlayerClass currentClass, int currentClassLevel) {
        if (requirements.isEmpty()) return true; // Base class (Novice)

        for (ClassRequirement req : requirements) {
            if (req.previousClass == currentClass && currentClassLevel >= req.requiredLevel) {
                return true;
            }
        }
        return false;
    }

    public boolean canTranscendFrom(PlayerClass currentClass, int currentClassLevel) {
        if (this.tier != 3 || currentClass.tier != 2) return false;
        return canEvolveFrom(currentClass, currentClassLevel);
    }

    public String getClassCode() {
        return tier + "-" + branch + (isTranscendent ? "T" : "");
    }

    public boolean isHigherTierThan(PlayerClass other) {
        return this.tier > other.tier;
    }

    public List<PlayerClass> getPossibleEvolutions() {
        return new ArrayList<>(nextClasses);
    }

    // NEW: Get transcendent evolutions only
    public List<PlayerClass> getTranscendentEvolutions() {
        return nextClasses.stream()
                .filter(PlayerClass::isTranscendent)
                .toList();
    }

    public void applyAttributeModifiers(LivingEntity entity) {
        for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : attributeModifiers.entrySet()) {
            EntityAttributeInstance attribute = entity.getAttributeInstance(entry.getKey());
            if (attribute != null) {
                Identifier modifierId = getClassModifierId();
                attribute.removeModifier(modifierId);

                double effectValue = entry.getValue();

                // For health, replace the base value instead of adding to it
                if (entry.getKey().equals(EntityAttributes.GENERIC_MAX_HEALTH)) {
                    double vanillaBase = 20.0; // Minecraft default health
                    double classBase = effectValue;
                    effectValue = classBase - vanillaBase; // This will make total = classBase
                }

                attribute.addTemporaryModifier(new EntityAttributeModifier(
                        modifierId,
                        effectValue,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
        }

        if (entity instanceof PlayerEntity player) updatePlayerHealth(player);
    }

    public void removeAttributeModifiers(LivingEntity entity) {
        Identifier modifierId = getClassModifierId();
        for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : attributeModifiers.entrySet()) {
            EntityAttributeInstance attribute = entity.getAttributeInstance(entry.getKey());
            if (attribute != null) attribute.removeModifier(modifierId);
        }
    }

    private void updatePlayerHealth(PlayerEntity player) {
        float currentHealth = player.getHealth();
        float oldMaxHealth = player.getMaxHealth();
        float newMaxHealth = (float) player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);

        // Maintain health percentage when max health changes
        if (oldMaxHealth != newMaxHealth && oldMaxHealth > 0) {
            float healthPercentage = currentHealth / oldMaxHealth;
            player.setHealth(healthPercentage * newMaxHealth);
        }
    }

    private Identifier getClassModifierId() {
        return Mamy.id("class_modify_" + id);
    }

    public Text getFormattedName() {
        String transcendentPrefix = isTranscendent ? "High " : "";
        return Text.literal(transcendentPrefix + displayName).formatted(color);
    }

    public Text getFormattedNameWithTier() {
        return Text.literal("[" + getClassCode() + "] " + displayName).formatted(color);
    }

    public Text getFormattedDescription() {
        return Text.literal(description).formatted(Formatting.GRAY);
    }

    // Getters
    public String getId() { return id; }
    public int getTier() { return tier; }
    public int getBranch() { return branch; }
    public String getDisplayName() { return displayName; }
    public Formatting getColor() { return color; }
    public Map<RegistryEntry<EntityAttribute>, Double> getAttributeModifiers() { return attributeModifiers; }
    public ResourceType getPrimaryResource() { return primaryResource; }
    public float getMaxResource() { return maxResource; }
    public String getDescription() { return description; }
    public List<ClassRequirement> getRequirements() { return requirements; }
    public List<PlayerClass> getNextClasses() { return nextClasses; }
    public int getMaxLevel() { return maxLevel; } // NEW
    public boolean isTranscendent() { return isTranscendent; } // NEW

    public record ClassRequirement(PlayerClass previousClass, int requiredLevel) {}
}