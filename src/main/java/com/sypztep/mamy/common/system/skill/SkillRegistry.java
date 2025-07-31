package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.common.system.skill.novice.BasicAttackSkill;
import com.sypztep.mamy.common.system.skill.swordman.BloodlustSkill;

import java.util.*;

public class SkillRegistry {
    private static final Map<String, Skill> SKILLS = new HashMap<>();

    // Register all skills
    public static void registerSkills() {
        // Novice Skills
        register(new BasicAttackSkill());

        // Swordman Skills
        register(new BloodlustSkill());
//        register(new ShieldBashSkill());
//
//        // Mage Skills
//        register(new FireballSkill());
//        register(new HealingLightSkill());
//
//        // Archer Skills
//        register(new ArrowShowerSkill());
//        register(new ShadowStepSkill());
//
//        // Knight Skills (Tier 2)
//        register(new BerserkerRageSkill());
//
//        // Wizard Skills (Tier 2)
//        register(new MeteorStrikeSkill());
//
//        // Crusader Skills (Tier 2)
//        register(new DivineJudgmentSkill());
    }

    private static void register(Skill skill) {
        SKILLS.put(skill.getId(), skill);
    }

    public static Skill getSkill(String id) {
        return SKILLS.get(id);
    }

    public static Collection<Skill> getAllSkills() {
        return SKILLS.values();
    }

    public static List<Skill> getSkillsForClass(String classId) {
        return SKILLS.values().stream()
                .filter(skill -> skill.getRequiredClass() != null && skill.getRequiredClass().getId().equals(classId))
                .toList();
    }
}