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
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.ArrayList;
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
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                baseValue -> MAX_HEALTH_SCALING * this.getEffective()
        );
    }

    @Override
    public void applySecondaryEffect(LivingEntity living) {
        List<AttributeModification> modifications = List.of(
                AttributeModification.addValue(
                        ModEntityAttributes.HEALTH_REGEN,
                        getSecondaryId(),
                        baseValue -> (HEALTH_REGEN_SCALING * this.getEffective())
                ),
                AttributeModification.addValue(
                        ModEntityAttributes.HEAL_EFFECTIVE,
                        getSecondaryId(),
                        baseValue -> (HEALTH_EFFECTIVE_SCALING * this.getEffective())
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

        double currentMaxHealth = currentTotal * MAX_HEALTH_SCALING * 100;
        double futureMaxHealth = futureTotal * MAX_HEALTH_SCALING * 100;
        double maxHealthIncrease = futureMaxHealth - currentMaxHealth;

        double currentHealthRegen = currentTotal * HEALTH_REGEN_SCALING * 100;
        double futureHealthRegen = futureTotal * HEALTH_REGEN_SCALING * 100;
        double healthRegenIncrease = futureHealthRegen - currentHealthRegen;

        double currentHealEffective = currentTotal * HEALTH_EFFECTIVE_SCALING * 100;
        double futureHealEffective = futureTotal * HEALTH_EFFECTIVE_SCALING * 100;
        double healEffectiveIncrease = futureHealEffective - currentHealEffective;

        List<Text> description = new ArrayList<>();

        if (currentClassBonus > 0) {
            description.add(Text.literal("VITALITY").formatted(Formatting.WHITE, Formatting.BOLD)
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
            description.add(Text.literal("VITALITY").formatted(Formatting.WHITE, Formatting.BOLD)
                    .append(Text.literal(" " + currentTotal).formatted(Formatting.WHITE))
                    .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal(String.valueOf(futureTotal)).formatted(Formatting.GREEN)));
        }

        description.add(Text.literal(""));
        description.add(Text.literal("Primary Effects").formatted(Formatting.GOLD));
        description.add(Text.literal("  Max Health: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("+%.1f%%", maxHealthIncrease)).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentMaxHealth, futureMaxHealth)).formatted(Formatting.DARK_GRAY)));

        description.add(Text.literal(""));
        description.add(Text.literal("Secondary Effects").formatted(Formatting.GOLD));
        description.add(Text.literal("  Health Regen: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("+%.1f%%", healthRegenIncrease)).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentHealthRegen, futureHealthRegen)).formatted(Formatting.DARK_GRAY)));
        description.add(Text.literal("  Heal Effective: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("+%.1f%%", healEffectiveIncrease)).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%.1f%% → %.1f%%)", currentHealEffective, futureHealEffective)).formatted(Formatting.DARK_GRAY)));

        return description;
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
