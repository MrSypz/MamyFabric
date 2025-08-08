package com.sypztep.mamy.common.system.passive.abilities.luck;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sypztep.tyrannus.common.util.AttributeModification;

import java.util.Map;

public class LuckyStrikesAbility extends PassiveAbility {
    public LuckyStrikesAbility(String id, Map<StatTypes, Integer> requirements) {
    super(id, requirements);
}

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_CHANCE,
                Mamy.id("lucky_strikes_chance"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.20 // +20% crit chance
        ));

        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("lucky_strikes_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 10.0 // +10 accuracy
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Lucky Strikes").formatted(Formatting.LIGHT_PURPLE);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Extraordinary fortune guides your attacks. +20% Crit Chance, +10 Accuracy");
    }
}
