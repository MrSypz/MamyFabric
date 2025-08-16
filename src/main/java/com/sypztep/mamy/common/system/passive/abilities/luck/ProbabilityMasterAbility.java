package com.sypztep.mamy.common.system.passive.abilities.luck;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.AttributeModification;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

// LUK 75: +15% Crit Chance, +20 Accuracy (5% hit boost)
public class ProbabilityMasterAbility extends PassiveAbility {
    public ProbabilityMasterAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_CHANCE,
                Mamy.id("probability_master_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.15
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("probability_master_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 20.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Probability Master").formatted(Formatting.DARK_PURPLE);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Control probability itself. +15% Crit Chance, +20 Accuracy (5% hit rate)");
    }
}
