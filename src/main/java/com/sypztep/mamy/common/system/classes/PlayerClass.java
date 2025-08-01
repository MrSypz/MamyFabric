package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
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
    private final String id;
    private final int tier;
    private final int branch;
    private final String displayName;
    private final Formatting color;
    private final Map<RegistryEntry<EntityAttribute>, Double> attributeModifiers;
    private final Map<RegistryEntry<EntityAttribute>, GrowthFactor> growthFactors; // NEW
    private final ResourceType primaryResource;
    private final String description;
    private final int maxLevel;
    private final boolean isTranscendent;

    // Class progression requirements
    private final List<ClassRequirement> requirements;
    private final List<PlayerClass> nextClasses;

    public PlayerClass(String id, int tier, int branch, String displayName, Formatting color,
                       Map<RegistryEntry<EntityAttribute>, Double> attributeModifiers,
                       Map<RegistryEntry<EntityAttribute>, GrowthFactor> growthFactors, // NEW PARAMETER
                       ResourceType primaryResource, float resourceBonus, String description,
                       int maxLevel, boolean isTranscendent) {
        this.id = id;
        this.tier = tier;
        this.branch = branch;
        this.displayName = displayName;
        this.color = color;
        this.primaryResource = primaryResource;
        this.description = description;
        this.maxLevel = maxLevel;
        this.isTranscendent = isTranscendent;
        this.requirements = new ArrayList<>();
        this.nextClasses = new ArrayList<>();
        this.growthFactors = new HashMap<>(growthFactors);

        // Add resource bonus to attribute modifiers if > 0
        this.attributeModifiers = new HashMap<>(attributeModifiers);
        if (resourceBonus > 0) {
            this.attributeModifiers.put(ModEntityAttributes.RESOURCE, (double) resourceBonus);
        }
    }
    public PlayerClass(String id, int tier, int branch, String displayName, Formatting color,
                       Map<RegistryEntry<EntityAttribute>, Double> attributeModifiers,
                       ResourceType primaryResource, float resourceBonus, String description,
                       int maxLevel, boolean isTranscendent) {
        this(id, tier, branch, displayName, color, attributeModifiers,
                new HashMap<>(), primaryResource, resourceBonus, description, maxLevel, isTranscendent);
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
        if (requirements.isEmpty()) return true;

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

    public List<PlayerClass> getTranscendentEvolutions() {
        return nextClasses.stream()
                .filter(PlayerClass::isTranscendent)
                .toList();
    }

    /**
     * Get max resource from player's current RESOURCE attribute value
     */
    public float getMaxResource(PlayerEntity player) {
        if (player == null) {
            double baseResource = 200.0; // ModEntityAttributes.RESOURCE base
            double classBonus = attributeModifiers.getOrDefault(ModEntityAttributes.RESOURCE, 0.0);
            return (float) (baseResource + classBonus);
        }

        // Get actual current value including INT bonuses
        return (float) player.getAttributeValue(ModEntityAttributes.RESOURCE);
    }

    /**
     * Get max resource without player context (for display/calculation)
     */
    public float getMaxResource() {
        double baseResource = 200.0; // ModEntityAttributes.RESOURCE base
        double classBonus = attributeModifiers.getOrDefault(ModEntityAttributes.RESOURCE, 0.0);
        return (float) (baseResource + classBonus);
    }

    public void applyAttributeModifiers(LivingEntity entity) {
        // Get current class level
        int classLevel = 1; // Default level
        if (entity instanceof PlayerEntity player) {
            PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.getNullable(player);
            if (classComponent != null) {
                classLevel = classComponent.getClassManager().getClassLevel();
            }
        }

        // Apply base attributes + growth
        for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : attributeModifiers.entrySet()) {
            EntityAttributeInstance attribute = entity.getAttributeInstance(entry.getKey());
            if (attribute != null) {
                Identifier modifierId = getClassModifierId();
                attribute.removeModifier(modifierId);

                double baseValue = entry.getValue();
                double effectValue = baseValue;

                // Apply growth if defined for this attribute
                GrowthFactor growth = growthFactors.get(entry.getKey());
                if (growth != null) {
                    double growthBonus = growth.calculateGrowth(baseValue, classLevel - 1); // Level 1 = no growth
                    effectValue += growthBonus;
                }

                // Special handling for health
                if (entry.getKey().equals(EntityAttributes.GENERIC_MAX_HEALTH)) {
                    double vanillaBase = 20.0;
                    effectValue = effectValue - vanillaBase;
                }

                // Use ADD_VALUE for all class bonuses (flat amounts)
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

        if (oldMaxHealth != newMaxHealth && oldMaxHealth > 0) {
            float healthPercentage = currentHealth / oldMaxHealth;
            player.setHealth(healthPercentage * newMaxHealth);
        }
    }
    public String getGrowthDescription(RegistryEntry<EntityAttribute> attribute) {
        GrowthFactor growth = growthFactors.get(attribute);
        if (growth == null) return "";

        StringBuilder desc = new StringBuilder();
        if (growth.flatPerLevel() > 0) {
            desc.append(String.format("+%.1f per level", growth.flatPerLevel()));
        }
        if (growth.percentPerLevel() > 0) {
            if (!desc.isEmpty()) desc.append(", ");
            desc.append(String.format("+%.1f%% per level", growth.percentPerLevel() * 100));
        }
        return desc.toString();
    }

    public Map<RegistryEntry<EntityAttribute>, GrowthFactor> getGrowthFactors() {
        return growthFactors;
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
    public String getDescription() { return description; }
    public List<ClassRequirement> getRequirements() { return requirements; }
    public List<PlayerClass> getNextClasses() { return nextClasses; }
    public int getMaxLevel() { return maxLevel; }
    public boolean isTranscendent() { return isTranscendent; }

    public record ClassRequirement(PlayerClass previousClass, int requiredLevel) {}
}