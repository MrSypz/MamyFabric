package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.classkill.acolyte.*;
import com.sypztep.mamy.common.system.classkill.acolyte.passive.DemonBanePassiveSkill;
import com.sypztep.mamy.common.system.classkill.acolyte.passive.DivineProtectionPassiveSkill;
import com.sypztep.mamy.common.system.classkill.novice.BasicPassiveSkill;
import com.sypztep.mamy.common.system.classkill.swordman.BashingBlowSkill;
import com.sypztep.mamy.common.system.classkill.swordman.EndureSkill;
import com.sypztep.mamy.common.system.classkill.swordman.EnergyBreakSkill;
import com.sypztep.mamy.common.system.classkill.swordman.ProvokeSkill;
import com.sypztep.mamy.common.system.classkill.swordman.passive.HPRecoveryPassiveSkill;
import com.sypztep.mamy.common.system.classkill.swordman.passive.SwordMasteryPassiveSkill;
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
    public static final Identifier HP_RECOVERY = Mamy.id("hp_recovery");
    public static final Identifier PROVOKE = Mamy.id("provoke");

    //Acolyte
    public static final Identifier DEMON_BANE = Mamy.id("demon_bane");
    public static final Identifier DIVINE_PROTECTION = Mamy.id("divine_protection");
    public static final Identifier HEAL = Mamy.id("heal");
    public static final Identifier INCREASE_AGILITY = Mamy.id("increase_agility");
    public static final Identifier DECREASE_AGILITY = Mamy.id("decrease_agility");
    public static final Identifier BLESSING = Mamy.id("blessing");
    public static final Identifier ANGELUS = Mamy.id("angelus");

    public static void registerSkills() {
        //Novice
        register(new BasicPassiveSkill(BASICSKILL));

        //SwordMan
        register(new SwordMasteryPassiveSkill(SWORD_MASTERY));
        register(new HPRecoveryPassiveSkill(HP_RECOVERY));
        register(new ProvokeSkill(PROVOKE));
        register(new BashingBlowSkill(BASHING_BLOW));
        register(new EnergyBreakSkill(ENERGY_BREAK, Skill.requiresSkills(Skill.requires(BASHING_BLOW,5))));
        register(new EndureSkill(ENDURE, Skill.requiresSkills(Skill.requires(PROVOKE, 5))));
        // Acolyte
        register(new DemonBanePassiveSkill(DEMON_BANE, Skill.requiresSkills(Skill.requires(DIVINE_PROTECTION,3))));
        register(new DivineProtectionPassiveSkill(DIVINE_PROTECTION));
        register(new HealSkill(HEAL));
        register(new IncreaseAgilitySkill(INCREASE_AGILITY, Skill.requiresSkills(Skill.requires(HEAL,3))));
        register(new DecreaseAgilitySkill(DECREASE_AGILITY, Skill.requiresSkills(Skill.requires(INCREASE_AGILITY,1))));
        register(new BlessingSkill(BLESSING, Skill.requiresSkills(Skill.requires(DIVINE_PROTECTION,5))));
        register(new AngelusSkill(ANGELUS, Skill.requiresSkills(Skill.requires(DIVINE_PROTECTION,3))));
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