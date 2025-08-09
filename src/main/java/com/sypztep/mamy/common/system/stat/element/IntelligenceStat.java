package com.sypztep.mamy.common.system.stat.element;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.stat.Stat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import sypztep.tyrannus.common.util.AttributeModification;

import java.util.ArrayList;
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
                baseValue -> MAGIC_DAMAGE_SCALING * this.getEffective()
        );
    }

    @Override
    public void applySecondaryEffect(LivingEntity living) {
        List<AttributeModification> modifications = List.of(
                AttributeModification.addValue(
                        ModEntityAttributes.MAGIC_RESISTANCE,
                        getSecondaryId(),
                        baseValue -> MAGIC_RESISTANCE_SCALING * this.getEffective()
                ),
                AttributeModification.addMultiply(
                        ModEntityAttributes.RESOURCE,
                        getSecondaryId(),
                        baseValue -> this.getEffective() * RESOURCE_SCALING
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

        double currentMagicDamage = currentTotal * MAGIC_DAMAGE_SCALING * 100;
        double futureMagicDamage = futureTotal * MAGIC_DAMAGE_SCALING * 100;
        double magicDamageIncrease = futureMagicDamage - currentMagicDamage;

        double currentMagicRes = currentTotal * MAGIC_RESISTANCE_SCALING * 100;
        double futureMagicRes = futureTotal * MAGIC_RESISTANCE_SCALING * 100;
        double magicResIncrease = futureMagicRes - currentMagicRes;

        double currentResourceBonus = currentTotal * RESOURCE_SCALING * 100;
        double futureResourceBonus = futureTotal * RESOURCE_SCALING * 100;
        double resourceIncrease = futureResourceBonus - currentResourceBonus;

        List<Text> description = new ArrayList<>();

        if (currentClassBonus > 0) {
            description.add(Text.literal("INTELLIGENCE").formatted(Formatting.WHITE, Formatting.BOLD)
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
            description.add(Text.literal("INTELLIGENCE").formatted(Formatting.WHITE, Formatting.BOLD)
                    .append(Text.literal(" " + currentTotal).formatted(Formatting.WHITE))
                    .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal(String.valueOf(futureTotal)).formatted(Formatting.GREEN)));
        }

        description.add(Text.literal(""));
        description.add(Text.literal("Primary Effects").formatted(Formatting.GOLD));
        description.add(Text.literal("  Magic Damage: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("+%.1f%%", magicDamageIncrease)).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentMagicDamage, futureMagicDamage)).formatted(Formatting.DARK_GRAY)));

        description.add(Text.literal(""));
        description.add(Text.literal("Secondary Effects").formatted(Formatting.GOLD));
        description.add(Text.literal("  Magic Resistance: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("+%.1f%%", magicResIncrease)).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentMagicRes, futureMagicRes)).formatted(Formatting.DARK_GRAY)));
        description.add(Text.literal("  Max Resource: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("+%.1f%%", resourceIncrease)).formatted(Formatting.AQUA))
                .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentResourceBonus, futureResourceBonus)).formatted(Formatting.DARK_GRAY)));

        return description;
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
