package com.sypztep.mamy.common.system.classkill.acolyte.passive;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.PassiveSkill;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class DivineProtectionPassiveSkill extends PassiveSkill {

    public DivineProtectionPassiveSkill(Identifier id) {
        super(
                id,
                "Divine Protection",
                "Divine blessing reduces damage from undead and demon creatures.",
                10,
                Mamy.id("skill/divine_protection")
        );
    }

    @Override
    protected void initializePassiveEffects() {
    }

    @Override
    public void applyPassiveEffects(PlayerEntity player, int skillLevel) {
        super.applyPassiveEffects(player, skillLevel);
    }

    public static float calculateDamageReduction(PlayerEntity player, int skillLevel) {
        if (skillLevel <= 0) return 0;

        int baseLevel = ModEntityComponents.LIVINGLEVEL.get(player).getLevel();
        return (1f * skillLevel) + (0.04f * (baseLevel + 1));
    }

    @Override
    protected void populatePassiveTooltipData(SkillTooltipData data, PlayerEntity player, int skillLevel) {
        if (skillLevel > 0) {
            int baseLevel = ModEntityComponents.LIVINGLEVEL.get(player).getLevel();
            float reduction = (1f * skillLevel) + (0.04f * (baseLevel + 1));
            
            data.additionalEffects.add("Damage Reduction vs Undead/Demons: +" + String.format("%.1f", reduction));
            data.additionalEffects.add("(Scales with Base Level)");
        }

        if (skillLevel < getMaxSkillLevel()) {
            int baseLevel = ModEntityComponents.LIVINGLEVEL.get(player).getLevel();
            float nextReduction = (1f * (skillLevel + 1)) + (0.04f * (baseLevel + 1));
            data.additionalEffects.add("Next Level - Damage Reduction: +" + String.format("%.1f", nextReduction));
        }

        // Set special description for passive skills
        data.specialDescription = "Divine blessing protects against unholy creatures.";
        
        // Context-sensitive tip for learning screen
        data.contextTip = "Passive protection that grows stronger with your base level";
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ACOLYTE;
    }
}