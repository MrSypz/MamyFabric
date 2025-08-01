package com.sypztep.mamy.common.system.stat.element;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.stat.Stat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import sypztep.tyrannus.common.util.AttributeModification;

import java.util.List;

public final class VitalityStat extends Stat {
    private static final double MAX_HEALTH_SCALING = 0.01; // 1% per point
    private static final double HEALTH_REGEN_SCALING = 0.02; // 2% per point
    private static final double HEALTH_EFFECTIVE_SCALING = 0.02; // 2% per point

    public VitalityStat(short baseValue) {
        super(baseValue);
    }


    @Override
    public void applyPrimaryEffect(LivingEntity living) {
        applyEffect(
                living,
                EntityAttributes.GENERIC_MAX_HEALTH,
                getPrimaryId(),
                baseValue -> baseValue * (MAX_HEALTH_SCALING * this.currentValue)
        );
    }

    @Override
    public void applySecondaryEffect(LivingEntity living) {
        List<AttributeModification> modifications = List.of(
                AttributeModification.addValue(
                        ModEntityAttributes.HEALTH_REGEN,
                        getSecondaryId(),
                        baseValue -> (HEALTH_REGEN_SCALING * this.currentValue)
                ),
                AttributeModification.addValue(
                        ModEntityAttributes.HEAL_EFFECTIVE,
                        getSecondaryId(),
                        baseValue -> (HEALTH_EFFECTIVE_SCALING * this.currentValue)
                )
        );
        applyEffects(living, modifications);
    }

    @Override
    public List<Text> getEffectDescription(int additionalPoints) {
        int futureValue = getValue() + additionalPoints;

        double currentMaxHealth = calculateMaxHealthBonus(getValue(), getBaseValue()) * 100;
        double futureMaxHealth = calculateMaxHealthBonus(futureValue, getBaseValue()) * 100;
        double maxHealthIncrease = futureMaxHealth - currentMaxHealth;

        double currentHealthRegen = calculateHealthRegenBonus(getValue(), getBaseValue()) * 100;
        double futureHealthRegen = calculateHealthRegenBonus(futureValue, getBaseValue()) * 100;
        double healthRegenIncrease = futureHealthRegen - currentHealthRegen;

        double currentHealEffective = calculateHealEffectiveBonus(getValue(), getBaseValue()) * 100;
        double futureHealEffective = calculateHealEffectiveBonus(futureValue, getBaseValue()) * 100;
        double healEffectiveIncrease = futureHealEffective - currentHealEffective;

        return List.of(
                Text.literal("VITALITY").formatted(Formatting.WHITE, Formatting.BOLD)
                        .append(Text.literal(" " + getValue()).formatted(Formatting.GRAY))
                        .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(String.valueOf(futureValue)).formatted(Formatting.WHITE)),

                Text.literal("Cost: ").formatted(Formatting.GRAY)
                        .append(Text.literal(getIncreasePerPoint() * additionalPoints + " Stat Points").formatted(Formatting.YELLOW)),

                Text.literal(""),

                Text.literal("Primary Effects").formatted(Formatting.GOLD),
                Text.literal("  Max Health: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.1f%%", maxHealthIncrease)).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentMaxHealth, futureMaxHealth)).formatted(Formatting.DARK_GRAY)),

                Text.literal(""),

                Text.literal("Secondary Effects").formatted(Formatting.GOLD),
                Text.literal("  Health Regen: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.1f%%", healthRegenIncrease)).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentHealthRegen, futureHealthRegen)).formatted(Formatting.DARK_GRAY)),

                Text.literal("  Heal Effective: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.1f%%", healEffectiveIncrease)).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentHealEffective, futureHealEffective)).formatted(Formatting.DARK_GRAY))
        );
    }

    public static double calculateMaxHealthBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * MAX_HEALTH_SCALING;
    }

    public static double calculateHealthRegenBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * HEALTH_REGEN_SCALING;
    }

    public static double calculateHealEffectiveBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * HEALTH_EFFECTIVE_SCALING;
    }

    @Override
    protected Identifier getPrimaryId() {
        return Mamy.id("vitality_primary");
    }

    @Override
    protected Identifier getSecondaryId() {
        return Mamy.id("vitality_secondary");
    }
}
