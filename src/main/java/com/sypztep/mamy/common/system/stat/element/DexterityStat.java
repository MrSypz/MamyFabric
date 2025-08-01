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

public final class DexterityStat extends Stat {
    private static final double PROJECTILE_DAMAGE_SCALING = 0.02; // 2% per point
    private static final double ATTACK_SPEED_SCALING = 0.005; // 0.5% per point

    public DexterityStat(short baseValue) {
        super(baseValue);
    }

    @Override
    public void applyPrimaryEffect(LivingEntity living) {
        applyEffect(living,
                ModEntityAttributes.ACCURACY,
                getPrimaryId(),
                baseValue -> (double)(currentValue - this.baseValue)
        );
    }

    @Override
    public void applySecondaryEffect(LivingEntity living) {
        List<AttributeModification> modifications = List.of(
                AttributeModification.addValue(
                        ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE,
                        getSecondaryId(),
                        baseValue -> (PROJECTILE_DAMAGE_SCALING * this.currentValue)
                ),
                AttributeModification.addValue(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        getSecondaryId(),
                        baseValue -> (ATTACK_SPEED_SCALING * this.currentValue)
                )
        );
        applyEffects(living, modifications);
    }

    @Override
    public List<Text> getEffectDescription(int additionalPoints) {
        int futureValue = getValue() + additionalPoints;

        int currentAccuracy = Math.max(0, calculateAccuracyBonus(getValue(), getBaseValue()));
        int futureAccuracy = Math.max(0, calculateAccuracyBonus(futureValue, getBaseValue()));
        int accuracyIncrease = futureAccuracy - currentAccuracy;

        double currentProjectileDamage = Math.max(0, calculateProjectileDamageBonus(getValue(), getBaseValue()) * 100);
        double futureProjectileDamage = Math.max(0, calculateProjectileDamageBonus(futureValue, getBaseValue()) * 100);
        double projectileDamageIncrease = futureProjectileDamage - currentProjectileDamage;

        double currentAttackSpeed = Math.max(0, calculateAttackSpeedBonus(getValue(), getBaseValue()) * 100);
        double futureAttackSpeed = Math.max(0, calculateAttackSpeedBonus(futureValue, getBaseValue()) * 100);
        double attackSpeedIncrease = futureAttackSpeed - currentAttackSpeed;

        return List.of(
                Text.literal("DEXTERITY").formatted(Formatting.WHITE, Formatting.BOLD)
                        .append(Text.literal(" " + getValue()).formatted(Formatting.GRAY))
                        .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(String.valueOf(futureValue)).formatted(Formatting.WHITE)),

                Text.literal("Cost: ").formatted(Formatting.GRAY)
                        .append(Text.literal(getIncreasePerPoint() * additionalPoints + " Stat Points").formatted(Formatting.YELLOW)),

                Text.literal(""),

                Text.literal("Primary Effects").formatted(Formatting.GOLD),
                Text.literal("  Accuracy: ").formatted(Formatting.GRAY)
                        .append(Text.literal("+" + accuracyIncrease).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%d → %d)", currentAccuracy, futureAccuracy)).formatted(Formatting.DARK_GRAY)),

                Text.literal(""),

                Text.literal("Secondary Effects").formatted(Formatting.GOLD),
                Text.literal("  Projectile Damage: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.1f%%", projectileDamageIncrease)).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentProjectileDamage, futureProjectileDamage)).formatted(Formatting.DARK_GRAY)),

                Text.literal("  Attack Speed: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.1f%%", attackSpeedIncrease)).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentAttackSpeed, futureAttackSpeed)).formatted(Formatting.DARK_GRAY))
        );
    }

    public static int calculateAccuracyBonus(int currentValue, int baseValue) {
        return currentValue - baseValue;
    }

    public static double calculateProjectileDamageBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * PROJECTILE_DAMAGE_SCALING;
    }

    public static double calculateAttackSpeedBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * ATTACK_SPEED_SCALING;
    }

    @Override
    protected Identifier getPrimaryId() {
        return Mamy.id("dexterity_primary");
    }

    @Override
    protected Identifier getSecondaryId() {
        return Mamy.id("dexterity_secondary");
    }
}
