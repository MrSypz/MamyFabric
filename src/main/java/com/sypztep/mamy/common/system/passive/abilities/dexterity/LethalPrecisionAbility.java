package com.sypztep.mamy.common.system.passive.abilities.dexterity;

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

// DEX 75: +60 Accuracy (15% hit boost)
public class LethalPrecisionAbility extends PassiveAbility {
    public LethalPrecisionAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("lethal_precision_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 60.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_DAMAGE,
                Mamy.id("lethal_precision_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.25
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Lethal Precision").formatted(Formatting.DARK_RED);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Deadly accuracy. +60 Accuracy (15% hit rate), +25% Crit Damage");
    }
}
