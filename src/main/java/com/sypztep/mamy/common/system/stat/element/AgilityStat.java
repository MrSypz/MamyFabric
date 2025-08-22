package com.sypztep.mamy.common.system.stat.element;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.stat.Stat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class AgilityStat extends Stat {
    private static final double ATTACK_SPEED_SCALING = 0.01; // 1% per point

    public AgilityStat(short baseValue) {
        super(baseValue);
    }

    @Override
    public void applyPrimaryEffect(LivingEntity living) {
        applyEffect(living, EntityAttributes.GENERIC_ATTACK_SPEED,
                getPrimaryId(),
                baseValue -> (ATTACK_SPEED_SCALING * this.getEffective()));
    }

    @Override
    public void applySecondaryEffect(LivingEntity living) {
        applyEffect(living,
                ModEntityAttributes.EVASION,
                getSecondaryId(),
                baseValue -> (double) this.getEffective()
        );
    }

    @Override
    public List<Text> getEffectDescription(int additionalPoints) {
        int currentPlayerStat = getCurrentValue(); // Player-invested points
        int currentClassBonus = getClassBonus(); // Class bonus
        int currentTotal = getEffective(); // Total effective

        int futurePlayerStat = currentPlayerStat + additionalPoints;
        int futureTotal = futurePlayerStat + currentClassBonus;

        double currentAttackSpeed = Math.max(0, (currentTotal * ATTACK_SPEED_SCALING) * 100);
        double futureAttackSpeed = Math.max(0, (futureTotal * ATTACK_SPEED_SCALING) * 100);
        double attackSpeedIncrease = futureAttackSpeed - currentAttackSpeed;

        int currentEvasion = Math.max(0, currentTotal); // Uses total effective (matches applySecondaryEffect)
        int futureEvasion = Math.max(0, futureTotal);
        int evasionIncrease = futureEvasion - currentEvasion;

        List<Text> description = new ArrayList<>();

        // Title with breakdown
        if (currentClassBonus > 0) {
            description.add(Text.literal("AGILITY").formatted(Formatting.WHITE, Formatting.BOLD)
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
            description.add(Text.literal("AGILITY").formatted(Formatting.WHITE, Formatting.BOLD)
                    .append(Text.literal(" " + currentTotal).formatted(Formatting.WHITE))
                    .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal(String.valueOf(futureTotal)).formatted(Formatting.GREEN)));
        }

        // Primary Effects (uses total effective value)
        description.add(Text.literal("Primary Effects").formatted(Formatting.GOLD));
        description.add(Text.literal("  Attack Speed: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("+%.1f%%", attackSpeedIncrease)).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentAttackSpeed, futureAttackSpeed)).formatted(Formatting.DARK_GRAY)));

        // Secondary Effects (uses total effective value to match applySecondaryEffect)
        description.add(Text.literal("Secondary Effects").formatted(Formatting.GOLD));
        description.add(Text.literal("  Evasion: ").formatted(Formatting.GRAY)
                .append(Text.literal("+" + evasionIncrease).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%d → %d)", currentEvasion, futureEvasion)).formatted(Formatting.DARK_GRAY)));

        return description;
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