package com.sypztep.mamy.common.system.passive.abilities.vitality;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.Map;

public class LastStandAbility extends PassiveAbility {
    public LastStandAbility(String id, Map<StatTypes, Integer> requirements) {
    super(id, requirements);
}

    @Override
    protected void initializeEffects() {
        // This would need special handling in damage events
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_MAX_HEALTH,
                Mamy.id("last_stand_health"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 10.0 // +10 max health
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Last Stand").formatted(Formatting.DARK_RED);
    }

    @Override
    public Text getDescription() {
        return Text.literal("When near death, gain massive damage resistance. +10 Max Health");
    }
}
