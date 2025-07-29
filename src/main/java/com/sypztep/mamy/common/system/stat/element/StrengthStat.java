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

import java.util.List;

public final class StrengthStat extends Stat {
    // Static constants for clean calculations
    private static final double MELEE_DAMAGE_SCALING = 0.05; // 5% per point
    private static final double CRIT_CHANCE_SCALING = 0.005; // 0.5% per point
    private static final double ATTACK_SPEED_SCALING = 0.002; // 0.2% per point

    public StrengthStat(short baseValue) {
        super(baseValue);
    }

    @Override
    public void applyPrimaryEffect(LivingEntity living) {
        applyEffect(living,
                ModEntityAttributes.MELEE_ATTACK_DAMAGE,
                getPrimaryId(),
                baseValue -> (MELEE_DAMAGE_SCALING * this.currentValue)
        );
    }

    @Override
    public void applySecondaryEffect(LivingEntity living) {
        List<AttributeModification> modifications = List.of(
                AttributeModification.addValue(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        getSecondaryId(),
                        baseValue -> (ATTACK_SPEED_SCALING * this.currentValue)
                )
        );
        applyEffects(living, modifications);
    }

    // ✅ Rich tooltip like Dominatus
    @Override
    public List<Text> getEffectDescription(int additionalPoints) {
        int futureValue = getValue() + additionalPoints;

        double currentMeleeDamage = Math.max(0, calculateMeleeDamageBonus(getValue(), getBaseValue()));
        double futureMeleeDamage = Math.max(0, calculateMeleeDamageBonus(futureValue, getBaseValue()));
        double meleeDamageIncrease = futureMeleeDamage - currentMeleeDamage;


        double currentAttackSpeed = Math.max(0, calculateAttackSpeedBonus(getValue(), getBaseValue()) * 100);
        double futureAttackSpeed = Math.max(0, calculateAttackSpeedBonus(futureValue, getBaseValue()) * 100);
        double attackSpeedIncrease = futureAttackSpeed - currentAttackSpeed;

        return List.of(
                Text.literal("STRENGTH").formatted(Formatting.WHITE, Formatting.BOLD)
                        .append(Text.literal(" " + getValue()).formatted(Formatting.GRAY))
                        .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(String.valueOf(futureValue)).formatted(Formatting.WHITE)),

                Text.literal("Cost: ").formatted(Formatting.GRAY)
                        .append(Text.literal(getIncreasePerPoint() * additionalPoints + " Stat Points").formatted(Formatting.YELLOW)),

                Text.literal(""),

                Text.literal("Primary Effects").formatted(Formatting.GOLD),
                Text.literal("  Melee Damage: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.1f%%", meleeDamageIncrease)).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentMeleeDamage, futureMeleeDamage)).formatted(Formatting.DARK_GRAY)),

                Text.literal(""),

                Text.literal("Secondary Effects").formatted(Formatting.GOLD),

                Text.literal("  Attack Speed: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.1f%%", attackSpeedIncrease)).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentAttackSpeed, futureAttackSpeed)).formatted(Formatting.DARK_GRAY))
        );
    }

    public static double calculateMeleeDamageBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * MELEE_DAMAGE_SCALING;
    }

    public static double calculateAttackSpeedBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * ATTACK_SPEED_SCALING;
    }

    @Override
    protected Identifier getPrimaryId() {
        return Mamy.id("strength_primary");
    }

    @Override
    protected Identifier getSecondaryId() {
        return Mamy.id("strength_secondary");
    }
}