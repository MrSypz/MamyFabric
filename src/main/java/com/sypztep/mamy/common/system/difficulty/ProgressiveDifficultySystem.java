package com.sypztep.mamy.common.system.difficulty;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModTags;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

/**
 * ENHANCED Korean MMORPG Progressive Difficulty System
 * - Integrates local difficulty (0-6) for progressive spawn rates
 * - Dynamic level scaling over time (no cap)
 * - Biome stat multipliers during spawn
 * - Armor scaling by tier
 * - Unified rarity detection (no redundant code)
 */
public class ProgressiveDifficultySystem {

    // ==================== PROGRESSION SCALING ====================

    private static final float PROGRESSION_BASE = 1.0f;
    private static final float PROGRESSION_SCALING = 0.08f;
    private static final int MAX_PROGRESSION_BONUS = 15;

    // ==================== ENVIRONMENTAL FACTORS ====================

    private static final float DAY_MULTIPLIER = 1.0f;
    private static final float NIGHT_MULTIPLIER = 2.0f;
    private static final float STORM_MULTIPLIER = 1.6f;
    private static final float UNDERGROUND_MULTIPLIER = 1.3f;
    private static final float DEEP_UNDERGROUND_MULTIPLIER = 2.2f;

    // ==================== BIOME STAT MULTIPLIERS (NEW!) ====================
    // These multiply monster stats during spawn, not just damage

    private static final float PEACEFUL_STAT_MULT = 1.0f;    // Plains - vanilla gameplay
    private static final float MODERATE_STAT_MULT = 1.2f;    // Forest - slight boost
    private static final float HARSH_STAT_MULT = 1.5f;       // Mountains - noticeable boost
    private static final float EXTREME_STAT_MULT = 2.0f;     // Desert - serious boost
    private static final float HELLISH_STAT_MULT = 3.0f;     // Badlands - hellish boost

    // ==================== LOCAL DIFFICULTY INTEGRATION ====================
    // Minecraft local difficulty goes from 0 to 6+ over time

    private static final float[] LOCAL_DIFFICULTY_SPAWN_MULTIPLIERS = {
            1.0f,   // 0 - Fresh world
            1.2f,   // 1 - Early game
            1.5f,   // 2 - Progressing
            2.0f,   // 3 - Mid game
            3.0f,   // 4 - Late game
            4.0f,   // 5 - End game
            6.0f    // 6+ - Hellish mode
    };

    // ==================== MONSTER RARITY SYSTEM ====================

    public enum MonsterRarity {
        COMMON(0.99f, 1.0f, 1.0f, ""),
        UNCOMMON(0.008f, 1.5f, 2.0f, "Strong"),
        RARE(0.0015f, 3.0f, 5.0f, "Fierce"),
        EPIC(0.0004f, 6.0f, 10.0f, "Savage"),
        LEGENDARY(0.0001f, 10.0f, 15.0f, "Apex"),
        MYTHIC(0.000001f, 15.0f, 25.0f, "Godlike");

        public final float baseSpawnChance;
        public final float statMultiplier;
        public final float expMultiplier;
        public final String defaultPrefix;

        MonsterRarity(float baseSpawnChance, float statMultiplier, float expMultiplier, String defaultPrefix) {
            this.baseSpawnChance = baseSpawnChance;
            this.statMultiplier = statMultiplier;
            this.expMultiplier = expMultiplier;
            this.defaultPrefix = defaultPrefix;
        }

        /**
         * Get spawn chance modified by local difficulty
         */
        public float getAdjustedSpawnChance(float localDifficultyMultiplier) {
            if (this == COMMON) return baseSpawnChance; // Common always common

            // Rare monsters become more likely with higher local difficulty
            float adjustedChance = baseSpawnChance * localDifficultyMultiplier;

            // Cap increases to prevent too many rare spawns
            return Math.min(adjustedChance, baseSpawnChance * 10.0f);
        }
    }

    // ==================== PREFIX SYSTEM ====================

    public enum MonsterPrefix {
        NONE("", 1.0f, 1.0f),
        ANGRY("Angry", 1.1f, 1.1f),
        WILD("Wild", 1.15f, 1.15f),
        BLOODTHIRSTY("Bloodthirsty", 1.5f, 1.8f),
        CORRUPTED("Corrupted", 1.8f, 2.2f),
        NIGHTMARE("Nightmare", 2.5f, 3.5f),
        ABYSSAL("Abyssal", 3.5f, 5.0f),
        PRIMORDIAL("Primordial", 5.0f, 7.5f),
        DIMENSIONAL("Dimensional", 8.0f, 12.0f),
        COSMIC("Cosmic", 12.0f, 18.0f),
        VOID("Void", 20.0f, 30.0f);

        public final String name;
        public final float statMultiplier;
        public final float expMultiplier;

        MonsterPrefix(String name, float statMultiplier, float expMultiplier) {
            this.name = name;
            this.statMultiplier = statMultiplier;
            this.expMultiplier = expMultiplier;
        }

        public static MonsterPrefix getRandom(Random random, MonsterRarity rarity) {
            return switch (rarity) {
                case COMMON -> random.nextFloat() < 0.7f ? NONE : random.nextBoolean() ? ANGRY : WILD;
                case UNCOMMON -> random.nextFloat() < 0.3f ? NONE : random.nextBoolean() ? ANGRY : WILD;
                case RARE -> random.nextFloat() < 0.6f ? (random.nextBoolean() ? BLOODTHIRSTY : CORRUPTED) : WILD;
                case EPIC -> random.nextFloat() < 0.8f ? (random.nextBoolean() ? NIGHTMARE : ABYSSAL) : CORRUPTED;
                case LEGENDARY -> random.nextFloat() < 0.9f ? (random.nextBoolean() ? PRIMORDIAL : DIMENSIONAL) : NIGHTMARE;
                case MYTHIC -> random.nextFloat() < 0.4f ? VOID : random.nextFloat() < 0.7f ? COSMIC : DIMENSIONAL;
            };
        }
    }

    // ==================== SUFFIX SYSTEM ====================

    public enum MonsterSuffix {
        NONE("", 1.0f, 1.0f),
        OF_STRENGTH("of Strength", 1.2f, 1.1f),
        OF_SPEED("of Speed", 1.15f, 1.15f),
        THE_HUNTER("the Hunter", 1.8f, 2.0f),
        THE_DESTROYER("the Destroyer", 2.2f, 2.8f),
        THE_ETERNAL("the Eternal", 3.0f, 4.0f),
        THE_IMMORTAL("the Immortal", 4.5f, 6.0f),
        BANE_OF_WORLDS("Bane of Worlds", 8.0f, 12.0f),
        HERALD_OF_DOOM("Herald of Doom", 15.0f, 20.0f),
        AVATAR_OF_CHAOS("Avatar of Chaos", 25.0f, 40.0f);

        public final String name;
        public final float statMultiplier;
        public final float expMultiplier;

        MonsterSuffix(String name, float statMultiplier, float expMultiplier) {
            this.name = name;
            this.statMultiplier = statMultiplier;
            this.expMultiplier = expMultiplier;
        }

        public static MonsterSuffix getRandom(Random random, MonsterRarity rarity) {
            return switch (rarity) {
                case COMMON -> random.nextFloat() < 0.8f ? NONE : random.nextBoolean() ? OF_STRENGTH : OF_SPEED;
                case UNCOMMON -> random.nextFloat() < 0.5f ? NONE : random.nextFloat() < 0.7f ? OF_STRENGTH : OF_SPEED;
                case RARE -> random.nextFloat() < 0.7f ? (random.nextBoolean() ? THE_HUNTER : THE_DESTROYER) : OF_STRENGTH;
                case EPIC -> random.nextFloat() < 0.8f ? (random.nextBoolean() ? THE_ETERNAL : THE_IMMORTAL) : THE_DESTROYER;
                case LEGENDARY -> random.nextFloat() < 0.6f ? BANE_OF_WORLDS : random.nextFloat() < 0.8f ? HERALD_OF_DOOM : THE_IMMORTAL;
                case MYTHIC -> random.nextFloat() < 0.5f ? AVATAR_OF_CHAOS : random.nextFloat() < 0.8f ? HERALD_OF_DOOM : BANE_OF_WORLDS;
            };
        }
    }

    // ==================== ENHANCED MONSTER VARIANT ====================

    public static class MonsterVariant {
        public final MonsterRarity rarity;
        public final MonsterPrefix prefix;
        public final MonsterSuffix suffix;
        public final String enhancedName;
        public final int dynamicLevel;
        public final int baseArmor;
        public final float biomeStatMultiplier;
        public final float totalStatMultiplier;
        public final float totalExpMultiplier;

        public MonsterVariant(MonsterRarity rarity, MonsterPrefix prefix, MonsterSuffix suffix,
                              String baseName, Random random, int baseLevel,
                              float localDifficultyMult, float biomeStatMult) {
            this.rarity = rarity;
            this.prefix = prefix;
            this.suffix = suffix;
            this.biomeStatMultiplier = biomeStatMult;

            // Generate enhanced name with stat keywords
            this.enhancedName = NameBasedBonusSystem.generateEnhancedName(baseName, rarity, random);

            // DYNAMIC LEVEL SCALING - grows with local difficulty
            int levelBonus = Math.round((localDifficultyMult - 1.0f) * 10); // 0-50 bonus levels
            int rarityLevelBonus = rarity.ordinal() * 3; // 0-15 bonus levels by rarity
            this.dynamicLevel = baseLevel + levelBonus + rarityLevelBonus;

            // ARMOR SCALING - base 2 + random + rarity bonus, cap 30
            int rarityArmorBonus = rarity.ordinal() * 2; // 0-10 bonus armor
            int randomArmor = random.nextInt(6); // 0-5 random armor
            this.baseArmor = Math.min(30, 2 + randomArmor + rarityArmorBonus);

            // Multiplicatively stack all bonuses (including biome!)
            this.totalStatMultiplier = rarity.statMultiplier * prefix.statMultiplier *
                    suffix.statMultiplier * biomeStatMult;
            this.totalExpMultiplier = rarity.expMultiplier * prefix.expMultiplier * suffix.expMultiplier;
        }

        /**
         * Create enhanced display name with colored level styling
         */
        public Text getEnhancedDisplayName() {
            Text levelText = createStyledLevelDisplay(dynamicLevel, rarity);

            MutableText nameText = Text.empty();

            if (!prefix.name.isEmpty()) {
                nameText = nameText.append(Text.literal(prefix.name + " ").formatted(getRarityColor()));
            }

            nameText = nameText.append(Text.literal(enhancedName).formatted(getRarityColor()));

            if (!suffix.name.isEmpty()) {
                nameText = nameText.append(Text.literal(" " + suffix.name).formatted(getRarityColor()));
            }

            return Text.empty()
                    .append(levelText)
                    .append(Text.literal(" "))
                    .append(nameText);
        }

        private Text createStyledLevelDisplay(int level, MonsterRarity rarity) {
            String levelDisplay;
            Formatting levelColor;

            switch (rarity) {
                case COMMON -> {
                    levelDisplay = "[Lv." + level + "]";
                    levelColor = Formatting.GREEN;
                }
                case UNCOMMON -> {
                    levelDisplay = "⟨Lv." + level + "⟩";
                    levelColor = Formatting.YELLOW;
                }
                case RARE -> {
                    levelDisplay = "⟪Lv." + level + "⟫";
                    levelColor = Formatting.BLUE;
                }
                case EPIC -> {
                    levelDisplay = "◆Lv." + level + "◆";
                    levelColor = Formatting.LIGHT_PURPLE;
                }
                case LEGENDARY -> {
                    levelDisplay = "★Lv." + level + "★";
                    levelColor = Formatting.GOLD;
                }
                case MYTHIC -> {
                    levelDisplay = "⚡Lv." + level + "⚡";
                    levelColor = Formatting.RED;
                }
                default -> {
                    levelDisplay = "[Lv." + level + "]";
                    levelColor = Formatting.WHITE;
                }
            }

            return Text.literal(levelDisplay).formatted(levelColor, Formatting.BOLD);
        }

        private Formatting getRarityColor() {
            return switch (rarity) {
                case COMMON -> Formatting.WHITE;
                case UNCOMMON -> Formatting.GREEN;
                case RARE -> Formatting.BLUE;
                case EPIC -> Formatting.LIGHT_PURPLE;
                case LEGENDARY -> Formatting.GOLD;
                case MYTHIC -> Formatting.RED;
            };
        }
    }

    // ==================== MAIN CALCULATION METHODS ====================

    /**
     * ENHANCED monster generation with local difficulty and biome integration
     */
    public static MonsterVariant generateEnhancedMonsterVariant(Random random, String baseName,
                                                                int baseLevel, World world, BlockPos pos) {
        // Get local difficulty (0-6+)
        LocalDifficulty localDiff = world.getLocalDifficulty(pos);
        float localDifficultyValue = localDiff.getLocalDifficulty(); // 0.0 to 6.0+

        // Cap and convert to multiplier index
        int difficultyIndex = Math.min(6, (int) localDifficultyValue);
        float localDifficultyMult = LOCAL_DIFFICULTY_SPAWN_MULTIPLIERS[difficultyIndex];

        // Get biome stat multiplier
        float biomeStatMult = getBiomeStatMultiplier(world, pos);

        // Generate rarity with adjusted spawn chances
        MonsterRarity rarity = generateRarityWithLocalDifficulty(random, localDifficultyMult);

        MonsterPrefix prefix = MonsterPrefix.getRandom(random, rarity);
        MonsterSuffix suffix = MonsterSuffix.getRandom(random, rarity);

        // Debug local difficulty effects
        if (localDifficultyValue > 3.0f) {
            System.out.printf("[LocalDifficulty] %.1f difficulty → ×%.1f spawn rates, ×%.1f biome stats\n",
                    localDifficultyValue, localDifficultyMult, biomeStatMult);
        }

        return new MonsterVariant(rarity, prefix, suffix, baseName, random,
                baseLevel, localDifficultyMult, biomeStatMult);
    }

    /**
     * Generate rarity with local difficulty affecting spawn rates
     */
    private static MonsterRarity generateRarityWithLocalDifficulty(Random random, float localDifficultyMult) {
        float roll = random.nextFloat();

        // Check from rarest to most common with adjusted chances
        for (MonsterRarity rarity : MonsterRarity.values()) {
            if (rarity == MonsterRarity.COMMON) continue; // Skip common for now

            float adjustedChance = rarity.getAdjustedSpawnChance(localDifficultyMult);
            if (roll <= adjustedChance) {
                System.out.printf("[RareSpawn] %s spawned! (Chance: %.6f, Local Difficulty: ×%.1f)\n",
                        rarity.name(), adjustedChance, localDifficultyMult);
                return rarity;
            }
        }

        return MonsterRarity.COMMON; // Default
    }

    /**
     * Get biome stat multiplier for monster stats during spawn
     */
    public static float getBiomeStatMultiplier(World world, BlockPos pos) {
        var biomeHolder = world.getBiome(pos);

        if (biomeHolder.isIn(ModTags.BiomeTags.HELLISH_BIOMES)) {
            return HELLISH_STAT_MULT; // 3.0x - Badlands are hellish
        }
        if (biomeHolder.isIn(ModTags.BiomeTags.EXTREME_BIOMES)) {
            return EXTREME_STAT_MULT; // 2.0x - Desert is tough
        }
        if (biomeHolder.isIn(ModTags.BiomeTags.HARSH_BIOMES)) {
            return HARSH_STAT_MULT; // 1.5x - Mountains are challenging
        }
        if (biomeHolder.isIn(ModTags.BiomeTags.MODERATE_BIOMES)) {
            return MODERATE_STAT_MULT; // 1.2x - Forest is slightly dangerous
        }

        return PEACEFUL_STAT_MULT; // 1.0x - Plains are vanilla
    }

    /**
     * ENHANCED monster variant application with all new systems
     */
    public static void applyMonsterVariant(LivingEntity entity, MonsterVariant variant) {
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(entity);
        if (levelComponent == null) return;

        // STEP 1: Set dynamic level
        levelComponent.getLevelSystem().setLevel((short) variant.dynamicLevel);

        // STEP 2: Apply stat multipliers (includes biome multiplier!)
        for (StatTypes statType : StatTypes.values()) {
            var stat = levelComponent.getStatByType(statType);
            if (stat != null) {
                int currentValue = stat.getCurrentValue();
                if (currentValue > 0) {
                    int newValue = Math.round(currentValue * variant.totalStatMultiplier);
                    stat.setPoints((short) Math.min(newValue, Short.MAX_VALUE));

                    if (variant.totalStatMultiplier > 2.0f) {
                        System.out.printf("[StatBoost] %s %s: %d → %d (×%.1f total)\n",
                                entity.getType().getName().getString(), statType.name(),
                                currentValue, newValue, variant.totalStatMultiplier);
                    }
                }
            }
        }

        entity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(variant.baseArmor);
        System.out.printf("[ArmorBoost] %s armor: %d (base 4 + rarity + random)\n",
                entity.getType().getName().getString(), variant.baseArmor);

        levelComponent.refreshAllStatEffectsInternal();

        Text enhancedName = variant.getEnhancedDisplayName();
        entity.setCustomName(enhancedName);

        NameBasedBonusSystem.applyNameBasedBonuses(entity);

        entity.setHealth(entity.getMaxHealth());

        if (variant.rarity.ordinal() >= MonsterRarity.EPIC.ordinal()) {
            entity.setGlowing(true);
        }

        // Debug summary for rare monsters
        if (variant.rarity != MonsterRarity.COMMON) {
            System.out.printf("[EnhancedSpawn] %s Lv.%d: ×%.1f stats, %d armor, ×%.1f biome\n",
                    variant.rarity.name(), variant.dynamicLevel, variant.totalStatMultiplier,
                    variant.baseArmor, variant.biomeStatMultiplier);
        }
    }

    // ==================== DAMAGE CALCULATION (SIMPLIFIED) ====================

    public static float calculateAmplifiedDamage(LivingEntity monster, float baseDamage, LivingEntity nearestPlayer) {
        if (monster.getWorld().isClient()) return baseDamage;

        BlockPos pos = monster.getBlockPos();
        World world = monster.getWorld();

        float progressionAmp = calculateProgressionAmplification(monster, nearestPlayer);
        float environmentalAmp = calculateEnvironmentalAmplification(world, pos);
        float variantAmp = getVariantDamageMultiplier(monster);

        float totalAmplification = progressionAmp * environmentalAmp * variantAmp;
        totalAmplification = MathHelper.clamp(totalAmplification, 0.5f, 50.0f);

        return baseDamage * totalAmplification;
    }

    public static float calculateProgressionAmplification(LivingEntity monster, LivingEntity nearestPlayer) {
        if (nearestPlayer == null) return PROGRESSION_BASE;

        LivingLevelComponent playerLevel = ModEntityComponents.LIVINGLEVEL.getNullable(nearestPlayer);
        LivingLevelComponent monsterLevel = ModEntityComponents.LIVINGLEVEL.getNullable(monster);

        if (playerLevel == null || monsterLevel == null) return PROGRESSION_BASE;

        int levelDifference = playerLevel.getLevel() - monsterLevel.getLevel();
        float progressionBonus = Math.max(0, levelDifference * PROGRESSION_SCALING);
        progressionBonus = Math.min(progressionBonus, MAX_PROGRESSION_BONUS * PROGRESSION_SCALING);

        return PROGRESSION_BASE + progressionBonus;
    }

    public static float calculateEnvironmentalAmplification(World world, BlockPos pos) {
        float envAmp = 1.0f;

        if (world.isNight()) {
            envAmp *= NIGHT_MULTIPLIER;
        } else {
            envAmp *= DAY_MULTIPLIER;
        }

        if (world.isThundering() || world.isRaining()) {
            envAmp *= STORM_MULTIPLIER;
        }

        int y = pos.getY();
        if (y < 0) {
            envAmp *= DEEP_UNDERGROUND_MULTIPLIER;
        } else if (y < 40) {
            envAmp *= UNDERGROUND_MULTIPLIER;
        }

        return envAmp;
    }

    /**
     * Use unified rarity detection (no more redundant code!)
     */
    public static float getVariantExpMultiplier(LivingEntity entity) {
        MonsterRarity rarity = NameBasedBonusSystem.getRarityFromName(entity);
        return rarity.expMultiplier;
    }

    private static float getVariantDamageMultiplier(LivingEntity entity) {
        MonsterRarity rarity = NameBasedBonusSystem.getRarityFromName(entity);

        return switch (rarity) {
            case COMMON -> 1.0f;
            case UNCOMMON -> 1.5f;
            case RARE -> 3.0f;
            case EPIC -> 6.0f;
            case LEGENDARY -> 10.0f;
            case MYTHIC -> 25.0f;
        };
    }

    public static PlayerEntity findNearestPlayer(LivingEntity monster, double maxDistance) {
        return monster.getWorld().getClosestPlayer(monster.getX(), monster.getY(), monster.getZ(), maxDistance, false);
    }

    public static float applyProgressiveDifficulty(LivingEntity monster, float damage) {
        PlayerEntity nearestPlayer = findNearestPlayer(monster, 64.0);
        return calculateAmplifiedDamage(monster, damage, nearestPlayer);
    }
}