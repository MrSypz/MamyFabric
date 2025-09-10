package com.sypztep.mamy.common.system.classkill.swordman.passive;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.PassiveSkill;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class HPRecoveryPassiveSkill extends PassiveSkill {
    private final Identifier HPRECOVERY = Mamy.id("hp_recovery_passive");

    public HPRecoveryPassiveSkill(Identifier id) {
        super(id, "HP Recovery", "Enhanced health regeneration based on your maximum health and training level.",
                ModClasses.SWORDMAN, 1, 1, 10, false, Mamy.id("skill/hp_recovery"));
    }

    @Override
    protected void initializePassiveEffects() {
    }

    @Override
    public void applyPassiveEffects(PlayerEntity player, int skillLevel) {
        super.applyPassiveEffects(player, skillLevel);

        double maxHP = player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
        double baseRegen = 5.0 * skillLevel;
        double percentageRegen = (0.002 * skillLevel * maxHP); // 0.2% per level
        double totalRegen = baseRegen + percentageRegen;

        EntityAttributeModifier modifier = new EntityAttributeModifier(HPRECOVERY,totalRegen,EntityAttributeModifier.Operation.ADD_VALUE);

        if (player.getAttributeInstance(ModEntityAttributes.PASSIVE_HEALTH_REGEN) != null) {
            player.getAttributeInstance(ModEntityAttributes.PASSIVE_HEALTH_REGEN).removeModifier(HPRECOVERY);
            player.getAttributeInstance(ModEntityAttributes.PASSIVE_HEALTH_REGEN).addPersistentModifier(modifier);
        }
    }

    @Override
    public void removePassiveEffects(PlayerEntity player) {
        super.removePassiveEffects(player);

        if (player.getAttributeInstance(ModEntityAttributes.HEALTH_REGEN) != null) {
            player.getAttributeInstance(ModEntityAttributes.HEALTH_REGEN).removeModifier(HPRECOVERY);
        }
    }

    @Override
    protected void addPassiveEffectsDescription(List<Text> tooltip, int skillLevel) {
        tooltip.add(Text.literal("Enhanced health regeneration through training and conditioning.").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Restores HP every 10 seconds when idle.").formatted(Formatting.GRAY));

        double baseRegen = 5 * skillLevel;
        double percentageRegen = 0.2 * skillLevel;

        tooltip.add(Text.literal("• Base Health Regen: ").formatted(Formatting.GRAY)
                .append(Text.literal("+" + String.format("%.0f", baseRegen) + " HP").formatted(Formatting.GREEN)));
        tooltip.add(Text.literal("• Percentage Health Regen: ").formatted(Formatting.GRAY)
                .append(Text.literal("+" + String.format("%.1f", percentageRegen) + "% of Max HP").formatted(Formatting.GREEN)));

        if (skillLevel < getMaxSkillLevel()) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Next Level:").formatted(Formatting.GOLD));
            double nextBaseRegen = 5 * (skillLevel + 1);
            double nextPercentageRegen = 0.2 * (skillLevel + 1);
            tooltip.add(Text.literal("• Base Health Regen: ").formatted(Formatting.GRAY)
                    .append(Text.literal("+" + String.format("%.0f", nextBaseRegen) + " HP").formatted(Formatting.DARK_GREEN)));
            tooltip.add(Text.literal("• Percentage Health Regen: ").formatted(Formatting.GRAY)
                    .append(Text.literal("+" + String.format("%.1f", nextPercentageRegen) + "% of Max HP").formatted(Formatting.DARK_GREEN)));
        }
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
}
