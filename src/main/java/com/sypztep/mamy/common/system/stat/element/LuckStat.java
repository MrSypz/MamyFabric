package com.sypztep.mamy.common.system.stat.element;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.stat.Stat;
import com.sypztep.mamy.common.util.AttributeModification;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class LuckStat extends Stat {
    private static final double CRIT_CHANCE_SCALING = 0.0025; // 0.25% per point (every 4 points = 1%)
    private static final double MAGIC_DAMAGE_SCALING = 0.002; // 0.2% per point
    private static final double ATTACK_SPEED_SCALING = 0.002; // 0.2% per point

    public LuckStat(short baseValue) {
        super(baseValue);
    }

    @Override
    public void applyPrimaryEffect(LivingEntity living) {
        applyEffect(living,
                ModEntityAttributes.CRIT_CHANCE,
                getPrimaryId(),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> (CRIT_CHANCE_SCALING * this.currentValue)
        );
    }

    @Override
    public void applySecondaryEffect(LivingEntity living) {
        List<AttributeModification> modifications = List.of(
                new AttributeModification(
                        ModEntityAttributes.MAGIC_ATTACK_DAMAGE,
                        getSecondaryId(),
                        EntityAttributeModifier.Operation.ADD_VALUE,
                        baseValue -> (MAGIC_DAMAGE_SCALING * this.currentValue)
                ),
                new AttributeModification(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        getSecondaryId(),
                        EntityAttributeModifier.Operation.ADD_VALUE,
                        baseValue -> (ATTACK_SPEED_SCALING * this.currentValue)
                )
        );
        applyEffects(living, modifications);

        // Special bonus effects
        int statPoints = currentValue - baseValue;
        if (statPoints > 0) {
            int accuracyBonus = statPoints / 3;
            if (accuracyBonus > 0) {
                applyEffect(living,
                        ModEntityAttributes.ACCURACY,
                        Mamy.id("luck_accuracy_bonus"),
                        baseValue -> (double) accuracyBonus
                );
            }

            int evasionBonus = statPoints / 5;
            if (evasionBonus > 0) {
                applyEffect(living,
                        ModEntityAttributes.EVASION,
                        Mamy.id("luck_evasion_bonus"),
                        baseValue -> (double) evasionBonus
                );
            }
        }
    }

    @Override
    public List<Text> getEffectDescription(int additionalPoints) {
        int futureValue = getValue() + additionalPoints;

        double currentCritChance = calculateCritChanceBonus(getValue(), getBaseValue()) * 100;
        double futureCritChance = calculateCritChanceBonus(futureValue, getBaseValue()) * 100;
        double critChanceIncrease = futureCritChance - currentCritChance;

        double currentMagicDamage = calculateMagicDamageBonus(getValue(), getBaseValue()) * 100;
        double futureMagicDamage = calculateMagicDamageBonus(futureValue, getBaseValue()) * 100;
        double magicDamageIncrease = futureMagicDamage - currentMagicDamage;

        double currentAttackSpeed = calculateAttackSpeedBonus(getValue(), getBaseValue()) * 100;
        double futureAttackSpeed = calculateAttackSpeedBonus(futureValue, getBaseValue()) * 100;
        double attackSpeedIncrease = futureAttackSpeed - currentAttackSpeed;

        int currentAccuracy = calculateAccuracyBonus(getValue(), getBaseValue());
        int futureAccuracy = calculateAccuracyBonus(futureValue, getBaseValue());
        int accuracyIncrease = futureAccuracy - currentAccuracy;

        int currentEvasion = calculateEvasionBonus(getValue(), getBaseValue());
        int futureEvasion = calculateEvasionBonus(futureValue, getBaseValue());
        int evasionIncrease = futureEvasion - currentEvasion;

        return List.of(
                Text.literal("LUCK").formatted(Formatting.WHITE, Formatting.BOLD)
                        .append(Text.literal(" " + getValue()).formatted(Formatting.GRAY))
                        .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(String.valueOf(futureValue)).formatted(Formatting.WHITE)),

                Text.literal("Cost: ").formatted(Formatting.GRAY)
                        .append(Text.literal(getIncreasePerPoint() * additionalPoints + " Stat Points").formatted(Formatting.YELLOW)),

                Text.literal(""),

                Text.literal("Primary Effects").formatted(Formatting.GOLD),
                Text.literal("  Critical Chance: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.2f%%", critChanceIncrease)).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%.2f%% → %.2f%%)", currentCritChance, futureCritChance)).formatted(Formatting.DARK_GRAY)),

                Text.literal(""),

                Text.literal("Secondary Effects").formatted(Formatting.GOLD),
                Text.literal("  Magic Damage: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.1f%%", magicDamageIncrease)).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentMagicDamage, futureMagicDamage)).formatted(Formatting.DARK_GRAY)),

                Text.literal("  Attack Speed: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.1f%%", attackSpeedIncrease)).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentAttackSpeed, futureAttackSpeed)).formatted(Formatting.DARK_GRAY)),

                Text.literal("  Accuracy: ").formatted(Formatting.GRAY)
                        .append(Text.literal("+" + accuracyIncrease).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%d → %d)", currentAccuracy, futureAccuracy)).formatted(Formatting.DARK_GRAY)),

                Text.literal("  Evasion: ").formatted(Formatting.GRAY)
                        .append(Text.literal("+" + evasionIncrease).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%d → %d)", currentEvasion, futureEvasion)).formatted(Formatting.DARK_GRAY))
        );
    }

    public static double calculateCritChanceBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * CRIT_CHANCE_SCALING;
    }

    public static double calculateMagicDamageBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * MAGIC_DAMAGE_SCALING;
    }

    public static double calculateAttackSpeedBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * ATTACK_SPEED_SCALING;
    }

    public static int calculateAccuracyBonus(int currentValue, int baseValue) {
        return Math.max(0, (currentValue - baseValue) / 3);
    }

    public static int calculateEvasionBonus(int currentValue, int baseValue) {
        return Math.max(0, (currentValue - baseValue) / 5);
    }

    @Override
    protected Identifier getPrimaryId() {
        return Mamy.id("luck_primary");
    }

    @Override
    protected Identifier getSecondaryId() {
        return Mamy.id("luck_secondary");
    }
}
