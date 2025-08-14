package com.sypztep.mamy.common.system.skill.swordman.passive;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.PassiveSkill;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.List;

public class SwordMasteryPassiveSkill extends PassiveSkill {

    public SwordMasteryPassiveSkill(Identifier id) {
        super(
                id,
                "Sword Mastery",
                "Extensive training with bladed weapons increases your melee damage.",
                ModClasses.SWORDMAN,
                0, // base cost to learn
                1, // upgrade cost per level
                10, // max level (10 levels = +15 damage at max)
                false, // not a default skill
                Mamy.id("skill/sword_mastery") // icon
        );
    }

    @Override
    protected void initializePassiveEffects() {
        addAttributeModification(
                AttributeModification.addValue(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT,
                        Mamy.id("sword_mastery_damage"),
                        skillLevel -> skillLevel * 1.5 // +1.5 damage per level
                )
        );
    }

    @Override
    protected void addPassiveEffectsDescription(List<Text> tooltip, int skillLevel) {
        tooltip.add(Text.literal("Extensive training with bladed weapons increases your melee damage.").formatted(Formatting.GRAY));

        // Show current bonus
        double damageBonus = skillLevel * 1.5D;
        tooltip.add(Text.literal("• Melee Attack Damage: ").formatted(Formatting.GRAY)
                .append(Text.literal("+" + String.format("%.0f", damageBonus)).formatted(Formatting.YELLOW)));

        // Show next level preview if not at max
        if (skillLevel < getMaxSkillLevel()) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Next Level:").formatted(Formatting.GOLD));
            double nextDamageBonus = (skillLevel + 1) * 1.5D;
            tooltip.add(Text.literal("• Melee Attack Damage: ").formatted(Formatting.GRAY)
                    .append(Text.literal("+" + String.format("%.0f", nextDamageBonus)).formatted(Formatting.DARK_GREEN)));
        }
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
}