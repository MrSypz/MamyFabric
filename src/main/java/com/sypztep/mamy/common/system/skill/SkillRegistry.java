package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.novice.BasicPassiveSkill;
import com.sypztep.mamy.common.system.skill.swordman.BashingBlowSkill;
import com.sypztep.mamy.common.system.skill.swordman.EndureSkill;
import com.sypztep.mamy.common.system.skill.swordman.EnergyBreakSkill;
import com.sypztep.mamy.common.system.skill.swordman.passive.SwordMasteryPassiveSkill;
import net.minecraft.util.Identifier;

import java.util.*;

public class SkillRegistry {
    private static final Map<Identifier, Skill> SKILLS = new HashMap<>();
    //Novice
    public static final Identifier BASICSKILL = Mamy.id("basic_skill");
    //SwordMan
    public static final Identifier BASHING_BLOW = Mamy.id("bashing_blow");
    public static final Identifier ENERGY_BREAK = Mamy.id("energy_break");
    public static final Identifier ENDURE = Mamy.id("endure");
    public static final Identifier SWORD_MASTERY = Mamy.id("sword_mastery");

    public static void registerSkills() {
        //Novice
        register(new BasicPassiveSkill(BASICSKILL));

        //SwordMan
        register(new SwordMasteryPassiveSkill(SWORD_MASTERY));
        register(new BashingBlowSkill(BASHING_BLOW));
        register(new EnergyBreakSkill(ENERGY_BREAK));
        register(new EndureSkill(ENDURE));

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