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

public class ImproveDodgePassiveSkill extends PassiveSkill {

    public ImproveDodgePassiveSkill(Identifier id) {
        super(
                id,
                "Improve Dodge",
                "Increases evasion chance through nimble movement and reflexes",
                ModClasses.THIEF,
                1,
                1,
                10,
                false,
                Mamy.id("skill/passive/improve_dodge")
        );
    }

    @Override
    protected void initializePassiveEffects() {
        addAttributeModification(
                AttributeModification.addValue(
                        ModEntityAttributes.EVASION,
                        Mamy.id("improve_dodge_passive"),
                        skillLevel -> skillLevel * 3 // +3 evasion per level
                )
        );
    }

    @Override
    protected void addPassiveEffectsDescription(List<Text> tooltip, int skillLevel) {
        tooltip.add(Text.literal("Improves evasion through agility training and quick reflexes.").formatted(Formatting.GRAY));

        // Show current evasion bonus
        int evasionBonus = skillLevel * 3;
        tooltip.add(Text.literal("Evasion: +").formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(evasionBonus)).formatted(Formatting.GREEN)));
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.THIEF;
    }
}