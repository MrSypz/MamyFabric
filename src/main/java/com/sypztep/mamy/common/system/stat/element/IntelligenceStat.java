package com.sypztep.mamy.common.system.stat.element;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.stat.Stat;
import com.sypztep.mamy.common.util.AttributeModification;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public final class IntelligenceStat extends Stat {
    private static final double MAGIC_DAMAGE_SCALING = 0.02; // 2% per point
    private static final double MAGIC_RESISTANCE_SCALING = 0.005; // 0.5% per point
    private static final double RESOURCE_SCALING = 0.01; // 1% per point


    public IntelligenceStat(short baseValue) {
        super(baseValue);
    }

    @Override
    public void applyPrimaryEffect(LivingEntity living) {
        applyEffect(
                living,
                ModEntityAttributes.MAGIC_ATTACK_DAMAGE,
                getPrimaryId(),
                baseValue -> MAGIC_DAMAGE_SCALING * this.currentValue
        );
    }

    @Override
    public void applySecondaryEffect(LivingEntity living) {
        List<AttributeModification> modifications = List.of(
                AttributeModification.addValue(
                        ModEntityAttributes.MAGIC_RESISTANCE,
                        getSecondaryId(),
                        baseValue -> MAGIC_RESISTANCE_SCALING * this.currentValue
                ),
                AttributeModification.addValue(
                        ModEntityAttributes.RESOURCE,
                        getSecondaryId(),
                        baseValue -> {
                            double intBonus = (this.currentValue - this.baseValue) * RESOURCE_SCALING;
                            return baseValue * intBonus;
                        }
                )
        );
        applyEffects(living, modifications);
    }

    @Override
    public List<Text> getEffectDescription(int additionalPoints) {
        int futureValue = getValue() + additionalPoints;

        double currentMagicDamage = calculateMagicDamageBonus(getValue(), getBaseValue()) * 100;
        double futureMagicDamage = calculateMagicDamageBonus(futureValue, getBaseValue()) * 100;
        double magicDamageIncrease = futureMagicDamage - currentMagicDamage;

        double currentMagicRes = calculateMagicResistanceBonus(getValue(), getBaseValue()) * 100;
        double futureMagicRes = calculateMagicResistanceBonus(futureValue, getBaseValue()) * 100;
        double magicResIncrease = futureMagicRes - currentMagicRes;

        double currentResourceBonus = calculateResourceBonus(getValue(), getBaseValue()) * 100;
        double futureResourceBonus = calculateResourceBonus(futureValue, getBaseValue()) * 100;
        double resourceIncrease = futureResourceBonus - currentResourceBonus;

        return List.of(
                Text.literal("INTELLIGENCE").formatted(Formatting.WHITE, Formatting.BOLD)
                        .append(Text.literal(" " + getValue()).formatted(Formatting.GRAY))
                        .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(String.valueOf(futureValue)).formatted(Formatting.WHITE)),

                Text.literal("Cost: ").formatted(Formatting.GRAY)
                        .append(Text.literal(getIncreasePerPoint() * additionalPoints + " Stat Points").formatted(Formatting.YELLOW)),

                Text.literal(""),

                Text.literal("Primary Effects").formatted(Formatting.GOLD),
                Text.literal("  Magic Damage: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.1f%%", magicDamageIncrease)).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentMagicDamage, futureMagicDamage)).formatted(Formatting.DARK_GRAY)),

                Text.literal(""),

                Text.literal("Secondary Effects").formatted(Formatting.GOLD),
                Text.literal("  Magic Resistance: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.1f%%", magicResIncrease)).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentMagicRes, futureMagicRes)).formatted(Formatting.DARK_GRAY)),

                Text.literal("  Max Resource: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.1f%%", resourceIncrease)).formatted(Formatting.AQUA))
                        .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentResourceBonus, futureResourceBonus)).formatted(Formatting.DARK_GRAY))
        );
    }

    public static double calculateMagicDamageBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * MAGIC_DAMAGE_SCALING;
    }

    public static double calculateMagicResistanceBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * MAGIC_RESISTANCE_SCALING;
    }

    // NEW: Resource bonus calculation
    public static double calculateResourceBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * RESOURCE_SCALING;
    }
    @Override
    protected Identifier getPrimaryId() {
        return Mamy.id("intelligence_primary");
    }

    @Override
    protected Identifier getSecondaryId() {
        return Mamy.id("intelligence_secondary");
    }
}
