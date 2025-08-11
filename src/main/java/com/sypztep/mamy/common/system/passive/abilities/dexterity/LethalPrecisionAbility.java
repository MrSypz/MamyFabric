package com.sypztep.mamy.common.system.passive.abilities.dexterity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.AttributeModification;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;

import java.util.Map;

public class LethalPrecisionAbility extends PassiveAbility {
    public LethalPrecisionAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_DAMAGE,
                Mamy.id("lethal_precision_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.1 // +10% crit chance
        ));

        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("lethal_precision_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 15.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Lethal Precision");
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Increases critical damage by +10% and +15 accuracy ");
    }
}
