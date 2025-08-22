package com.sypztep.mamy.common.system.difficulty;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.random.Random;

import java.util.*;

/**
 * System that analyzes monster names and applies additional stat bonuses
 * based on keywords in their names (prefix + suffix combinations)
 */
public class NameBasedBonusSystem {

    // ==================== KEYWORD DEFINITIONS ====================

    public enum StatKeyword {
        // AGI Keywords (Evasion & Speed)
        SWIFT("Swift", StatTypes.AGILITY, 3, 8),
        QUICK("Quick", StatTypes.AGILITY, 2, 6),
        FAST("Fast", StatTypes.AGILITY, 2, 5),
        SPEEDY("Speedy", StatTypes.AGILITY, 4, 10),
        EVASIVE("Evasive", StatTypes.AGILITY, 5, 12),
        NIMBLE("Nimble", StatTypes.AGILITY, 3, 7),

        // DEX Keywords (Accuracy & Ranged)
        ACCURATE("Accurate", StatTypes.DEXTERITY, 4, 9),
        PRECISE("Precise", StatTypes.DEXTERITY, 3, 7),
        SHARP("Sharp", StatTypes.DEXTERITY, 2, 5),
        MARKSMAN("Marksman", StatTypes.DEXTERITY, 6, 15),
        SNIPER("Sniper", StatTypes.DEXTERITY, 5, 12),

        // STR Keywords (Physical Damage)
        STRONG("Strong", StatTypes.STRENGTH, 4, 10),
        MIGHTY("Mighty", StatTypes.STRENGTH, 5, 12),
        BRUTAL("Brutal", StatTypes.STRENGTH, 6, 15),
        SAVAGE("Savage", StatTypes.STRENGTH, 7, 18),
        CRUSHER("Crusher", StatTypes.STRENGTH, 5, 13),
        BERSERKER("Berserker", StatTypes.STRENGTH, 8, 20),

        // INT Keywords (Magic Damage)
        MYSTIC("Mystic", StatTypes.INTELLIGENCE, 4, 10),
        ARCANE("Arcane", StatTypes.INTELLIGENCE, 5, 12),
        MAGICAL("Magical", StatTypes.INTELLIGENCE, 3, 8),
        SORCEROUS("Sorcerous", StatTypes.INTELLIGENCE, 6, 15),
        ENCHANTED("Enchanted", StatTypes.INTELLIGENCE, 4, 9),
        SPELLBOUND("Spellbound", StatTypes.INTELLIGENCE, 7, 17),

        // VIT Keywords (Health & Resistance)
        TOUGH("Tough", StatTypes.VITALITY, 14, 20),
        HARDY("Hardy", StatTypes.VITALITY, 13, 28),
        RESILIENT("Resilient", StatTypes.VITALITY, 15, 22),
        ARMORED("Armored", StatTypes.VITALITY, 16, 24),
        FORTIFIED("Fortified", StatTypes.VITALITY, 17, 26),
        IRONHIDE("Ironhide", StatTypes.VITALITY, 18, 20),

        // LUK Keywords (Critical & Fortune)
        LUCKY("Lucky", StatTypes.LUCK, 3, 7),
        FORTUNATE("Fortunate", StatTypes.LUCK, 4, 9),
        BLESSED("Blessed", StatTypes.LUCK, 5, 12),
        CRITICAL("Critical", StatTypes.LUCK, 6, 15),
        FATED("Fated", StatTypes.LUCK, 7, 17),
        DESTINED("Destined", StatTypes.LUCK, 8, 20);

        public final String keyword;
        public final StatTypes statType;
        public final int minBonus;
        public final int maxBonus;

        StatKeyword(String keyword, StatTypes statType, int minBonus, int maxBonus) {
            this.keyword = keyword;
            this.statType = statType;
            this.minBonus = minBonus;
            this.maxBonus = maxBonus;
        }

        public int getRandomBonus(Random random) {
            return minBonus + random.nextInt(maxBonus - minBonus + 1);
        }
    }

    // ==================== SPECIAL NAME COMBINATIONS ====================

    public enum SpecialCombination {
        SPEED_DEMON("Speed Demon",
                Map.of(StatTypes.AGILITY, 15, StatTypes.STRENGTH, 8)),
        BATTLE_MAGE("Battle Mage",
                Map.of(StatTypes.INTELLIGENCE, 12, StatTypes.STRENGTH, 8)),
        TANK_KILLER("Tank Killer",
                Map.of(StatTypes.STRENGTH, 18, StatTypes.DEXTERITY, 6)),
        SHADOW_ASSASSIN("Shadow Assassin",
                Map.of(StatTypes.AGILITY, 20, StatTypes.LUCK, 10)),
        ARCANE_WARRIOR("Arcane Warrior",
                Map.of(StatTypes.INTELLIGENCE, 15, StatTypes.STRENGTH, 10)),
        FORTRESS_GUARDIAN("Fortress Guardian",
                Map.of(StatTypes.VITALITY, 25, StatTypes.STRENGTH, 5)),
        LUCKY_STRIKER("Lucky Striker",
                Map.of(StatTypes.LUCK, 20, StatTypes.DEXTERITY, 8)),
        SWIFT_DESTROYER("Swift Destroyer",
                Map.of(StatTypes.AGILITY, 12, StatTypes.STRENGTH, 15));

        public final String namePattern;
        public final Map<StatTypes, Integer> statBonuses;

        SpecialCombination(String namePattern, Map<StatTypes, Integer> statBonuses) {
            this.namePattern = namePattern;
            this.statBonuses = statBonuses;
        }
    }

    // ==================== MAIN BONUS APPLICATION ====================

    /**
     * Analyze monster name and apply additional stat bonuses
     * This runs AFTER the base stats and rarity multipliers
     */
    public static void applyNameBasedBonuses(LivingEntity entity) {
        if (!entity.hasCustomName()) return;

        String fullName = Objects.requireNonNull(entity.getCustomName()).getString();
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(entity);
        if (levelComponent == null) return;

        Map<StatTypes, Integer> totalBonuses = new HashMap<>();
//        List<String> appliedBonuses = new ArrayList<>();

        // Check for special combinations first (higher priority)
        for (SpecialCombination combo : SpecialCombination.values()) {
            if (fullName.contains(combo.namePattern)) {
                combo.statBonuses.forEach((stat, bonus) -> {
                    totalBonuses.merge(stat, bonus, Integer::sum);
                });
//                appliedBonuses.add(combo.namePattern + " (" + combo.statBonuses.size() + " stats)");
                break; // Only one special combination per monster
            }
        }

        // Check for individual keywords
        Random random = entity.getRandom();
        for (StatKeyword keyword : StatKeyword.values()) {
            if (fullName.contains(keyword.keyword)) {
                int bonus = keyword.getRandomBonus(random);
                totalBonuses.merge(keyword.statType, bonus, Integer::sum);
//                appliedBonuses.add(keyword.keyword + " (+" + bonus + " " + keyword.statType.name() + ")");
            }
        }

        // Apply all bonuses
        if (!totalBonuses.isEmpty()) {
            for (Map.Entry<StatTypes, Integer> entry : totalBonuses.entrySet()) {
                var stat = levelComponent.getStatByType(entry.getKey());
                if (stat != null) {
                    int currentValue = stat.getCurrentValue();
                    int newValue = currentValue + entry.getValue();
                    stat.setPoints((short) Math.min(newValue, Short.MAX_VALUE));
                }
            }

            levelComponent.refreshAllStatEffectsInternal();
            entity.setHealth(entity.getMaxHealth());

            // Debug log
//            System.out.printf("[NameBonus] %s applied bonuses: %s\n",
//                    entity.getType().getName().getString(), String.join(", ", appliedBonuses));
        }
    }

    // ==================== ENHANCED NAME GENERATION ====================

    /**
     * Generate enhanced names with stat-related keywords
     * This can be called during monster variant generation
     */
    public static String generateEnhancedName(String baseName,
                                              ProgressiveDifficultySystem.MonsterRarity rarity,
                                              Random random) {

        // Higher rarity = more likely to get special names
        float specialChance = switch (rarity) {
            case COMMON -> 0.05f;      // 5% chance
            case UNCOMMON -> 0.15f;    // 15% chance
            case RARE -> 0.30f;        // 30% chance
            case EPIC -> 0.50f;        // 50% chance
            case LEGENDARY -> 0.70f;   // 70% chance
            case MYTHIC -> 0.90f;      // 90% chance
        };

        if (random.nextFloat() < specialChance) {
            // Choose enhancement type
            if (random.nextFloat() < 0.2f && rarity.ordinal() >= 2) { // 20% for special combos (Rare+)
                SpecialCombination combo = SpecialCombination.values()[
                        random.nextInt(SpecialCombination.values().length)];
                return combo.namePattern + " " + baseName;
            } else {
                // Add 1-3 keyword modifiers
                int numKeywords = 1 + random.nextInt(Math.min(3, rarity.ordinal() + 1));
                List<StatKeyword> availableKeywords = new ArrayList<>(Arrays.asList(StatKeyword.values()));
                Collections.shuffle(availableKeywords, new java.util.Random());

                StringBuilder nameBuilder = new StringBuilder();

                // Add prefix keywords
                for (int i = 0; i < numKeywords && i < availableKeywords.size(); i++) {
                    if (!nameBuilder.isEmpty()) nameBuilder.append(" ");
                    nameBuilder.append(availableKeywords.get(i).keyword);
                }

                nameBuilder.append(" ").append(baseName);
                return nameBuilder.toString();
            }
        }

        return baseName; // No enhancement
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Get total bonus preview for a name (for WTHIT tooltips)
     */
    public static Map<StatTypes, Integer> previewNameBonuses(String fullName) {
        Map<StatTypes, Integer> bonuses = new HashMap<>();

        // Check special combinations
        for (SpecialCombination combo : SpecialCombination.values()) {
            if (fullName.contains(combo.namePattern)) {
                combo.statBonuses.forEach((stat, bonus) -> bonuses.merge(stat, bonus, Integer::sum));
                return bonuses; // Only one special combination
            }
        }

        // Check keywords
        for (StatKeyword keyword : StatKeyword.values()) {
            if (fullName.contains(keyword.keyword)) {
                int avgBonus = (keyword.minBonus + keyword.maxBonus) / 2;
                bonuses.merge(keyword.statType, avgBonus, Integer::sum);
            }
        }

        return bonuses;
    }

    /**
     * Check if a name has any special bonuses
     */
    public static boolean hasNameBonuses(String fullName) {
        // Check special combinations
        for (SpecialCombination combo : SpecialCombination.values()) {
            if (fullName.contains(combo.namePattern)) return true;
        }

        // Check keywords
        for (StatKeyword keyword : StatKeyword.values()) {
            if (fullName.contains(keyword.keyword)) return true;
        }

        return false;
    }
}