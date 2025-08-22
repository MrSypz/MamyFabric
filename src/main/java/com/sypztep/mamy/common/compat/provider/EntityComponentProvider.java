package com.sypztep.mamy.common.compat.provider;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.event.living.MobSpawnStatsEvent;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.difficulty.ProgressiveDifficultySystem;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public enum EntityComponentProvider implements IEntityComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        Entity target = entityAccessor.getEntity();
        PlayerEntity viewer = entityAccessor.getPlayer();

        if (!(target instanceof LivingEntity livingTarget) || viewer == null) return;

        // === UNIVERSAL STATS (for all living entities) ===
        addBasicStats(iTooltip, livingTarget, viewer);

        // === PLAYER-SPECIFIC STATS ===
        if (livingTarget instanceof PlayerEntity playerTarget) {
            addPlayerStats(iTooltip, playerTarget);
        }
        // === MONSTER-SPECIFIC STATS ===
        else {
            addMonsterStats(iTooltip, livingTarget, viewer);
        }
    }

    private void addBasicStats(ITooltip tooltip, LivingEntity target, PlayerEntity viewer) {
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(target);
        if (levelComponent == null) return;

        // Level and Health
        float currentHealth = target.getHealth();
        float maxHealth = target.getMaxHealth();

        tooltip.add(Text.literal("Health: ").formatted(Formatting.RED)
                .append(Text.literal(String.format("%.1f/%.1f", currentHealth, maxHealth)).formatted(Formatting.WHITE)));

        // Hit Rate (viewer attacking target)
        float hitRate = LivingEntityUtil.hitRate(viewer, target) * 100;
        Formatting hitRateColor = getHitRateColor(hitRate);

        tooltip.add(Text.literal("Hit Rate: ").formatted(Formatting.YELLOW)
                .append(Text.literal(String.format("%.1f%%", hitRate)).formatted(hitRateColor)));
    }

    private void addPlayerStats(ITooltip tooltip, PlayerEntity player) {
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(player);
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.getNullable(player);

        if (levelComponent == null) return;

        // Experience
        long currentExp = levelComponent.getExperience();
        long expToNext = levelComponent.getExperienceToNextLevel();

        tooltip.add(Text.literal("Exp: ").formatted(Formatting.AQUA)
                .append(Text.literal(String.format("%,d/%,d", currentExp, currentExp + expToNext)).formatted(Formatting.WHITE)));

        // Class Information
        if (classComponent != null) {
            String className = classComponent.getClassManager().getCurrentClass().getDisplayName();
            int classLevel = classComponent.getClassManager().getClassLevel();

            tooltip.add(Text.literal("Class: ").formatted(Formatting.LIGHT_PURPLE)
                    .append(Text.literal(className + " Lv." + classLevel).formatted(Formatting.WHITE)));
        }

        // Combat Stats
        addCombatStats(tooltip, player);
    }

    private void addMonsterStats(ITooltip tooltip, LivingEntity monster, PlayerEntity viewer) {
        // Rarity Information
        addRarityInfo(tooltip, monster);

        // Biome Danger
        addBiomeDanger(tooltip, monster);

        // Combat Stats
        addCombatStats(tooltip, monster);

        // Experience Reward Preview
        addExpReward(tooltip, monster, viewer);
    }

    private void addRarityInfo(ITooltip tooltip, LivingEntity monster) {
        if (!monster.hasCustomName()) return;

        int rarityLevel = MobSpawnStatsEvent.getRarityLevel(monster);

        if (rarityLevel > 0) {
            String[] rarityNames = {"Common", "Uncommon", "Rare", "Epic", "Legendary", "Mythic"};
            Formatting[] rarityColors = {
                    Formatting.WHITE, Formatting.GREEN, Formatting.BLUE,
                    Formatting.LIGHT_PURPLE, Formatting.GOLD, Formatting.RED
            };

            String rarityName = rarityNames[Math.min(rarityLevel, 5)];
            Formatting rarityColor = rarityColors[Math.min(rarityLevel, 5)];

            float expMult = ProgressiveDifficultySystem.getVariantExpMultiplier(monster);

            tooltip.add(Text.literal("Rarity: ").formatted(Formatting.GRAY)
                    .append(Text.literal(rarityName).formatted(rarityColor))
                    .append(Text.literal(" (×" + String.format("%.1f", expMult) + " EXP)").formatted(Formatting.YELLOW)));
        }
    }

    private void addBiomeDanger(ITooltip tooltip, LivingEntity monster) {
        BlockPos pos = monster.getBlockPos();
        float biomeMultiplier = ProgressiveDifficultySystem.calculateBiomeAmplification(monster.getWorld(), pos);

        if (biomeMultiplier > 1.0f) {
            String dangerLevel = getDangerLevel(biomeMultiplier);
            Formatting dangerColor = getDangerColor(biomeMultiplier);

            tooltip.add(Text.literal("Biome: ").formatted(Formatting.GRAY)
                    .append(Text.literal(dangerLevel).formatted(dangerColor))
                    .append(Text.literal(" (×" + String.format("%.1f", biomeMultiplier) + ")").formatted(Formatting.GRAY)));
        }
    }

    private void addCombatStats(ITooltip tooltip, LivingEntity entity) {
        // Accuracy & Evasion
        double accuracy = entity.getAttributeValue(ModEntityAttributes.ACCURACY);
        double evasion = entity.getAttributeValue(ModEntityAttributes.EVASION);

        tooltip.add(Text.literal("Accuracy: ").formatted(Formatting.GOLD)
                .append(Text.literal(String.format("%.0f", accuracy)).formatted(Formatting.WHITE))
                .append(Text.literal(" | Evasion: ").formatted(Formatting.GOLD))
                .append(Text.literal(String.format("%.0f", evasion)).formatted(Formatting.WHITE)));

        // Critical Stats
        double critChance = entity.getAttributeValue(ModEntityAttributes.CRIT_CHANCE) * 100;
        double critDamage = entity.getAttributeValue(ModEntityAttributes.CRIT_DAMAGE) * 100;

        if (critChance > 0) {
            tooltip.add(Text.literal("Crit: ").formatted(Formatting.RED)
                    .append(Text.literal(String.format("%.1f%%", critChance)).formatted(Formatting.WHITE))
                    .append(Text.literal(" | Damage: ").formatted(Formatting.RED))
                    .append(Text.literal(String.format("%.0f%%", critDamage)).formatted(Formatting.WHITE)));
        }

        // Damage Types
        double meleeFlat = entity.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT);
        double meleeMult = entity.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_MULT) * 100;

        if (meleeFlat > 0 || meleeMult > 0) {
            tooltip.add(Text.literal("Melee: ").formatted(Formatting.DARK_RED)
                    .append(Text.literal(String.format("%.0f", meleeFlat)).formatted(Formatting.WHITE))
                    .append(Text.literal(" (+").formatted(Formatting.GRAY))
                    .append(Text.literal(String.format("%.0f%%", meleeMult)).formatted(Formatting.WHITE))
                    .append(Text.literal(")").formatted(Formatting.GRAY)));
        }

        double magicFlat = entity.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT);
        double magicMult = entity.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_MULT) * 100;

        if (magicFlat > 0 || magicMult > 0) {
            tooltip.add(Text.literal("Magic: ").formatted(Formatting.DARK_PURPLE)
                    .append(Text.literal(String.format("%.0f", magicFlat)).formatted(Formatting.WHITE))
                    .append(Text.literal(" (+").formatted(Formatting.GRAY))
                    .append(Text.literal(String.format("%.0f%%", magicMult)).formatted(Formatting.WHITE))
                    .append(Text.literal(")").formatted(Formatting.GRAY)));
        }

        double projectileFlat = entity.getAttributeValue(ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE_FLAT);
        double projectileMult = entity.getAttributeValue(ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE_MULT) * 100;

        if (projectileFlat > 0 || projectileMult > 0) {
            tooltip.add(Text.literal("Ranged: ").formatted(Formatting.DARK_GREEN)
                    .append(Text.literal(String.format("%.0f", projectileFlat)).formatted(Formatting.WHITE))
                    .append(Text.literal(" (+").formatted(Formatting.GRAY))
                    .append(Text.literal(String.format("%.0f%%", projectileMult)).formatted(Formatting.WHITE))
                    .append(Text.literal(")").formatted(Formatting.GRAY)));
        }

        // Resistances
        addResistances(tooltip, entity);
    }

    private void addResistances(ITooltip tooltip, LivingEntity entity) {
        double damageReduction = entity.getAttributeValue(ModEntityAttributes.DAMAGE_REDUCTION) * 100;

        if (damageReduction > 0) {
            tooltip.add(Text.literal("Damage Reduction: ").formatted(Formatting.BLUE)
                    .append(Text.literal(String.format("%.1f%%", damageReduction)).formatted(Formatting.WHITE)));
        }

        // Check for significant resistances
        double magicRes = entity.getAttributeValue(ModEntityAttributes.MAGIC_RESISTANCE) * 100;
        double meleeRes = entity.getAttributeValue(ModEntityAttributes.MELEE_RESISTANCE) * 100;
        double rangedRes = entity.getAttributeValue(ModEntityAttributes.PROJECTILE_RESISTANCE) * 100;

        if (magicRes > 0 || meleeRes > 0 || rangedRes > 0) {
            tooltip.add(Text.literal("Resistances: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("M:%.0f%% ", magicRes)).formatted(Formatting.DARK_PURPLE))
                    .append(Text.literal(String.format("P:%.0f%% ", meleeRes)).formatted(Formatting.DARK_RED))
                    .append(Text.literal(String.format("R:%.0f%%", rangedRes)).formatted(Formatting.DARK_GREEN)));
        }
    }

    private void addExpReward(ITooltip tooltip, LivingEntity monster, PlayerEntity viewer) {
        // Calculate potential experience reward
        int baseExp = com.sypztep.mamy.common.data.MobExpEntry.getExpReward(monster.getType());

        if (baseExp > 0) {
            LivingLevelComponent viewerLevel = ModEntityComponents.LIVINGLEVEL.getNullable(viewer);
            LivingLevelComponent monsterLevel = ModEntityComponents.LIVINGLEVEL.getNullable(monster);

            if (viewerLevel != null && monsterLevel != null) {
                float levelPenalty = com.sypztep.mamy.common.util.ExpUtil.calculateLevelPenalty(
                        viewerLevel.getLevel(), monsterLevel.getLevel());
                float rarityBonus = ProgressiveDifficultySystem.getVariantExpMultiplier(monster);

                int finalExp = Math.round(baseExp * levelPenalty * rarityBonus);

                Formatting expColor = finalExp > baseExp * 2 ? Formatting.GOLD :
                        finalExp > baseExp ? Formatting.YELLOW : Formatting.WHITE;

                tooltip.add(Text.literal("Exp Reward: ").formatted(Formatting.AQUA)
                        .append(Text.literal(String.format("%,d", finalExp)).formatted(expColor)));
            }
        }
    }

    // === UTILITY METHODS ===

    private Formatting getHitRateColor(float hitRate) {
        if (hitRate >= 90) return Formatting.GREEN;
        if (hitRate >= 75) return Formatting.YELLOW;
        if (hitRate >= 50) return Formatting.GOLD;
        if (hitRate >= 25) return Formatting.RED;
        return Formatting.DARK_RED;
    }

    private String getDangerLevel(float multiplier) {
        if (multiplier >= 4.0f) return "Hellish";
        if (multiplier >= 2.5f) return "Extreme";
        if (multiplier >= 1.5f) return "Harsh";
        if (multiplier >= 1.2f) return "Moderate";
        return "Peaceful";
    }

    private Formatting getDangerColor(float multiplier) {
        if (multiplier >= 4.0f) return Formatting.DARK_RED;
        if (multiplier >= 2.5f) return Formatting.RED;
        if (multiplier >= 1.5f) return Formatting.GOLD;
        if (multiplier >= 1.2f) return Formatting.YELLOW;
        return Formatting.GREEN;
    }

    @Override
    public Identifier getUid() {
        return Mamy.id("stats_config");
    }
}