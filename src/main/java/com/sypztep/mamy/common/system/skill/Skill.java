package com.sypztep.mamy.common.system.skill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import com.sypztep.mamy.common.system.classes.PlayerClass;

public abstract class Skill {
    protected final Identifier id;
    protected final String name;
    protected final String description;
    protected final float resourceCost;
    protected final float cooldown;
    protected final int maxLevel;
    protected final PlayerClass requiredClass;
    protected final int requiredClassLevel;
    protected final Identifier icon;

    public Skill(Identifier id, String name, String description, float resourceCost,
                 float cooldown, int maxLevel, PlayerClass requiredClass, int requiredClassLevel, Identifier icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.resourceCost = resourceCost;
        this.cooldown = cooldown; // Store as seconds
        this.maxLevel = maxLevel;
        this.requiredClass = requiredClass;
        this.requiredClassLevel = requiredClassLevel;
        this.icon = icon;
    }

    public Skill(Identifier id, String name, String description, float resourceCost,
                 float cooldown, int maxLevel, PlayerClass requiredClass, int requiredClassLevel) {
        this(id, name, description, resourceCost, cooldown, maxLevel, requiredClass, requiredClassLevel, null);
    }

    public abstract boolean canUse(LivingEntity caster);
    public abstract void use(LivingEntity caster, int level);
    public abstract boolean isAvailableForClass(PlayerClass playerClass);

    public float getResourceCost(int level) {
        return resourceCost;
    }

    public float getCooldown(int level) {
        return Math.max(0.1f, cooldown - (level - 1) * 0.1f); // Reduce by 0.1s per level
    }

    public String getDescription(int level) {
        return description;
    }

    // Getters
    public Identifier getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getResourceCost() { return resourceCost; }
    public float getCooldown() { return cooldown; } // ✅ CHANGED: Return float
    public int getMaxLevel() { return maxLevel; }
    public PlayerClass getRequiredClass() { return requiredClass; }
    public int getRequiredClassLevel() { return requiredClassLevel; }
    public Identifier getIcon() { return icon; }
}