package com.sypztep.mamy.common.system.passive.abilities.dexterity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.Map;

public class PrecisionStrikesAbility extends PassiveAbility {
    public PrecisionStrikesAbility(String id, Map<StatTypes, Integer> requirements) {
    super(id, requirements);
}

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_CHANCE,
                Mamy.id("precision_strikes_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.05 // +5% crit chance
        ));

        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("precision_strikes_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 3.0 // +3 accuracy
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Precision Strikes").formatted(Formatting.YELLOW);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Your strikes find their mark with deadly precision. +5% Crit Chance, +3 Accuracy");
    }
}
