package com.sypztep.mamy.common.system.skill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.classes.ResourceType;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.damage.HybridDamageSource;
import com.sypztep.mamy.common.system.damage.DamageComponent;
import com.sypztep.mamy.common.system.damage.CombatType;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public abstract class Skill implements HybridDamageSource {
    protected final Identifier id;
    protected final String name;
    protected final String description;
    protected final float baseResourceCost;
    protected final float baseCooldown;
    protected final int baseClassPointCost;
    protected final int upgradeClassPointCost;
    protected final int maxSkillLevel;
    protected final boolean isDefaultSkill;
    protected final Identifier icon;
    protected final List<SkillRequirement> prerequisites;

    public Skill(Identifier id, String name, String description, float baseResourceCost,
                 float baseCooldown, int baseClassPointCost, int upgradeClassPointCost,
                 int maxSkillLevel, boolean isDefaultSkill, Identifier icon,
                 List<SkillRequirement> prerequisites) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseResourceCost = baseResourceCost;
        this.baseCooldown = baseCooldown;
        this.baseClassPointCost = baseClassPointCost;
        this.upgradeClassPointCost = upgradeClassPointCost;
        this.maxSkillLevel = maxSkillLevel;
        this.isDefaultSkill = isDefaultSkill;
        this.icon = icon;
        this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
    }

    public Skill(Identifier id, String name, String description, float baseResourceCost,
                 float baseCooldown,
                 int maxSkillLevel, Identifier icon,
                 List<SkillRequirement> prerequisites) {
        this(id, name, description, baseResourceCost, baseCooldown, 1,
                1, maxSkillLevel, false, icon, prerequisites);
    }

    public Skill(Identifier id, String name, String description, float baseResourceCost,
                 float baseCooldown,
                 int maxSkillLevel, boolean isDefaultSkill, Identifier icon) {
        this(id, name, description, baseResourceCost, baseCooldown, 1,
                1, maxSkillLevel, isDefaultSkill, icon, null);
    }

    public Skill(Identifier id, String name, String description, float baseResourceCost,
                 float baseCooldown,
                 int maxSkillLevel, Identifier icon) {
        this(id, name, description, baseResourceCost, baseCooldown, 1,
                1, maxSkillLevel, false, icon, null);
    }

    public Skill(Identifier id, String name, String description, float baseResourceCost,
                 float baseCooldown,  int baseClassPointCost,
                 int upgradeClassPointCost, int maxSkillLevel, boolean isDefaultSkill, Identifier icon) {
        this(id, name, description, baseResourceCost, baseCooldown,
                baseClassPointCost, upgradeClassPointCost, maxSkillLevel, isDefaultSkill, icon, null);
    }

    // ============================================================================
    // HYBRID DAMAGE SOURCE IMPLEMENTATION
    // ============================================================================

    /**
     * Default implementation returns Physical Melee damage
     * Override this method in skills that need custom elemental/combat type combinations
     */
    @Override
    public List<DamageComponent> getDamageComponents() {
        return List.of(DamageComponent.pureCombat(CombatType.MELEE, 1.0f));
    }

    /**
     * Helper method to create simple elemental skills
     */
    protected List<DamageComponent> createElementalDamage(com.sypztep.mamy.common.system.damage.ElementType elementType) {
        return List.of(DamageComponent.pureElemental(elementType, 1.0f));
    }

    /**
     * Helper method to create simple combat type skills
     */
    protected List<DamageComponent> createCombatDamage(CombatType combatType) {
        return List.of(DamageComponent.pureCombat(combatType, 1.0f));
    }

    /**
     * Helper method to create hybrid skills
     */
    protected List<DamageComponent> createHybridDamage(com.sypztep.mamy.common.system.damage.ElementType elementType, float elementalWeight,
                                                       CombatType combatType, float combatWeight) {
        return List.of(DamageComponent.hybrid(elementType, elementalWeight, combatType, combatWeight));
    }

    /**
     * Helper method to create multi-component skills
     */
    protected List<DamageComponent> createMultiComponentDamage(DamageComponent... components) {
        return Arrays.asList(components);
    }

    // ============================================================================
    // SKILL REQUIREMENTS AND PREREQUISITES
    // ============================================================================

    public static SkillRequirement requires(Identifier skillId, int minLevel) {
        return new SkillRequirement(skillId, minLevel);
    }

    public static List<SkillRequirement> requiresSkills(SkillRequirement... requirements) {
        return Arrays.asList(requirements);
    }

    public boolean arePrerequisitesMet(PlayerEntity player) {
        if (prerequisites.isEmpty()) return true;

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);

        for (SkillRequirement req : prerequisites) {
            if (!classComponent.hasLearnedSkill(req.skillId())) return false;

            int currentLevel = classComponent.getSkillLevel(req.skillId());

            if (currentLevel < req.minLevel()) return false;
        }
        return true;
    }

    public List<SkillRequirement> getMissingPrerequisites(PlayerEntity player) {
        List<SkillRequirement> missing = new ArrayList<>();
        if (prerequisites.isEmpty()) return missing;

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);

        for (SkillRequirement req : prerequisites) {
            boolean learned = classComponent.hasLearnedSkill(req.skillId());
            int currentLevel = learned ? classComponent.getSkillLevel(req.skillId()) : 0;

            if (!learned || currentLevel < req.minLevel()) {
                missing.add(req);
            }
        }
        return missing;
    }

    // ============================================================================
    // TOOLTIP GENERATION
    // ============================================================================

    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        SkillTooltipData data = getSkillTooltipData(player, skillLevel);
        return SkillTooltipRenderer.render(this, data, player, skillLevel, isLearned, context);
    }

    // ============================================================================
    // DATA CLASSES
    // ============================================================================

    public static class SkillTooltipData {
        public float baseDamage = 0;
        public float damagePercentage = 0; // As decimal (0.2 = 20%)
        public DamageTypeRef damageType = DamageTypeRef.PHYSICAL;
        public int maxHits = 1;
        public float healthPerHit = 0;
        public float resourcePerHit = 0;
        public List<SecondaryDamage> secondaryDamages = new ArrayList<>();
        
        // Enhanced fields for universal tooltip system
        public List<String> statusEffectsRemoved = new ArrayList<>();
        public List<String> statusEffectsApplied = new ArrayList<>();
        public String targetType = "Single Target";
        public float targetRange = 0;
        public String specialDescription = "";
        public String contextTip = "";
        public boolean overrideCooldown = false;
        public String customCooldownText = "";
        public boolean overrideResourceCost = false;
        public String customResourceCostText = "";
        public List<String> additionalEffects = new ArrayList<>();
        public boolean isChanneled = false;
        public boolean isPassive = false;
    }

    public record SecondaryDamage(
            DamageTypeRef damageType,
            float baseDamage,
            float damagePercentage,
            int maxHits
    ) {}

    public enum DamageTypeRef {
        MELEE, MAGIC, PHYSICAL, ELEMENT, HEAL
    }

    public enum TooltipContext {
        LEARNING_SCREEN,
        BINDING_SCREEN,
        BINDING_SLOT
    }

    public record SkillRequirement(Identifier skillId, int minLevel) {}

    // ============================================================================
    // ABSTRACT METHODS FOR SKILL DATA
    // ============================================================================

    protected abstract SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel);

    public float getResourceCost(int skillLevel) {
        return baseResourceCost;
    }

    public float getCooldown(int skillLevel) {
        return baseCooldown;
    }

    // ============================================================================
    // ABSTRACT METHODS FOR SKILL BEHAVIOR
    // ============================================================================

    public abstract boolean canUse(LivingEntity caster, int skillLevel);
    public abstract boolean use(LivingEntity caster, int skillLevel);
    public abstract boolean isAvailableForClass(PlayerClass playerClass);

    // ============================================================================
    // GETTERS
    // ============================================================================

    public Identifier getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getMaxSkillLevel() { return maxSkillLevel; }
    public int getBaseClassPointCost() { return baseClassPointCost; }
    public int getUpgradeClassPointCost() { return upgradeClassPointCost; }
    public boolean isDefaultSkill() { return isDefaultSkill; }
    public Identifier getIcon() { return icon; }
    public List<SkillRequirement> getPrerequisites() { return new ArrayList<>(prerequisites); }
}