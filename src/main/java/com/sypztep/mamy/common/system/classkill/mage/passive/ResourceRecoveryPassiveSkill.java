package com.sypztep.mamy.common.system.classkill.mage.passive;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.PassiveSkill;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class ResourceRecoveryPassiveSkill extends PassiveSkill {

    public ResourceRecoveryPassiveSkill(Identifier id) {
        super(id, "Resource Recovery", "Enhanced resource regeneration based on your maximum resource and meditation training.",
                ModClasses.MAGE, 1, 1, 10, false, Mamy.id("skill/increase_resource_recovery"));
    }

    @Override
    protected void initializePassiveEffects() {
    }

    @Override
    public void applyPassiveEffects(PlayerEntity player, int skillLevel) {
        super.applyPassiveEffects(player, skillLevel);

        double maxResource = player.getAttributeValue(ModEntityAttributes.RESOURCE);
        double baseRegen = 3.0 * skillLevel;
        double percentageRegen = (0.002 * skillLevel * maxResource); // 0.2% per level
        double totalRegen = baseRegen + percentageRegen;

        EntityAttributeModifier modifier = new EntityAttributeModifier(
                Mamy.id("resource_recovery_passive"),
                totalRegen,
                EntityAttributeModifier.Operation.ADD_VALUE
        );

        if (player.getAttributeInstance(ModEntityAttributes.PASSIVE_RESOURCE_REGEN) != null) {
            player.getAttributeInstance(ModEntityAttributes.PASSIVE_RESOURCE_REGEN).removeModifier(Mamy.id("resource_recovery_passive"));
            player.getAttributeInstance(ModEntityAttributes.PASSIVE_RESOURCE_REGEN).addPersistentModifier(modifier);
        }
    }

    @Override
    public void removePassiveEffects(PlayerEntity player) {
        super.removePassiveEffects(player);

        if (player.getAttributeInstance(ModEntityAttributes.PASSIVE_RESOURCE_REGEN) != null) {
            player.getAttributeInstance(ModEntityAttributes.PASSIVE_RESOURCE_REGEN).removeModifier(Mamy.id("resource_recovery_passive"));
        }
    }

    @Override
    protected void addPassiveEffectsDescription(List<Text> tooltip, int skillLevel) {
        tooltip.add(Text.literal("Enhanced resource regeneration through meditation and focus training.").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Restores SP every 10 seconds when idle.").formatted(Formatting.GRAY));

        double baseRegen = 3 * skillLevel;
        double percentageRegen = 0.2 * skillLevel;

        tooltip.add(Text.literal("• Base Resource Regen: ").formatted(Formatting.GRAY)
                .append(Text.literal("+" + String.format("%.0f", baseRegen) + " SP").formatted(Formatting.BLUE)));
        tooltip.add(Text.literal("• Percentage Resource Regen: ").formatted(Formatting.GRAY)
                .append(Text.literal("+" + String.format("%.1f", percentageRegen) + "% of Max SP").formatted(Formatting.BLUE)));

        if (skillLevel < getMaxSkillLevel()) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Next Level:").formatted(Formatting.GOLD));
            double nextBaseRegen = 3 * (skillLevel + 1);
            double nextPercentageRegen = 0.2 * (skillLevel + 1);
            tooltip.add(Text.literal("• Base Resource Regen: ").formatted(Formatting.GRAY)
                    .append(Text.literal("+" + String.format("%.0f", nextBaseRegen) + " SP").formatted(Formatting.DARK_BLUE)));
            tooltip.add(Text.literal("• Percentage Resource Regen: ").formatted(Formatting.GRAY)
                    .append(Text.literal("+" + String.format("%.1f", nextPercentageRegen) + "% of Max SP").formatted(Formatting.DARK_BLUE)));
        }
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.MAGE;
    }
}