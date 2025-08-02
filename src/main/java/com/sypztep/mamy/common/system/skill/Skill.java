package com.sypztep.mamy.common.system.skill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import com.sypztep.mamy.common.system.classes.PlayerClass;

public abstract class Skill {
    protected final Identifier id;
    protected final String name;
    protected final String description;
    protected final float baseResourceCost;
    protected final float baseCooldown;
    protected final PlayerClass requiredClass;
    protected final int baseClassPointCost; // Cost to learn the skill
    protected final int upgradeClassPointCost; // Cost to upgrade per level
    protected final int maxSkillLevel; // Maximum skill level (upgradeable)
    protected final Identifier icon;

    public Skill(Identifier id, String name, String description, float baseResourceCost,
                 float baseCooldown, PlayerClass requiredClass, int baseClassPointCost,
                 int upgradeClassPointCost, int maxSkillLevel, Identifier icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseResourceCost = baseResourceCost;
        this.baseCooldown = baseCooldown;
        this.requiredClass = requiredClass;
        this.baseClassPointCost = baseClassPointCost;
        this.upgradeClassPointCost = upgradeClassPointCost;
        this.maxSkillLevel = maxSkillLevel;
        this.icon = icon;
    }

    public Skill(Identifier id, String name, String description, float baseResourceCost,
                 float baseCooldown, PlayerClass requiredClass, int baseClassPointCost,
                 int upgradeClassPointCost, int maxSkillLevel) {
        this(id, name, description, baseResourceCost, baseCooldown, requiredClass,
                baseClassPointCost, upgradeClassPointCost, maxSkillLevel, null);
    }

    // Abstract methods
    public abstract boolean canUse(LivingEntity caster, int skillLevel);
    public abstract void use(LivingEntity caster, int skillLevel);
    public abstract boolean isAvailableForClass(PlayerClass playerClass);

    // Level-based calculations
    public float getResourceCost(int skillLevel) {
        return Math.max(1.0f, baseResourceCost - (skillLevel - 1) * 0.5f);
    }

    public float getCooldown(int skillLevel) {
        return Math.max(0.1f, baseCooldown - (skillLevel - 1) * 0.1f);
    }

    public String getDescription(int skillLevel) {
        return description + " (Level " + skillLevel + ")";
    }

    public int getClassPointCostForLevel(int targetLevel) {
        if (targetLevel == 1) return baseClassPointCost; // Learning the skill
        return upgradeClassPointCost; // Upgrading to next level
    }

    // Getters
    public Identifier getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getBaseResourceCost() { return baseResourceCost; }
    public float getBaseCooldown() { return baseCooldown; }
    public PlayerClass getRequiredClass() { return requiredClass; }
    public int getBaseClassPointCost() { return baseClassPointCost; }
    public int getUpgradeClassPointCost() { return upgradeClassPointCost; }
    public int getMaxSkillLevel() { return maxSkillLevel; }
    public Identifier getIcon() { return icon; }

    // Backward compatibility for existing methods
//    @Deprecated
//    public float getResourceCost() { return getResourceCost(1); }
//    @Deprecated
//    public float getCooldown() { return getCooldown(1); }
//    @Deprecated
//    public boolean canUse(LivingEntity caster) { return canUse(caster, 1); }
//    @Deprecated
//    public void use(LivingEntity caster, int level) { use(caster, level); }
}
