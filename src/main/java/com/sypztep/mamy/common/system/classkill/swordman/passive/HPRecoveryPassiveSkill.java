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
                10,
                Mamy.id("skill/hp_recovery"));
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
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();
        
        // Configure for passive skills
        data.hideDamage = true;
        data.hideResourceCost = true;
        data.hideCooldown = true;
        
        // Add regeneration info as effects
        double baseRegen = 5 * skillLevel;
        double percentageRegen = 0.2 * skillLevel;
        
        data.effects.add("Base Health Regen: +" + String.format("%.0f", baseRegen) + " HP");
        data.effects.add("Percentage Health Regen: +" + String.format("%.1f", percentageRegen) + "% of Max HP");
        data.effects.add("Restores HP every 10 seconds when idle");
        
        if (skillLevel < getMaxSkillLevel()) {
            double nextBaseRegen = 5 * (skillLevel + 1);
            double nextPercentageRegen = 0.2 * (skillLevel + 1);
            data.tip = "Next level: +" + String.format("%.0f", nextBaseRegen) + " HP, " + 
                      String.format("%.1f", nextPercentageRegen) + "% of Max HP";
        }
        
        return data;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
}
