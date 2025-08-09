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

import java.util.ArrayList;
import java.util.List;

public final class StrengthStat extends Stat {
    // Static constants for clean calculations
    private static final double MELEE_DAMAGE_SCALING = 0.05; // 5% per point
    private static final double ATTACK_SPEED_SCALING = 0.002; // 0.2% per point

    public StrengthStat(short baseValue) {
        super(baseValue);
    }

    @Override
    public void applyPrimaryEffect(LivingEntity living) {
        applyEffect(living,
                ModEntityAttributes.MELEE_ATTACK_DAMAGE,
                getPrimaryId(),
                baseValue -> (MELEE_DAMAGE_SCALING * this.getEffective())
        );
    }

    @Override
    public void applySecondaryEffect(LivingEntity living) {
        List<AttributeModification> modifications = List.of(
                AttributeModification.addValue(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        getSecondaryId(),
                        baseValue -> (ATTACK_SPEED_SCALING * this.getEffective())
                )
        );
        applyEffects(living, modifications);
    }

    @Override
    public List<Text> getEffectDescription(int additionalPoints) {
        int currentPlayerStat = getValue();
        int currentClassBonus = getClassBonus();
        int currentTotal = getEffective();

        int futurePlayerStat = currentPlayerStat + additionalPoints;
        int futureTotal = futurePlayerStat + currentClassBonus;

        double currentMeleeDamage = Math.max(0, currentTotal * MELEE_DAMAGE_SCALING * 100);
        double futureMeleeDamage = Math.max(0, futureTotal * MELEE_DAMAGE_SCALING * 100);
        double meleeDamageIncrease = futureMeleeDamage - currentMeleeDamage;

        double currentAttackSpeed = Math.max(0, currentTotal * ATTACK_SPEED_SCALING * 100);
        double futureAttackSpeed = Math.max(0, futureTotal * ATTACK_SPEED_SCALING * 100);
        double attackSpeedIncrease = futureAttackSpeed - currentAttackSpeed;

        List<Text> description = new ArrayList<>();

        if (currentClassBonus > 0) {
            description.add(Text.literal("STRENGTH").formatted(Formatting.WHITE, Formatting.BOLD)
                    .append(Text.literal(" " + currentPlayerStat).formatted(Formatting.WHITE))
                    .append(Text.literal(" + ").formatted(Formatting.GRAY))
                    .append(Text.literal(String.valueOf(currentClassBonus)).formatted(Formatting.YELLOW))
                    .append(Text.literal(" = ").formatted(Formatting.GRAY))
                    .append(Text.literal(String.valueOf(currentTotal)).formatted(Formatting.GREEN))
                    .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal(String.valueOf(futurePlayerStat)).formatted(Formatting.WHITE))
                    .append(Text.literal(" + ").formatted(Formatting.GRAY))
                    .append(Text.literal(String.valueOf(currentClassBonus)).formatted(Formatting.YELLOW))
                    .append(Text.literal(" = ").formatted(Formatting.GRAY))
                    .append(Text.literal(String.valueOf(futureTotal)).formatted(Formatting.GREEN)));
            description.add(Text.literal("  Player: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.valueOf(currentPlayerStat)).formatted(Formatting.WHITE))
                    .append(Text.literal(" | Class Bonus: ").formatted(Formatting.GRAY))
                    .append(Text.literal(String.valueOf(currentClassBonus)).formatted(Formatting.YELLOW)));
        } else {
            description.add(Text.literal("STRENGTH").formatted(Formatting.WHITE, Formatting.BOLD)
                    .append(Text.literal(" " + currentTotal).formatted(Formatting.WHITE))
                    .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal(String.valueOf(futureTotal)).formatted(Formatting.GREEN)));
        }

        description.add(Text.literal(""));
        description.add(Text.literal("Primary Effects").formatted(Formatting.GOLD));
        description.add(Text.literal("  Melee Damage: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("+%.1f%%", meleeDamageIncrease)).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentMeleeDamage, futureMeleeDamage)).formatted(Formatting.DARK_GRAY)));

        description.add(Text.literal(""));
        description.add(Text.literal("Secondary Effects").formatted(Formatting.GOLD));
        description.add(Text.literal("  Attack Speed: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("+%.1f%%", attackSpeedIncrease)).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentAttackSpeed, futureAttackSpeed)).formatted(Formatting.DARK_GRAY)));

        return description;
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