package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.classkill.acolyte.*;
import com.sypztep.mamy.common.system.classkill.acolyte.passive.DemonBanePassiveSkill;
import com.sypztep.mamy.common.system.classkill.acolyte.passive.DivineProtectionPassiveSkill;
import com.sypztep.mamy.common.system.classkill.archer.ArrowRainSkill;
import com.sypztep.mamy.common.system.classkill.archer.DoubleStrafeSkill;
import com.sypztep.mamy.common.system.classkill.archer.ImproveConcentrationSkill;
import com.sypztep.mamy.common.system.classkill.archer.passive.OwlsEyePassiveSkill;
import com.sypztep.mamy.common.system.classkill.archer.passive.VulturesEyePassiveSkill;
import com.sypztep.mamy.common.system.classkill.mage.*;
import com.sypztep.mamy.common.system.classkill.mage.passive.ResourceRecoveryPassiveSkill;
import com.sypztep.mamy.common.system.classkill.novice.passive.BasicPassiveSkill;
import com.sypztep.mamy.common.system.classkill.swordman.BashingBlowSkill;
import com.sypztep.mamy.common.system.classkill.swordman.EndureSkill;
import com.sypztep.mamy.common.system.classkill.swordman.EnergyBreakSkill;
import com.sypztep.mamy.common.system.classkill.swordman.ProvokeSkill;
import com.sypztep.mamy.common.system.classkill.swordman.passive.HPRecoveryPassiveSkill;
import com.sypztep.mamy.common.system.classkill.swordman.passive.SwordMasteryPassiveSkill;
import com.sypztep.mamy.common.system.classkill.thief.DetoxifySkill;
import com.sypztep.mamy.common.system.classkill.thief.EnvenomSkill;
import com.sypztep.mamy.common.system.classkill.thief.HidingSkill;
import com.sypztep.mamy.common.system.classkill.thief.StealSkill;
import com.sypztep.mamy.common.system.classkill.thief.passive.DoubleAttackPassiveSkill;
import com.sypztep.mamy.common.system.classkill.thief.passive.ImproveDodgePassiveSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.util.Identifier;

import java.util.*;

public class ModClassesSkill {
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
    public static final Identifier CURE = Mamy.id("cure");
    public static final Identifier AQUABENEDICTA = Mamy.id("aquabenedicta");

    //Archer
    public static final Identifier OWLS_EYE = Mamy.id("owls_eye");
    public static final Identifier VULTURES_EYE = Mamy.id("vultures_eye");
    public static final Identifier IMPROVE_CONCENTRATION = Mamy.id("improve_concentration");
    public static final Identifier ARROW_RAIN = Mamy.id("arrow_rain");
    public static final Identifier DOUBLE_STRAFE = Mamy.id("double_strafe");

    //Thief
    public static final Identifier DOUBLE_ATTACK = Mamy.id("double_attack");
    public static final Identifier DETOXIFY = Mamy.id("detoxify");
    public static final Identifier ENVENOM = Mamy.id("envenom");
    public static final Identifier IMPROVE_DODGE = Mamy.id("improve_dodge");
    public static final Identifier STEAL = Mamy.id("steal");
    public static final Identifier HIDING = Mamy.id("hiding");

    //Magician
    public static final Identifier IRR = Mamy.id("increase_resource_recovery");
    public static final Identifier MAGIC_ARROW = Mamy.id("magic_arrow");
    public static final Identifier FIREBOLT = Mamy.id("firebolt");
    public static final Identifier FIREBALL = Mamy.id("fireball");
    public static final Identifier METEOR_SHOWER = Mamy.id("meteor_shower");
    public static final Identifier THUNDER_BOLT = Mamy.id("thunder_bolt");
    public static final Identifier THUNDER_STORM = Mamy.id("thunder_storm");
    public static final Identifier THUNDER_SPHERE = Mamy.id("thunder_sphere");

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
        register(new CureSkill(CURE, Skill.requiresSkills(Skill.requires(HEAL, 2))));
        register(new AquaBenedictaSkill(AQUABENEDICTA));

        // Archer
        register(new OwlsEyePassiveSkill(OWLS_EYE));
        register(new VulturesEyePassiveSkill(VULTURES_EYE, Skill.requiresSkills(Skill.requires(OWLS_EYE, 3))));
        register(new ImproveConcentrationSkill(IMPROVE_CONCENTRATION, Skill.requiresSkills(Skill.requires(VULTURES_EYE, 1))));
        register(new ArrowRainSkill(ARROW_RAIN, Skill.requiresSkills(Skill.requires(DOUBLE_STRAFE, 5))));
        register(new DoubleStrafeSkill(DOUBLE_STRAFE));

        // Thief
        register(new DoubleAttackPassiveSkill(DOUBLE_ATTACK));
        register(new DetoxifySkill(DETOXIFY, Skill.requiresSkills(Skill.requires(ENVENOM, 3))));
        register(new EnvenomSkill(ENVENOM));
        register(new ImproveDodgePassiveSkill(IMPROVE_DODGE));
        register(new StealSkill(STEAL));
        register(new HidingSkill(HIDING, Skill.requiresSkills(Skill.requires(STEAL, 5))));

        // Magician
        register(new ResourceRecoveryPassiveSkill(IRR));
        register(new MagicArrowSkill(MAGIC_ARROW));
        register(new FireboltSkill(FIREBOLT));
        register(new FireballSkill(FIREBALL, Skill.requiresSkills(Skill.requires(FIREBOLT, 4))));
        register(new MeteorShowerSkill(METEOR_SHOWER, Skill.requiresSkills(Skill.requires(FIREBALL, 7))));
        register(new ThunderBoltSkill(THUNDER_BOLT));
        register(new ThunderStormSkill(THUNDER_STORM));
        register(new ThunderSphereSkill(THUNDER_SPHERE));
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

    // ====================
    // UPDATED QUERY METHODS
    // ====================

    /**
     * Get all skills available for a specific class (by PlayerClass object)
     */
    public static List<Skill> getSkillsForClass(PlayerClass playerClass) {
        return SKILLS.values().stream()
                .filter(skill -> skill.isAvailableForClass(playerClass))
                .toList();
    }

    /**
     * Check if a skill exists and is valid
     */
    public static boolean isValidSkill(Identifier skillId) {
        return SKILLS.containsKey(skillId);
    }
}