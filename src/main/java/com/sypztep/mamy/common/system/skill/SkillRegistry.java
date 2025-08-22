package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.novice.BasicSkill;
import net.minecraft.util.Identifier;

import java.util.*;

public class SkillRegistry {
    private static final Map<Identifier, Skill> SKILLS = new HashMap<>();

    public static final Identifier BASICSKILL = Mamy.id("basic_skill");
    public static final Identifier FIRSTAID = Mamy.id("first_aid");
//    public static final Identifier BLOODLUST = Mamy.id("bloodlust");
//    public static final Identifier SWORD_MASTERY = Mamy.id("sword_mastery");
//    public static final Identifier DOUBLE_ATTACK = Mamy.id("double_attack");
//    public static final Identifier SHIELD_BASH = Mamy.id("shield_bash");
//    public static final Identifier FIREBALL = Mamy.id("fireball");
//    public static final Identifier HEALING_LIGHT = Mamy.id("healing_light");
//    public static final Identifier ARROW_SHOWER = Mamy.id("arrow_shower");
//    public static final Identifier SHADOW_STEP = Mamy.id("shadow_step");
//    public static final Identifier BERSERKER_RAGE = Mamy.id("berserker_rage");
//    public static final Identifier METEOR_STRIKE = Mamy.id("meteor_strike");
//    public static final Identifier DIVINE_JUDGMENT = Mamy.id("divine_judgment");

    public static void registerSkills() {
        register(new BasicSkill(BASICSKILL));
//        register(new FirstAidSkill(FIRSTAID));
//        register(new BloodlustSkill(BLOODLUST));
//        register(new SwordMasteryPassiveSkill(SWORD_MASTERY));
//        register(new DoubleAttackPassiveSkill(DOUBLE_ATTACK));
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
    // UPDATED QUERY METHODS
    // ====================

    /**
     * Get all skills available for a specific class
     */
    public static List<Skill> getSkillsForClass(String classId) {
        return SKILLS.values().stream()
                .filter(skill -> skill.getRequiredClass() != null &&
                        skill.getRequiredClass().getId().equals(classId))
                .toList();
    }

    /**
     * Get all skills available for a specific class (by PlayerClass object)
     */
    public static List<Skill> getSkillsForClass(PlayerClass playerClass) {
        return SKILLS.values().stream()
                .filter(skill -> skill.isAvailableForClass(playerClass))
                .toList();
    }

    /**
     * Get skills that can be learned with the given class points
     */
    public static List<Skill> getAffordableSkills(String classId, int availableClassPoints) {
        return getSkillsForClass(classId).stream()
                .filter(skill -> skill.getBaseClassPointCost() <= availableClassPoints)
                .toList();
    }

    /**
     * Check if a skill exists and is valid
     */
    public static boolean isValidSkill(Identifier skillId) {
        return SKILLS.containsKey(skillId);
    }

    /**
     * Get skills ordered by class point cost (cheaper first)
     */
    public static List<Skill> getSkillsOrderedByCost(String classId) {
        return getSkillsForClass(classId).stream()
                .sorted(Comparator.comparingInt(Skill::getBaseClassPointCost))
                .toList();
    }
}