package com.sypztep.mamy.common.system.difficulty;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.random.Random;

import java.util.*;

/**
 * PERFORMANCE OPTIMIZED system with visual scaling and thematic stat bonuses
 * - Uses Minecraft's Random for compatibility
 * - Pre-shuffled arrays to avoid Collections.shuffle()
 * - Minimal object allocation during mob spawning
 */
public class NameBasedBonusSystem {

    // ==================== PERFORMANCE OPTIMIZATION ====================

    // Pre-shuffled keyword arrays to avoid Collections.shuffle() during spawning
    private static final StatKeyword[][] SHUFFLED_KEYWORDS_BY_RARITY;
    private static final SpecialCombination[] SPECIAL_COMBOS_ARRAY;

    static {
        // Pre-shuffle keywords for each rarity level to avoid runtime shuffling
        SHUFFLED_KEYWORDS_BY_RARITY = new StatKeyword[6][];
        for (int rarity = 0; rarity < 6; rarity++) {
            List<StatKeyword> keywords = new ArrayList<>(Arrays.asList(StatKeyword.values()));
            Collections.shuffle(keywords); // One-time shuffle during class loading
            SHUFFLED_KEYWORDS_BY_RARITY[rarity] = keywords.toArray(new StatKeyword[0]);
        }

        // Convert enum to array for faster access
        SPECIAL_COMBOS_ARRAY = SpecialCombination.values();
    }

    // ==================== UNIFIED RARITY DETECTION ====================

    public static ProgressiveDifficultySystem.MonsterRarity getRarityFromName(LivingEntity entity) {
        if (!entity.hasCustomName()) return ProgressiveDifficultySystem.MonsterRarity.COMMON;

        String name = entity.getCustomName().getString();

        if (name.contains("Void") || name.contains("Avatar") || name.contains("Cosmic")) {
            return ProgressiveDifficultySystem.MonsterRarity.MYTHIC;
        } else if (name.contains("Primordial") || name.contains("Dimensional") || name.contains("Bane")) {
            return ProgressiveDifficultySystem.MonsterRarity.LEGENDARY;
        } else if (name.contains("Nightmare") || name.contains("Abyssal") || name.contains("Eternal")) {
            return ProgressiveDifficultySystem.MonsterRarity.EPIC;
        } else if (name.contains("Bloodthirsty") || name.contains("Corrupted") || name.contains("Fierce")) {
            return ProgressiveDifficultySystem.MonsterRarity.RARE;
        } else if (name.contains("Strong") || name.contains("Angry") || name.contains("Wild")) {
            return ProgressiveDifficultySystem.MonsterRarity.UNCOMMON;
        }

        return ProgressiveDifficultySystem.MonsterRarity.COMMON;
    }

    public static int getRarityLevel(LivingEntity entity) {
        return getRarityFromName(entity).ordinal();
    }

    // ==================== ENHANCED KEYWORD DEFINITIONS WITH SCALING ====================

    public enum StatKeyword {
        // === AGILITY KEYWORDS (Speed & Evasion) ===
        SWIFT("Swift", StatTypes.AGILITY, 3, 8, 1.05f, 1.15f),
        QUICK("Quick", StatTypes.AGILITY, 2, 6, 1.02f, 1.10f),
        FAST("Fast", StatTypes.AGILITY, 2, 5, 1.03f, 1.12f),
        SPEEDY("Speedy", StatTypes.AGILITY, 4, 10, 1.08f, 1.20f),
        EVASIVE("Evasive", StatTypes.AGILITY, 5, 12, 1.06f, 1.18f),
        NIMBLE("Nimble", StatTypes.AGILITY, 3, 7, 1.04f, 1.15f),

        // === DEXTERITY KEYWORDS (Accuracy & Precision) ===
        ACCURATE("Accurate", StatTypes.DEXTERITY, 4, 9, 1.03f, 1.12f),
        PRECISE("Precise", StatTypes.DEXTERITY, 3, 7, 1.02f, 1.10f),
        SHARP("Sharp", StatTypes.DEXTERITY, 2, 5, 1.04f, 1.15f),
        MARKSMAN("Marksman", StatTypes.DEXTERITY, 6, 15, 1.06f, 1.18f),
        SNIPER("Sniper", StatTypes.DEXTERITY, 5, 12, 1.05f, 1.16f),

        // === STRENGTH KEYWORDS (Physical Power) - BIGGER SCALING ===
        STRONG("Strong", StatTypes.STRENGTH, 4, 10, 1.10f, 1.25f),
        MIGHTY("Mighty", StatTypes.STRENGTH, 5, 12, 1.15f, 1.35f),
        BRUTAL("Brutal", StatTypes.STRENGTH, 6, 15, 1.12f, 1.30f),
        SAVAGE("Savage", StatTypes.STRENGTH, 7, 18, 1.18f, 1.40f),
        CRUSHER("Crusher", StatTypes.STRENGTH, 5, 13, 1.20f, 1.45f),
        BERSERKER("Berserker", StatTypes.STRENGTH, 8, 20, 1.16f, 1.38f),
        GIANT("Giant", StatTypes.STRENGTH, 10, 25, 1.30f, 1.60f),
        COLOSSAL("Colossal", StatTypes.STRENGTH, 12, 30, 1.40f, 1.80f),

        // === INTELLIGENCE KEYWORDS (Magical Power) ===
        MYSTIC("Mystic", StatTypes.INTELLIGENCE, 4, 10, 1.06f, 1.18f),
        ARCANE("Arcane", StatTypes.INTELLIGENCE, 5, 12, 1.08f, 1.22f),
        MAGICAL("Magical", StatTypes.INTELLIGENCE, 3, 8, 1.05f, 1.15f),
        SORCEROUS("Sorcerous", StatTypes.INTELLIGENCE, 6, 15, 1.10f, 1.25f),
        ENCHANTED("Enchanted", StatTypes.INTELLIGENCE, 4, 9, 1.07f, 1.20f),
        SPELLBOUND("Spellbound", StatTypes.INTELLIGENCE, 7, 17, 1.12f, 1.28f),

        // === VITALITY KEYWORDS (Health & Defense) - BIGGER SCALING ===
        TOUGH("Tough", StatTypes.VITALITY, 4, 10, 1.08f, 1.20f),
        HARDY("Hardy", StatTypes.VITALITY, 3, 8, 1.06f, 1.18f),
        RESILIENT("Resilient", StatTypes.VITALITY, 5, 12, 1.10f, 1.25f),
        ARMORED("Armored", StatTypes.VITALITY, 6, 14, 1.12f, 1.30f),
        FORTIFIED("Fortified", StatTypes.VITALITY, 7, 16, 1.15f, 1.35f),
        IRONHIDE("Ironhide", StatTypes.VITALITY, 8, 20, 1.18f, 1.40f),
        TITAN("Titan", StatTypes.VITALITY, 10, 25, 1.25f, 1.55f),

        // === LUCK KEYWORDS (Critical & Fortune) ===
        LUCKY("Lucky", StatTypes.LUCK, 3, 7, 1.02f, 1.08f),
        FORTUNATE("Fortunate", StatTypes.LUCK, 4, 9, 1.03f, 1.10f),
        BLESSED("Blessed", StatTypes.LUCK, 5, 12, 1.05f, 1.15f),
        CRITICAL("Critical", StatTypes.LUCK, 6, 15, 1.04f, 1.12f),
        FATED("Fated", StatTypes.LUCK, 7, 17, 1.06f, 1.18f),
        DESTINED("Destined", StatTypes.LUCK, 8, 20, 1.08f, 1.20f);

        public final String keyword;
        public final StatTypes statType;
        public final int minBonus;
        public final int maxBonus;
        public final float minScale;
        public final float maxScale;

        StatKeyword(String keyword, StatTypes statType, int minBonus, int maxBonus, float minScale, float maxScale) {
            this.keyword = keyword;
            this.statType = statType;
            this.minBonus = minBonus;
            this.maxBonus = maxBonus;
            this.minScale = minScale;
            this.maxScale = maxScale;
        }

        public int getRandomBonus(Random random) {
            return minBonus + random.nextInt(maxBonus - minBonus + 1);
        }

        public float getRandomScale(Random random) {
            return minScale + random.nextFloat() * (maxScale - minScale);
        }

        public boolean hasVisualEffect() {
            return maxScale > 1.05f;
        }
    }

    // ==================== ENHANCED SPECIAL COMBINATIONS WITH SCALING ====================

    public enum SpecialCombination {
        SPEED_DEMON("Speed Demon",
                Map.of(StatTypes.AGILITY, 15, StatTypes.STRENGTH, 8), 1.08f, 1.22f),
        BATTLE_MAGE("Battle Mage",
                Map.of(StatTypes.INTELLIGENCE, 12, StatTypes.STRENGTH, 8), 1.10f, 1.25f),
        TANK_KILLER("Tank Killer",
                Map.of(StatTypes.STRENGTH, 18, StatTypes.DEXTERITY, 6), 1.15f, 1.35f),
        SHADOW_ASSASSIN("Shadow Assassin",
                Map.of(StatTypes.AGILITY, 20, StatTypes.LUCK, 10), 1.05f, 1.15f),
        ARCANE_WARRIOR("Arcane Warrior",
                Map.of(StatTypes.INTELLIGENCE, 15, StatTypes.STRENGTH, 10), 1.12f, 1.30f),
        FORTRESS_GUARDIAN("Fortress Guardian",
                Map.of(StatTypes.VITALITY, 25, StatTypes.STRENGTH, 5), 1.25f, 1.55f),
        LUCKY_STRIKER("Lucky Striker",
                Map.of(StatTypes.LUCK, 20, StatTypes.DEXTERITY, 8), 1.06f, 1.18f),
        SWIFT_DESTROYER("Swift Destroyer",
                Map.of(StatTypes.AGILITY, 12, StatTypes.STRENGTH, 15), 1.14f, 1.32f),
        TITAN_CRUSHER("Titan Crusher",
                Map.of(StatTypes.STRENGTH, 30, StatTypes.VITALITY, 20), 1.35f, 1.70f),
        VOID_COLOSSUS("Void Colossus",
                Map.of(StatTypes.STRENGTH, 25, StatTypes.VITALITY, 25, StatTypes.INTELLIGENCE, 15), 1.40f, 1.80f);

        public final String namePattern;
        public final Map<StatTypes, Integer> statBonuses;
        public final float minScale;
        public final float maxScale;

        SpecialCombination(String namePattern, Map<StatTypes, Integer> statBonuses, float minScale, float maxScale) {
            this.namePattern = namePattern;
            this.statBonuses = statBonuses;
            this.minScale = minScale;
            this.maxScale = maxScale;
        }

        public float getRandomScale(Random random) {
            return minScale + random.nextFloat() * (maxScale - minScale);
        }
    }

    // ==================== PERFORMANCE OPTIMIZED MAIN APPLICATION ====================

    /**
     * OPTIMIZED application with minimal object allocation
     */
    public static void applyNameBasedBonuses(LivingEntity entity) {
        if (!entity.hasCustomName()) return;

        String fullName = entity.getCustomName().getString();
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(entity);
        if (levelComponent == null) return;

        // Use primitive arrays to avoid HashMap allocation
        int[] statBonuses = new int[StatTypes.values().length];
        boolean hasAnyBonus = false;
        float finalScale = 1.0f;
        boolean hasVisualEffect = false;

        // Check special combinations first (no allocation needed)
        for (SpecialCombination combo : SPECIAL_COMBOS_ARRAY) {
            if (fullName.contains(combo.namePattern)) {
                for (Map.Entry<StatTypes, Integer> entry : combo.statBonuses.entrySet()) {
                    statBonuses[entry.getKey().ordinal()] += entry.getValue();
                    hasAnyBonus = true;
                }

                finalScale = combo.getRandomScale(entity.getRandom());
                hasVisualEffect = true;
                break; // Only one special combination per monster
            }
        }

        // Check individual keywords (use entity's random directly)
        Random random = entity.getRandom();
        float largestKeywordScale = 1.0f;

        for (StatKeyword keyword : StatKeyword.values()) {
            if (fullName.contains(keyword.keyword)) {
                int bonus = keyword.getRandomBonus(random);
                statBonuses[keyword.statType.ordinal()] += bonus;
                hasAnyBonus = true;

                if (keyword.hasVisualEffect()) {
                    float keywordScale = keyword.getRandomScale(random);
                    largestKeywordScale = Math.max(largestKeywordScale, keywordScale);
                    hasVisualEffect = true;
                }
            }
        }

        // Use the larger scale (special combo or largest keyword)
        if (finalScale == 1.0f) {
            finalScale = largestKeywordScale;
        }

        // Apply stat bonuses if any exist
        if (hasAnyBonus) {
            StatTypes[] statTypesArray = StatTypes.values();
            for (int i = 0; i < statBonuses.length; i++) {
                if (statBonuses[i] > 0) {
                    var stat = levelComponent.getStatByType(statTypesArray[i]);
                    if (stat != null) {
                        int currentValue = stat.getCurrentValue();
                        int newValue = currentValue + statBonuses[i];
                        stat.setPoints((short) Math.min(newValue, Short.MAX_VALUE));
                    }
                }
            }

            levelComponent.refreshAllStatEffectsInternal();
        }

        // Apply visual scaling effect
        if (hasVisualEffect && finalScale > 1.0f) applyVisualScaling(entity, finalScale);

        // Refresh health after all changes
        entity.setHealth(entity.getMaxHealth());
    }

    /**
     * OPTIMIZED visual scaling application
     */
    private static void applyVisualScaling(LivingEntity entity, float scale) {
        try {
            EntityAttributeInstance scaleAttribute = entity.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
            if (scaleAttribute != null) scaleAttribute.setBaseValue(scale);
        } catch (Exception ignored) {
            // Donothing if can't modify
            Mamy.LOGGER.error("Couldn't apply visual scaling [Not a big deal..]");
        }
    }

    // ==================== PERFORMANCE OPTIMIZED NAME GENERATION ====================

    /**
     * OPTIMIZED name generation using pre-shuffled arrays
     */
    public static String generateEnhancedName(String baseName,
                                              ProgressiveDifficultySystem.MonsterRarity rarity,
                                              Random random) {

        float specialChance = switch (rarity) {
            case COMMON -> 0.05f;
            case UNCOMMON -> 0.15f;
            case RARE -> 0.35f;
            case EPIC -> 0.60f;
            case LEGENDARY -> 0.80f;
            case MYTHIC -> 0.95f;
        };

        if (random.nextFloat() < specialChance) {
            // Special combo chance
            if (random.nextFloat() < 0.3f && rarity.ordinal() >= 2) {
                SpecialCombination combo = SPECIAL_COMBOS_ARRAY[random.nextInt(SPECIAL_COMBOS_ARRAY.length)];
                return combo.namePattern + " " + baseName;
            } else {
                // Use pre-shuffled keywords for this rarity
                StatKeyword[] availableKeywords = SHUFFLED_KEYWORDS_BY_RARITY[rarity.ordinal()];

                // Add 1-3 keyword modifiers
                int numKeywords = 1 + random.nextInt(Math.min(3, rarity.ordinal() + 1));
                StringBuilder nameBuilder = new StringBuilder();

                for (int i = 0; i < numKeywords && i < availableKeywords.length; i++) {
                    if (!nameBuilder.isEmpty()) nameBuilder.append(" ");
                    nameBuilder.append(availableKeywords[i].keyword);
                }

                nameBuilder.append(" ").append(baseName);
                return nameBuilder.toString();
            }
        }

        return baseName;
    }

    // ==================== UTILITY METHODS ====================

    public static boolean hasNameBonuses(String fullName) {
        // Check special combinations
        for (SpecialCombination specialCombination : SPECIAL_COMBOS_ARRAY) {
            if (fullName.contains(specialCombination.namePattern)) return true;
        }

        // Check keywords
        for (StatKeyword keyword : StatKeyword.values()) {
            if (fullName.contains(keyword.keyword)) return true;
        }

        return false;
    }

    public static boolean hasVisualEffects(String fullName) {
        // Check special combinations (all have visual effects)
        for (SpecialCombination specialCombination : SPECIAL_COMBOS_ARRAY) {
            if (fullName.contains(specialCombination.namePattern)) return true;
        }

        // Check keywords with visual effects
        for (StatKeyword keyword : StatKeyword.values()) {
            if (keyword.hasVisualEffect() && fullName.contains(keyword.keyword)) {
                return true;
            }
        }

        return false;
    }

    public static float getEstimatedScale(String fullName) {
        for (SpecialCombination combo : SPECIAL_COMBOS_ARRAY) {
            if (fullName.contains(combo.namePattern)) {
                return (combo.minScale + combo.maxScale) / 2.0f;
            }
        }

        // Check keywords
        float maxScale = 1.0f;
        for (StatKeyword keyword : StatKeyword.values()) {
            if (keyword.hasVisualEffect() && fullName.contains(keyword.keyword)) {
                float avgScale = (keyword.minScale + keyword.maxScale) / 2.0f;
                maxScale = Math.max(maxScale, avgScale);
            }
        }

        return maxScale;
    }
}