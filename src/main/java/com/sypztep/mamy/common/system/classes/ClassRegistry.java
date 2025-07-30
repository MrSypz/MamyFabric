package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Formatting;

import java.util.*;

public class ClassRegistry {
    private static final Map<String, PlayerClass> CLASSES = new HashMap<>();

    // ===== TIER 0 (Starting) =====
    public static final PlayerClass NOVICE = register(new PlayerClass(
            "novice", 0, 1, "Novice", Formatting.GRAY,
            Map.of(), // No bonuses
            ResourceType.MANA, 100f,
            "A beginning adventurer with no specialization"
    ));

    // ===== TIER 1 (First Job) =====
    public static final PlayerClass SWORDMAN = register(new PlayerClass(
            "swordman", 1, 1, "Swordman", Formatting.RED,
            Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 20.0,
                    ModEntityAttributes.MELEE_ATTACK_DAMAGE, 0.15
            ),
            ResourceType.RAGE, 300f,
            "A warrior who has chosen the path of the sword"
    ).addRequirement(NOVICE, 10));

    public static final PlayerClass MAGE = register(new PlayerClass(
            "mage", 1, 2, "Mage", Formatting.BLUE,
            Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, -5.0,
                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, 0.25
            ),
            ResourceType.MANA, 400f,
            "A spellcaster who manipulates arcane energies"
    ).addRequirement(NOVICE, 10));

    public static final PlayerClass ARCHER = register(new PlayerClass(
            "archer", 1, 3, "Archer", Formatting.GREEN,
            Map.of(
                    EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.02,
                    ModEntityAttributes.CRIT_CHANCE, 0.10
            ),
            ResourceType.MANA, 250f,
            "A ranged combatant skilled with bow and arrow"
    ).addRequirement(NOVICE, 10));

    // ===== TIER 2 (Second Job - Swordman Path) =====
    public static final PlayerClass KNIGHT = register(new PlayerClass(
            "knight", 2, 1, "Knight", Formatting.GOLD,
            Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 40.0,
                    EntityAttributes.GENERIC_ARMOR, 4.0,
                    ModEntityAttributes.MELEE_ATTACK_DAMAGE, 0.20
            ),
            ResourceType.RAGE, 450f,
            "A noble warrior sworn to protect others"
    ).addRequirement(SWORDMAN, 25));

    public static final PlayerClass CRUSADER = register(new PlayerClass(
            "crusader", 2, 2, "Crusader", Formatting.YELLOW,
            Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 30.0,
                    ModEntityAttributes.MELEE_ATTACK_DAMAGE, 0.15,
                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, 0.15 // Hybrid magic/melee
            ),
            ResourceType.MANA, 400f,
            "A holy warrior who blends sword and divine magic"
    ).addRequirement(SWORDMAN, 25));

    // ===== TIER 2 (Second Job - Mage Path) =====
    public static final PlayerClass WIZARD = register(new PlayerClass(
            "wizard", 2, 3, "Wizard", Formatting.DARK_BLUE,
            Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, -5.0,
                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, 0.40
//                    ModEntityAttributes.MANA_COST_REDUCTION, 0.15
            ),
            ResourceType.MANA, 600f,
            "A master of elemental magic and arcane knowledge"
    ).addRequirement(MAGE, 25));

    public static final PlayerClass WARLOCK = register(new PlayerClass(
            "warlock", 2, 4, "Warlock", Formatting.DARK_PURPLE,
            Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 10.0,
                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, 0.30
//                    ModEntityAttributes.LIFE_STEAL, 0.10 // Dark magic life steal
            ),
            ResourceType.MANA, 500f,
            "A dark sorcerer who draws power from forbidden magic"
    ).addRequirement(MAGE, 25));

    // ===== TIER 2 (Second Job - Archer Path) =====
    public static final PlayerClass HUNTER = register(new PlayerClass(
            "hunter", 2, 5, "Hunter", Formatting.DARK_GREEN,
            Map.of(
                    EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.04,
                    ModEntityAttributes.CRIT_CHANCE, 0.20,
                    ModEntityAttributes.CRIT_DAMAGE, 0.25
            ),
            ResourceType.RAGE, 350f,
            "A master tracker skilled in hunting beasts"
    ).addRequirement(ARCHER, 25));

    public static final PlayerClass ASSASSIN = register(new PlayerClass(
            "assassin", 2, 6, "Assassin", Formatting.BLACK,
            Map.of(
                    EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.05,
                    ModEntityAttributes.CRIT_CHANCE, 0.25
//                    ModEntityAttributes.STEALTH_DURATION, 2.0 // Custom stealth attribute
            ),
            ResourceType.RAGE, 300f,
            "A shadow warrior who strikes from darkness"
    ).addRequirement(ARCHER, 25));

    // ===== TIER 3 (Advanced Jobs - Examples) =====
    public static final PlayerClass PALADIN = register(new PlayerClass(
            "paladin", 3, 1, "Paladin", Formatting.WHITE,
            Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 60.0,
                    EntityAttributes.GENERIC_ARMOR, 6.0,
                    ModEntityAttributes.MELEE_ATTACK_DAMAGE, 0.25,
                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, 0.20
//                    ModEntityAttributes.HEALING_POWER, 0.30
            ),
            ResourceType.MANA, 500f,
            "A legendary holy knight with divine powers"
    ).addRequirement(KNIGHT, 45).addRequirement(CRUSADER, 45)); // Can evolve from either!

    private static PlayerClass register(PlayerClass playerClass) {
        CLASSES.put(playerClass.getId(), playerClass);
        return playerClass;
    }

    // === Registry Methods ===
    public static PlayerClass getClass(String id) {
        return CLASSES.get(id);
    }

    public static Collection<PlayerClass> getAllClasses() {
        return CLASSES.values();
    }

    public static List<PlayerClass> getClassesByTier(int tier) {
        return CLASSES.values().stream()
                .filter(clazz -> clazz.getTier() == tier)
                .sorted(Comparator.comparingInt(PlayerClass::getBranch))
                .toList();
    }

    public static List<PlayerClass> getAvailableEvolutions(PlayerClass currentClass, int classLevel) {
        return CLASSES.values().stream()
                .filter(clazz -> clazz.canEvolveFrom(currentClass, classLevel))
                .filter(clazz -> clazz != currentClass) // Don't include current class
                .toList();
    }

    public static PlayerClass getDefaultClass() {
        return NOVICE;
    }

    /**
     * Get the class progression tree as a formatted string (for debugging)
     */
    public static String getClassTree() {
        StringBuilder sb = new StringBuilder();
        for (int tier = 0; tier <= 3; tier++) {
            sb.append("=== TIER ").append(tier).append(" ===\n");
            for (PlayerClass clazz : getClassesByTier(tier)) {
                sb.append(clazz.getClassCode()).append(" ").append(clazz.getDisplayName());
                if (!clazz.getRequirements().isEmpty()) {
                    sb.append(" (Requires: ");
                    for (var req : clazz.getRequirements()) {
                        sb.append(req.previousClass().getDisplayName()).append(" Lv.").append(req.requiredLevel()).append(" ");
                    }
                    sb.append(")");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}