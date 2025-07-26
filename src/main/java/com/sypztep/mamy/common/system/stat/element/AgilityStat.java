package com.sypztep.mamy.common.system.stat.element;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.stat.Stat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public final class AgilityStat extends Stat {
    private static final double ATTACK_SPEED_SCALING = 0.01; // 1% per point

    public AgilityStat(short baseValue) {
        super(baseValue);
    }

    @Override
    public void applyPrimaryEffect(LivingEntity living) {
        applyEffect(living, EntityAttributes.GENERIC_ATTACK_SPEED, getPrimaryId(),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> (ATTACK_SPEED_SCALING * this.currentValue));
    }

    @Override
    public void applySecondaryEffect(LivingEntity living) {
        applyEffect(living,
                ModEntityAttributes.EVASION,
                getSecondaryId(),
                baseValue -> (double)(currentValue - this.baseValue)
        );
    }

    @Override
    public List<Text> getEffectDescription(int additionalPoints) {
        int futureValue = getValue() + additionalPoints;

        double currentAttackSpeed = Math.max(0, calculateAttackSpeedBonus(getValue(), getBaseValue()) * 100);
        double futureAttackSpeed = Math.max(0, calculateAttackSpeedBonus(futureValue, getBaseValue()) * 100);
        double attackSpeedIncrease = futureAttackSpeed - currentAttackSpeed;

        int currentEvasion = Math.max(0, calculateEvasionBonus(getValue(), getBaseValue()));
        int futureEvasion = Math.max(0, calculateEvasionBonus(futureValue, getBaseValue()));
        int evasionIncrease = futureEvasion - currentEvasion;

        return List.of(
                Text.literal("AGILITY").formatted(Formatting.WHITE, Formatting.BOLD)
                        .append(Text.literal(" " + getValue()).formatted(Formatting.GRAY))
                        .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(String.valueOf(futureValue)).formatted(Formatting.WHITE)),

                Text.literal("Cost: ").formatted(Formatting.GRAY)
                        .append(Text.literal(getIncreasePerPoint() * additionalPoints + " Stat Points").formatted(Formatting.YELLOW)),

                Text.literal(""),

                Text.literal("Primary Effects").formatted(Formatting.GOLD),
                Text.literal("  Attack Speed: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("+%.1f%%", attackSpeedIncrease)).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentAttackSpeed, futureAttackSpeed)).formatted(Formatting.DARK_GRAY)),

                Text.literal(""),

                Text.literal("Secondary Effects").formatted(Formatting.GOLD),
                Text.literal("  Evasion: ").formatted(Formatting.GRAY)
                        .append(Text.literal("+" + evasionIncrease).formatted(Formatting.GREEN))
                        .append(Text.literal(String.format(" (%d → %d)", currentEvasion, futureEvasion)).formatted(Formatting.DARK_GRAY))
        );
    }

    public static double calculateAttackSpeedBonus(int currentValue, int baseValue) {
        return (currentValue - baseValue) * ATTACK_SPEED_SCALING;
    }

    public static int calculateEvasionBonus(int currentValue, int baseValue) {
        return currentValue - baseValue;
    }

    @Override
    protected Identifier getPrimaryId() {
        return Mamy.id("agility_primary");
    }

    @Override
    protected Identifier getSecondaryId() {
        return Mamy.id("agility_secondary");
    }
}

