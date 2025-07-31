package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.skill.novice.BasicAttackSkill;
import com.sypztep.mamy.common.system.skill.swordman.BloodlustSkill;
import net.minecraft.util.Identifier;

import java.util.*;

public class SkillRegistry {
    private static final Map<Identifier, Skill> SKILLS = new HashMap<>();

    // Static skill IDs
    public static final Identifier BASIC_ATTACK = Mamy.id("basic_attack");
    public static final Identifier BLOODLUST = Mamy.id("bloodlust");
    public static final Identifier SHIELD_BASH = Mamy.id("shield_bash");
    public static final Identifier FIREBALL = Mamy.id("fireball");
    public static final Identifier HEALING_LIGHT = Mamy.id("healing_light");
    public static final Identifier ARROW_SHOWER = Mamy.id("arrow_shower");
    public static final Identifier SHADOW_STEP = Mamy.id("shadow_step");
    public static final Identifier BERSERKER_RAGE = Mamy.id("berserker_rage");
    public static final Identifier METEOR_STRIKE = Mamy.id("meteor_strike");
    public static final Identifier DIVINE_JUDGMENT = Mamy.id("divine_judgment");

    public static void registerSkills() {
        register(new BasicAttackSkill());
        register(new BloodlustSkill());
        // register(new ShieldBashSkill());
        // register(new FireballSkill());
        // register(new HealingLightSkill());
        // register(new ArrowShowerSkill());
        // register(new ShadowStepSkill());
        // register(new BerserkerRageSkill());
        // register(new MeteorStrikeSkill());
        // register(new DivineJudgmentSkill());
    }

    private static void register(Skill skill) {
        SKILLS.put(skill.getId(), skill);
    }

    // ====================
    // BASIC GETTERS
    // ====================

    public static Skill getSkill(Identifier id) {
        return SKILLS.get(id);
    }

    public static Skill getSkill(String id) {
        return SKILLS.get(Mamy.id(id));
    }

    public static Collection<Skill> getAllSkills() {
        return SKILLS.values();
    }

    // ====================
    // QUERY METHODS (เหมือน PassiveAbility)
    // ====================

    /**
     * Get skills for a specific class
     */
    public static List<Skill> getSkillsForClass(String classId) {
        return SKILLS.values().stream()
                .filter(skill -> skill.getRequiredClass() != null &&
                        skill.getRequiredClass().getId().equals(classId))
                .toList();
    }

    /**
     * Get skills available at a specific level for a class
     */
    public static List<Skill> getSkillsForLevel(String classId, int level) {
        return getSkillsForClass(classId).stream()
                .filter(skill -> skill.getRequiredClassLevel() <= level)
                .toList();
    }

    /**
     * Get skills that unlock at exactly this level
     */
    public static List<Skill> getSkillsUnlockedAtLevel(String classId, int level) {
        return getSkillsForClass(classId).stream()
                .filter(skill -> skill.getRequiredClassLevel() == level)
                .toList();
    }

    /**
     * Get all unlockable skills for a class ordered by level
     */
    public static List<Skill> getSkillsOrderedByLevel(String classId) {
        return getSkillsForClass(classId).stream()
                .sorted(Comparator.comparingInt(Skill::getRequiredClassLevel))
                .toList();
    }

    /**
     * Check if any skills unlock at this level
     */
    public static boolean hasSkillsAtLevel(String classId, int level) {
        return !getSkillsUnlockedAtLevel(classId, level).isEmpty();
    }

    /**
     * Get next level that unlocks skills
     */
    public static int getNextSkillLevel(String classId, int currentLevel) {
        return getSkillsForClass(classId).stream()
                .mapToInt(Skill::getRequiredClassLevel)
                .filter(level -> level > currentLevel)
                .min()
                .orElse(-1); // No more skills
    }
}