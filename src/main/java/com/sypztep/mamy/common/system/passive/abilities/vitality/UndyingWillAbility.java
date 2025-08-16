package com.sypztep.mamy.common.system.passive.abilities.vitality;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.AttributeModification;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

// VIT 75: +150 Health, +5 Health Regen
public class UndyingWillAbility extends PassiveAbility {
    public UndyingWillAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_MAX_HEALTH,
                Mamy.id("undying_will_health"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 150.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.HEALTH_REGEN,
                Mamy.id("undying_will_regen"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 5.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Undying Will").formatted(Formatting.DARK_RED);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Refuse to die. +150 Max Health, +5 Health Regen/sec");
    }
}
