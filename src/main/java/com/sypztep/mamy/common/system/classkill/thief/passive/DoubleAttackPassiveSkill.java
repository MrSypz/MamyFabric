package com.sypztep.mamy.common.system.classkill.thief.passive;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.PassiveSkill;
import com.sypztep.mamy.common.util.AttributeModification;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class DoubleAttackPassiveSkill extends PassiveSkill {

    public DoubleAttackPassiveSkill(Identifier id) {
        super(
                id,
                "Double Attack",
                "Adds a high chance to deal double damage when attacking",
                ModClasses.SWORDMAN,
                0,
                1,
                10,
                false,
                Mamy.id("skill/passive/double_attack")
        );
    }

    @Override
    protected void initializePassiveEffects() {
        addAttributeModification(
                AttributeModification.addValue(
                        ModEntityAttributes.ACCURACY,
                        Mamy.id("double_attack_passive"),
                        skillLevel -> skillLevel * 2 // +2 accuracy per level accuracy 5%
                )
        );
        addAttributeModification(
                AttributeModification.addValue(
                        ModEntityAttributes.DOUBLE_ATTACK_CHANCE,
                        Mamy.id("double_attack_passive"),
                        skillLevel -> skillLevel * 0.07f // +7% per level
                )
        );
    }

    @Override
    protected void addPassiveEffectsDescription(List<Text> tooltip, int skillLevel) {
        tooltip.add(Text.literal("Gives the chance to inflict two hits instead of one and improves hit rate while attacking.").formatted(Formatting.GRAY));
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.THIEF;
    }
}