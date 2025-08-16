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

// LUK 50: +20% Crit Damage, +25 Accuracy (6.25% hit boost)
public class DestinedStrikesAbility extends PassiveAbility {
    public DestinedStrikesAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_DAMAGE,
                Mamy.id("destined_strikes_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.20
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("destined_strikes_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 25.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Destined Strikes").formatted(Formatting.GOLD);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Fate guides your attacks. +20% Crit Damage, +25 Accuracy (6.25% hit rate)");
    }
}
