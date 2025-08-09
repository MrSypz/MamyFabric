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
                baseValue -> (CRIT_CHANCE_SCALING * this.getEffective())
        );
    }

    @Override
    public void applySecondaryEffect(LivingEntity living) {
        List<AttributeModification> modifications = List.of(
                AttributeModification.addValue(
                        ModEntityAttributes.MAGIC_ATTACK_DAMAGE,
                        getSecondaryId(),
                        baseValue -> (MAGIC_DAMAGE_SCALING * this.getEffective())
                ),
                AttributeModification.addValue(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        getSecondaryId(),
                        baseValue -> (ATTACK_SPEED_SCALING * this.getEffective())
                )
        );
        applyEffects(living, modifications);

        int statPoints = getEffective();
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
        int currentPlayerStat = getValue();
        int currentClassBonus = getClassBonus();
        int currentTotal = getEffective();

        int futurePlayerStat = currentPlayerStat + additionalPoints;
        int futureTotal = futurePlayerStat + currentClassBonus;

        double currentCritChance = currentTotal * CRIT_CHANCE_SCALING * 100;
        double futureCritChance = futureTotal * CRIT_CHANCE_SCALING * 100;
        double critChanceIncrease = futureCritChance - currentCritChance;

        double currentMagicDamage = currentTotal * MAGIC_DAMAGE_SCALING * 100;
        double futureMagicDamage = futureTotal * MAGIC_DAMAGE_SCALING * 100;
        double magicDamageIncrease = futureMagicDamage - currentMagicDamage;

        double currentAttackSpeed = currentTotal * ATTACK_SPEED_SCALING * 100;
        double futureAttackSpeed = futureTotal * ATTACK_SPEED_SCALING * 100;
        double attackSpeedIncrease = futureAttackSpeed - currentAttackSpeed;

        int currentAccuracy = Math.max(0, currentTotal / 3);
        int futureAccuracy = Math.max(0, futureTotal / 3);
        int accuracyIncrease = futureAccuracy - currentAccuracy;

        int currentEvasion = Math.max(0, currentTotal / 5);
        int futureEvasion = Math.max(0, futureTotal / 5);
        int evasionIncrease = futureEvasion - currentEvasion;

        List<Text> description = new ArrayList<>();

        if (currentClassBonus > 0) {
            description.add(Text.literal("LUCK").formatted(Formatting.WHITE, Formatting.BOLD)
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
            description.add(Text.literal("LUCK").formatted(Formatting.WHITE, Formatting.BOLD)
                    .append(Text.literal(" " + currentTotal).formatted(Formatting.WHITE))
                    .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal(String.valueOf(futureTotal)).formatted(Formatting.GREEN)));
        }

        description.add(Text.literal(""));
        description.add(Text.literal("Primary Effects").formatted(Formatting.GOLD));
        description.add(Text.literal("  Critical Chance: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("+%.2f%%", critChanceIncrease)).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%.2f%% → %.2f%%)", currentCritChance, futureCritChance)).formatted(Formatting.DARK_GRAY)));

        description.add(Text.literal(""));
        description.add(Text.literal("Secondary Effects").formatted(Formatting.GOLD));
        description.add(Text.literal("  Magic Damage: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("+%.1f%%", magicDamageIncrease)).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentMagicDamage, futureMagicDamage)).formatted(Formatting.DARK_GRAY)));
        description.add(Text.literal("  Attack Speed: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("+%.1f%%", attackSpeedIncrease)).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentAttackSpeed, futureAttackSpeed)).formatted(Formatting.DARK_GRAY)));
        description.add(Text.literal("  Accuracy: ").formatted(Formatting.GRAY)
                .append(Text.literal("+" + accuracyIncrease).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%d → %d)", currentAccuracy, futureAccuracy)).formatted(Formatting.DARK_GRAY)));
        description.add(Text.literal("  Evasion: ").formatted(Formatting.GRAY)
                .append(Text.literal("+" + evasionIncrease).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%d → %d)", currentEvasion, futureEvasion)).formatted(Formatting.DARK_GRAY)));

        return description;
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
