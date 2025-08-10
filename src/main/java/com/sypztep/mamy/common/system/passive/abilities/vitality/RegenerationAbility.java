package com.sypztep.mamy.common.system.passive.abilities.vitality;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.Map;

public class RegenerationAbility extends PassiveAbility {
    public RegenerationAbility(String id, Map<StatTypes, Integer> requirements) {
    super(id, requirements);
}

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.HEALTH_REGEN,
                Mamy.id("regeneration_regen"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 1.0 // +1 health regen per second
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Regeneration").formatted(Formatting.GREEN);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Your wounds close rapidly. +1 Health Regeneration per second");
    }
}
