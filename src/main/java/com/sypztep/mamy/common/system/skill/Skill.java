package com.sypztep.mamy.common.system.skill;

import net.minecraft.entity.LivingEntity;
import com.sypztep.mamy.common.system.classes.PlayerClass;

public abstract class Skill {
    protected final String id;
    protected final String name;
    protected final String description;
    protected final float resourceCost;
    protected final int cooldown; // in ticks
    protected final int maxLevel;
    protected final PlayerClass requiredClass;
    protected final int requiredClassLevel;

    public Skill(String id, String name, String description, float resourceCost,
                 int cooldown, int maxLevel, PlayerClass requiredClass, int requiredClassLevel) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.resourceCost = resourceCost;
        this.cooldown = cooldown;
        this.maxLevel = maxLevel;
        this.requiredClass = requiredClass;
        this.requiredClassLevel = requiredClassLevel;
    }

    // Abstract methods for skill implementation
    public abstract boolean canUse(LivingEntity caster);
    public abstract void use(LivingEntity caster, int level);
    public abstract boolean isAvailableForClass(PlayerClass playerClass);

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getResourceCost() { return resourceCost; }
    public int getCooldown() { return cooldown; }
    public int getMaxLevel() { return maxLevel; }
    public PlayerClass getRequiredClass() { return requiredClass; }
    public int getRequiredClassLevel() { return requiredClassLevel; }
}
