package com.sypztep.mamy.common.system.skill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import com.sypztep.mamy.common.system.classes.PlayerClass;

public abstract class Skill {
    protected final Identifier id;
    protected final String name;
    protected final String description;
    protected final float resourceCost;
    protected final int cooldown; // in ticks
    protected final int maxLevel;
    protected final PlayerClass requiredClass;
    protected final int requiredClassLevel;
    protected final Identifier icon; // NEW: Skill icon

    public Skill(Identifier id, String name, String description, float resourceCost,
                 int cooldown, int maxLevel, PlayerClass requiredClass, int requiredClassLevel, Identifier icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.resourceCost = resourceCost;
        this.cooldown = cooldown;
        this.maxLevel = maxLevel;
        this.requiredClass = requiredClass;
        this.requiredClassLevel = requiredClassLevel;
        this.icon = icon; // Default icon
    }

    public Skill(Identifier id, String name, String description, float resourceCost,
                 int cooldown, int maxLevel, PlayerClass requiredClass, int requiredClassLevel) {
        this(id, name, description, resourceCost, cooldown, maxLevel, requiredClass, requiredClassLevel, null);
    }

    public abstract boolean canUse(LivingEntity caster);
    public abstract void use(LivingEntity caster, int level);
    public abstract boolean isAvailableForClass(PlayerClass playerClass);

    public float getResourceCost(int level) {
        return resourceCost; // Base implementation, can be overridden
    }

    public int getCooldown(int level) {
        return Math.max(1, cooldown - (level - 1) * 2); // Reduce cooldown by 2 ticks per level
    }

    public String getDescription(int level) {
        return description; // Base implementation, can be overridden for level-specific descriptions
    }

    // Getters
    public Identifier getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getResourceCost() { return resourceCost; }
    public int getCooldown() { return cooldown; }
    public int getMaxLevel() { return maxLevel; }
    public PlayerClass getRequiredClass() { return requiredClass; }
    public int getRequiredClassLevel() { return requiredClassLevel; }
    public Identifier getIcon() { return icon; }
}