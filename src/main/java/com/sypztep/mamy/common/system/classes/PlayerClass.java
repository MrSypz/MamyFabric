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

    // Class progression requirements
    private final List<ClassRequirement> requirements;
    private final List<PlayerClass> nextClasses; // Possible evolution paths

    public PlayerClass(String id, int tier, int branch, String displayName, Formatting color,
                       Map<RegistryEntry<EntityAttribute>, Double> attributeModifiers,
                       ResourceType primaryResource, float maxResource, String description) {
        this.id = id;
        this.tier = tier;
        this.branch = branch;
        this.displayName = displayName;
        this.color = color;
        this.attributeModifiers = attributeModifiers;
        this.primaryResource = primaryResource;
        this.maxResource = maxResource;
        this.description = description;
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

    public boolean canEvolveFrom(PlayerClass currentClass, int currentClassLevel) {
        if (requirements.isEmpty()) return true; // Base class (Novice)

        for (ClassRequirement req : requirements) {
            if (req.previousClass == currentClass && currentClassLevel >= req.requiredLevel) {
                return true;
            }
        }
        return false;
    }

    public String getClassCode() {
        return tier + "-" + branch;
    }

    public boolean isHigherTierThan(PlayerClass other) {
        return this.tier > other.tier;
    }

    public List<PlayerClass> getPossibleEvolutions() {
        return new ArrayList<>(nextClasses);
    }

    public void applyAttributeModifiers(LivingEntity entity) {
        for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : attributeModifiers.entrySet()) {
            EntityAttributeInstance attribute = entity.getAttributeInstance(entry.getKey());
            if (attribute != null) {
                Identifier modifierId = getClassModifierId();
                attribute.removeModifier(modifierId);
                attribute.addTemporaryModifier(new EntityAttributeModifier(
                        modifierId,
                        entry.getValue(),
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

        if (newMaxHealth != oldMaxHealth && newMaxHealth < currentHealth) {
            float healthRatio = Math.max(0.1f, newMaxHealth / oldMaxHealth);
            player.setHealth(currentHealth * healthRatio);
        }
    }

    private Identifier getClassModifierId() {
        return Mamy.id("class_modify_" + id);
    }

    public String getId() {
        return id;
    }

    public int getTier() {
        return tier;
    }

    public int getBranch() {
        return branch;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Formatting getColor() {
        return color;
    }

    public ResourceType getPrimaryResource() {
        return primaryResource;
    }

    public float getMaxResource() {
        return maxResource;
    }

    public String getDescription() {
        return description;
    }

    public List<ClassRequirement> getRequirements() {
        return requirements;
    }

    public Text getFormattedName() {
        return Text.literal(displayName).formatted(color);
    }

    public Text getFormattedNameWithTier() {
        return Text.literal("[" + getClassCode() + "] " + displayName).formatted(color);
    }

    public Text getFormattedDescription() {
        return Text.literal(description).formatted(Formatting.GRAY);
    }

    /**
     * Requirement for class evolution
     */
    public record ClassRequirement(PlayerClass previousClass, int requiredLevel) {

        public Text getFormattedRequirement() {
            if (previousClass == null) {
                return Text.literal("Starting class").formatted(Formatting.GREEN);
            }
            return Text.literal("Requires: ").formatted(Formatting.GRAY)
                    .append(previousClass.getFormattedName())
                    .append(Text.literal(" Lv." + requiredLevel).formatted(Formatting.YELLOW));
        }
    }
}