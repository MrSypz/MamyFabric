package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.util.NumberUtil;
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
//TODO : Replace hardcode section on jobhealthmodify and resource
public class PlayerClass {
    public record JobBonuses(short str, short agi, short vit, short intel, short dex, short luk) {
        public static final JobBonuses NONE = new JobBonuses((short)0, (short)0, (short)0, (short)0, (short)0, (short)0);
    }
    public record ClassRequirement(PlayerClass previousClass, int requiredLevel) {}

    private final String id;
    private final int tier;
    private final int branch;
    private final String displayName;
    private final Formatting color;
    private final Map<RegistryEntry<EntityAttribute>, Double> attributeModifiers;
    private final Map<RegistryEntry<EntityAttribute>, GrowthFactor> growthFactors;
    private final ResourceType primaryResource;
    private final String description;
    private final int maxLevel;
    private final boolean isTranscendent;
    private final JobBonuses jobBonuses;
    private final double jobHealthModify; // Add this field

    // Class progression requirements
    private final List<ClassRequirement> requirements;
    private final List<PlayerClass> nextClasses;

    private PlayerClass(Builder builder) {
        this.id = builder.id;
        this.tier = builder.tier;
        this.branch = builder.branch;
        this.displayName = builder.displayName;
        this.color = builder.color;
        this.primaryResource = builder.primaryResource;
        this.description = builder.description;
        this.maxLevel = builder.maxLevel;
        this.isTranscendent = builder.isTranscendent;
        this.jobBonuses = builder.jobBonuses;
        this.jobHealthModify = builder.jobHealthModify;
        this.requirements = new ArrayList<>();
        this.nextClasses = new ArrayList<>();
        this.growthFactors = new HashMap<>(builder.growthFactors);

        // Handle attribute modifiers and resource
        this.attributeModifiers = new HashMap<>(builder.attributeModifiers);
        if (builder.resourceAmount > 0) this.attributeModifiers.put(ModEntityAttributes.RESOURCE, (double) builder.resourceAmount);
    }

    // Builder class
    public static class Builder {
        private final String id;
        private final int tier;
        private final int branch;
        private final String displayName;
        private final Formatting color;
        private final ResourceType primaryResource;
        private final String description;
        private double jobHealthModify = 0.0; // Default value

        // Optional fields with defaults
        private Map<RegistryEntry<EntityAttribute>, Double> attributeModifiers = new HashMap<>();
        private Map<RegistryEntry<EntityAttribute>, GrowthFactor> growthFactors = new HashMap<>();
        private int resourceAmount = 0;
        private JobBonuses jobBonuses = JobBonuses.NONE;
        private int maxLevel = 50;
        private boolean isTranscendent = false;

        public Builder(String id, int tier, int branch, String displayName, Formatting color, ResourceType primaryResource, String description) {
            this.id = id;
            this.tier = tier;
            this.branch = branch;
            this.displayName = displayName;
            this.color = color;
            this.primaryResource = primaryResource;
            this.description = description;
        }

        public Builder attributes(Map<RegistryEntry<EntityAttribute>, Double> attributeModifiers) {
            this.attributeModifiers = new HashMap<>(attributeModifiers);
            return this;
        }

        public Builder attribute(RegistryEntry<EntityAttribute> attribute, double value) {
            this.attributeModifiers.put(attribute, value);
            return this;
        }

        public Builder growthFactors(Map<RegistryEntry<EntityAttribute>, GrowthFactor> growthFactors) {
            this.growthFactors = new HashMap<>(growthFactors);
            return this;
        }

        public Builder growth(RegistryEntry<EntityAttribute> attribute, GrowthFactor factor) {
            this.growthFactors.put(attribute, factor);
            return this;
        }

        public Builder resource(int resourceAmount) {
            this.resourceAmount = resourceAmount;
            return this;
        }

        public Builder jobBonuses(short str, short agi, short vit, short intel, short dex, short luk) {
            this.jobBonuses = new JobBonuses(str, agi, vit, intel, dex, luk);
            return this;
        }
        public Builder jobHealthModify(double modifier) {
            this.jobHealthModify = modifier;
            return this;
        }
        public Builder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public Builder transcendent() {
            this.isTranscendent = true;
            return this;
        }

        public PlayerClass build() {
            return new PlayerClass(this);
        }
    }

    // Static factory method for cleaner API
    public static Builder create(String id, int tier, int branch, String displayName, Formatting color, ResourceType primaryResource, String description) {
        return new Builder(id, tier, branch, displayName, color, primaryResource, description);
    }

    public PlayerClass addRequirement(PlayerClass previousClass, int requiredLevel) {
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

    /**
     * Get max resource from player's current RESOURCE attribute value
     */
    public float getMaxResource(PlayerEntity player) {
        if (player == null) return getBaseMaxResource();
        return (float) player.getAttributeValue(ModEntityAttributes.RESOURCE);
    }

    /**
     * Get base max resource calculation (class base + class bonus only)
     * Use this for UI display when player context isn't available
     */
    public float getBaseMaxResource() {
        double classBonus = attributeModifiers.getOrDefault(ModEntityAttributes.RESOURCE, 0.0);
        return (float) (classBonus);
    }
    public void applyClassAttributeModifiers(LivingEntity entity) {
        int baseLvl = 1;
        if (entity instanceof PlayerEntity player) {
            LivingLevelComponent baseComponent = ModEntityComponents.LIVINGLEVEL.getNullable(player);
            if (baseComponent != null) baseLvl = baseComponent.getLevel();
        }

        for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : attributeModifiers.entrySet()) {
            EntityAttributeInstance attribute = entity.getAttributeInstance(entry.getKey());
            if (attribute != null) {
                Identifier modifierId = getClassModifierId();
                attribute.removeModifier(modifierId);

                double baseValue = entry.getValue();
                double effectValue = baseValue;

                GrowthFactor growth = growthFactors.get(entry.getKey());
                if (growth != null) {
                    double growthBonus = growth.calculateGrowth(baseValue, baseLvl - 1);
                    effectValue += growthBonus;
                }

                if (entry.getKey().equals(EntityAttributes.GENERIC_MAX_HEALTH)) {
                    effectValue = entry.getValue();
                    effectValue += baseLvl * 5;

                    for (int i = 2; i <= baseLvl; i++) effectValue += Math.round(jobHealthModify * i);
                    effectValue = effectValue - 20.0;
                }

                attribute.addPersistentModifier(new EntityAttributeModifier(modifierId, effectValue, EntityAttributeModifier.Operation.ADD_VALUE));
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

    public JobBonuses getProgressiveJobBonuses(int currentClassLevel) {
        if (jobBonuses == null || currentClassLevel <= 0) {
            return JobBonuses.NONE;
        }

        int maxLevel = getMaxLevel();
        int level = Math.min(currentClassLevel, maxLevel);

        double progression = (double) level / maxLevel;

        // Apply progression to each stat
        short progressiveStr = (short) Math.floor(jobBonuses.str() * progression);
        short progressiveAgi = (short) Math.floor(jobBonuses.agi() * progression);
        short progressiveVit = (short) Math.floor(jobBonuses.vit() * progression);
        short progressiveInt = (short) Math.floor(jobBonuses.intel() * progression);
        short progressiveDex = (short) Math.floor(jobBonuses.dex() * progression);
        short progressiveLuk = (short) Math.floor(jobBonuses.luk() * progression);

        return new JobBonuses(progressiveStr, progressiveAgi, progressiveVit,
                progressiveInt, progressiveDex, progressiveLuk);
    }

    public String getGrowthDescription(RegistryEntry<EntityAttribute> attribute) {
        GrowthFactor growth = growthFactors.get(attribute);
        if (growth == null) return "";

        StringBuilder desc = new StringBuilder();

        if (growth.flatPerLevel() > 0) {
            desc.append("+").append(NumberUtil.formatDouble(growth.flatPerLevel(), 3)).append("/lvl");
        }

        if (growth.percentPerLevel() > 0) {
            if (!desc.isEmpty()) desc.append(" ");
            desc.append("+").append(NumberUtil.formatDouble(growth.percentPerLevel() * 100, 2)).append("%/lvl");
        }

        return desc.toString();
    }

    private Identifier getClassModifierId() {
        return Mamy.id("class_modify_" + id);
    }

    public Text getFormattedName() {
        String transcendentPrefix = isTranscendent ? "High " : "";
        return Text.literal(transcendentPrefix + displayName).formatted(color);
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
    public JobBonuses getJobBonuses() { return jobBonuses; }
}