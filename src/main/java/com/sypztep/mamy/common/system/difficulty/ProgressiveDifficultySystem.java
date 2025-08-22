package com.sypztep.mamy.common.system.difficulty;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModTags;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Korean MMORPG-style Progressive Difficulty System
 * Enhanced with name-based stat bonuses and colorful server-side names
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

    // ==================== BIOME DIFFICULTY MULTIPLIERS ====================

    private static final float PEACEFUL_MULTIPLIER = 1.0f;
    private static final float MODERATE_MULTIPLIER = 1.2f;
    private static final float HARSH_MULTIPLIER = 1.5f;
    private static final float EXTREME_MULTIPLIER = 2.5f;
    private static final float HELLISH_MULTIPLIER = 4.0f;

    // ==================== MONSTER RARITY SYSTEM ====================

    public enum MonsterRarity {
        COMMON(0.99f, 1.0f, 1.0f, ""),
        UNCOMMON(0.008f, 1.5f, 2.0f, "Strong"),
        RARE(0.0015f, 3.0f, 5.0f, "Fierce"),
        EPIC(0.0004f, 6.0f, 10.0f, "Savage"),
        LEGENDARY(0.0001f, 10.0f, 15.0f, "Apex"),
        MYTHIC(0.000001f, 15.0f, 25.0f, "Godlike");

        public final float spawnChance;
        public final float statMultiplier;
        public final float expMultiplier;
        public final String defaultPrefix;

        MonsterRarity(float spawnChance, float statMultiplier, float expMultiplier, String defaultPrefix) {
            this.spawnChance = spawnChance;
            this.statMultiplier = statMultiplier;
            this.expMultiplier = expMultiplier;
            this.defaultPrefix = defaultPrefix;
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
        public final float totalStatMultiplier;
        public final float totalExpMultiplier;

        public MonsterVariant(MonsterRarity rarity, MonsterPrefix prefix, MonsterSuffix suffix, String baseName, Random random) {
            this.rarity = rarity;
            this.prefix = prefix;
            this.suffix = suffix;

            // Generate enhanced name with stat keywords
            this.enhancedName = NameBasedBonusSystem.generateEnhancedName(baseName, rarity, random);

            // Multiplicatively stack all bonuses
            this.totalStatMultiplier = rarity.statMultiplier * prefix.statMultiplier * suffix.statMultiplier;
            this.totalExpMultiplier = rarity.expMultiplier * prefix.expMultiplier * suffix.expMultiplier;
        }

        /**
         * Create enhanced display name with colored level styling - SERVER SIDE ONLY
         */
        public Text getEnhancedDisplayName(int level) {
            // Create level display with enhanced styling and colors
            Text levelText = createStyledLevelDisplay(level, rarity);

            // Create name parts
            MutableText nameText = Text.empty();

            // Add prefix if present
            if (!prefix.name.isEmpty()) {
                nameText = nameText.append(Text.literal(prefix.name + " ").formatted(getRarityColor()));
            }

            // Add enhanced base name (with keywords)
            nameText = nameText.append(Text.literal(enhancedName).formatted(getRarityColor()));

            // Add suffix if present
            if (!suffix.name.isEmpty()) {
                nameText = nameText.append(Text.literal(" " + suffix.name).formatted(getRarityColor()));
            }

            // Combine level and name
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

        /**
         * Legacy method for string-based names (fallback)
         */
        public String getDisplayNameString(int level) {
            StringBuilder name = new StringBuilder();

            // Level prefix with enhanced styling
            String levelPrefix = switch (rarity) {
                case COMMON -> "[Lv." + level + "]";
                case UNCOMMON -> "⟨Lv." + level + "⟩";
                case RARE -> "⟪Lv." + level + "⟫";
                case EPIC -> "◆Lv." + level + "◆";
                case LEGENDARY -> "★Lv." + level + "★";
                case MYTHIC -> "⚡Lv." + level + "⚡";
            };

            name.append(levelPrefix).append(" ");

            // Prefix
            if (!prefix.name.isEmpty()) {
                name.append(prefix.name).append(" ");
            }

            // Enhanced name (includes stat keywords)
            name.append(enhancedName);

            // Suffix
            if (!suffix.name.isEmpty()) {
                name.append(" ").append(suffix.name);
            }

            return name.toString();
        }
    }

    // ==================== MAIN CALCULATION METHODS ====================

    /**
     * Generate enhanced monster variant with name-based bonuses
     */
    public static MonsterVariant generateMonsterVariant(Random random, String baseName) {
        float roll = random.nextFloat();
        MonsterRarity rarity = MonsterRarity.COMMON;

        // Fixed cumulative probability
        if (roll <= MonsterRarity.MYTHIC.spawnChance) {
            rarity = MonsterRarity.MYTHIC;
        } else if (roll <= MonsterRarity.MYTHIC.spawnChance + MonsterRarity.LEGENDARY.spawnChance) {
            rarity = MonsterRarity.LEGENDARY;
        } else if (roll <= MonsterRarity.MYTHIC.spawnChance + MonsterRarity.LEGENDARY.spawnChance + MonsterRarity.EPIC.spawnChance) {
            rarity = MonsterRarity.EPIC;
        } else if (roll <= MonsterRarity.MYTHIC.spawnChance + MonsterRarity.LEGENDARY.spawnChance +
                MonsterRarity.EPIC.spawnChance + MonsterRarity.RARE.spawnChance) {
            rarity = MonsterRarity.RARE;
        } else if (roll <= MonsterRarity.MYTHIC.spawnChance + MonsterRarity.LEGENDARY.spawnChance +
                MonsterRarity.EPIC.spawnChance + MonsterRarity.RARE.spawnChance + MonsterRarity.UNCOMMON.spawnChance) {
            rarity = MonsterRarity.UNCOMMON;
        }

        MonsterPrefix prefix = MonsterPrefix.getRandom(random, rarity);
        MonsterSuffix suffix = MonsterSuffix.getRandom(random, rarity);

        return new MonsterVariant(rarity, prefix, suffix, baseName, random);
    }

    /**
     * Apply enhanced monster variant with BOTH rarity multipliers AND name bonuses
     * SETS SERVER-SIDE CUSTOM NAME with colors and styling
     */
    public static void applyMonsterVariant(LivingEntity entity, MonsterVariant variant) {
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(entity);
        if (levelComponent == null) return;

        // STEP 1: Apply rarity multipliers to base stats
        for (StatTypes statType : StatTypes.values()) {
            var stat = levelComponent.getStatByType(statType);
            if (stat != null) {
                int currentValue = stat.getCurrentValue();
                if (currentValue > 0) {
                    int newValue = Math.round(currentValue * variant.totalStatMultiplier);
                    stat.setPoints((short) Math.min(newValue, Short.MAX_VALUE));
                }
            }
        }

        levelComponent.refreshAllStatEffectsInternal();

        int level = levelComponent.getLevel();
        Text enhancedName = variant.getEnhancedDisplayName(level);

        entity.setCustomName(enhancedName);
//        entity.setCustomNameVisible(true);

        NameBasedBonusSystem.applyNameBasedBonuses(entity);

        entity.setHealth(entity.getMaxHealth());

        if (variant.rarity.ordinal() >= MonsterRarity.EPIC.ordinal()) {
            entity.setGlowing(true);
        }

        // Debug log
//        if (variant.rarity != MonsterRarity.COMMON) {
//            System.out.printf("[EnhancedSpawn] %s spawned with enhanced name: %s\n",
//                    variant.rarity.name(), variant.getDisplayNameString(level));
//        }
    }

    // [All other methods remain the same - calculateProgressionAmplification, etc.]

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

    public static float calculateBiomeAmplification(World world, BlockPos pos) {
        var biomeHolder = world.getBiome(pos);

        if (biomeHolder.isIn(ModTags.BiomeTags.HELLISH_BIOMES)) {
            return HELLISH_MULTIPLIER;
        }
        if (biomeHolder.isIn(ModTags.BiomeTags.EXTREME_BIOMES)) {
            return EXTREME_MULTIPLIER;
        }
        if (biomeHolder.isIn(ModTags.BiomeTags.HARSH_BIOMES)) {
            return HARSH_MULTIPLIER;
        }
        if (biomeHolder.isIn(ModTags.BiomeTags.MODERATE_BIOMES)) {
            return MODERATE_MULTIPLIER;
        }

        return PEACEFUL_MULTIPLIER;
    }

    public static float getVariantExpMultiplier(LivingEntity entity) {
        if (!entity.hasCustomName()) return 1.0f;

        String name = entity.getCustomName().getString();

        if (name.contains("Void") || name.contains("Cosmic") || name.contains("Avatar")) {
            return MonsterRarity.MYTHIC.expMultiplier;
        } else if (name.contains("Primordial") || name.contains("Dimensional") || name.contains("Bane")) {
            return MonsterRarity.LEGENDARY.expMultiplier;
        } else if (name.contains("Nightmare") || name.contains("Abyssal") || name.contains("Eternal")) {
            return MonsterRarity.EPIC.expMultiplier;
        } else if (name.contains("Bloodthirsty") || name.contains("Corrupted") || name.contains("Hunter")) {
            return MonsterRarity.RARE.expMultiplier;
        } else if (name.contains("Strong") || name.contains("Angry") || name.contains("Wild")) {
            return MonsterRarity.UNCOMMON.expMultiplier;
        }

        return MonsterRarity.COMMON.expMultiplier;
    }

    public static float calculateAmplifiedDamage(LivingEntity monster, float baseDamage, LivingEntity nearestPlayer) {
        if (monster.getWorld().isClient()) return baseDamage;

        BlockPos pos = monster.getBlockPos();
        World world = monster.getWorld();

        float progressionAmp = calculateProgressionAmplification(monster, nearestPlayer);
        float environmentalAmp = calculateEnvironmentalAmplification(world, pos);
        float biomeAmp = calculateBiomeAmplification(world, pos);
        float variantAmp = getVariantDamageMultiplier(monster);

        float totalAmplification = progressionAmp * environmentalAmp * biomeAmp * variantAmp;
        totalAmplification = MathHelper.clamp(totalAmplification, 0.5f, 50.0f);

        return baseDamage * totalAmplification;
    }

    private static float getVariantDamageMultiplier(LivingEntity entity) {
        if (!entity.hasCustomName()) return 1.0f;
        String name = entity.getCustomName().getString();

        if (name.contains("Void") || name.contains("Avatar")) {
            return 25.0f;
        } else if (name.contains("Cosmic") || name.contains("Bane")) {
            return 15.0f;
        } else if (name.contains("Primordial") || name.contains("Dimensional")) {
            return 10.0f;
        } else if (name.contains("Nightmare") || name.contains("Abyssal")) {
            return 6.0f;
        } else if (name.contains("Bloodthirsty") || name.contains("Corrupted")) {
            return 3.0f;
        } else if (name.contains("Strong") || name.contains("Angry")) {
            return 1.5f;
        }

        return 1.0f;
    }

    public static PlayerEntity findNearestPlayer(LivingEntity monster, double maxDistance) {
        return monster.getWorld().getClosestPlayer(monster.getX(), monster.getY(), monster.getZ(), maxDistance, false);
    }

    public static float applyProgressiveDifficulty(LivingEntity monster, float damage) {
        PlayerEntity nearestPlayer = findNearestPlayer(monster, 64.0);
        return calculateAmplifiedDamage(monster, damage, nearestPlayer);
    }
}