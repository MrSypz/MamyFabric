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

// LUK 99: +25% Crit Chance, +10 Accuracy (2.5% hit boost) + Utility
public class FateWeaverAbility extends PassiveAbility {
    public FateWeaverAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_CHANCE,
                Mamy.id("fate_weaver_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.25
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("fate_weaver_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 10.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_DAMAGE,
                Mamy.id("fate_weaver_crit_dmg"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.30
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Fate Weaver").formatted(Formatting.DARK_PURPLE);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Weave the threads of destiny. +25% Crit Chance, +30% Crit Damage, +10 Accuracy");
    }
}
