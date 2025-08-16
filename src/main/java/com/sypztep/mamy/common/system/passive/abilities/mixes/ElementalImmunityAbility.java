package com.sypztep.mamy.common.system.passive.abilities.mixes;

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

public class ElementalImmunityAbility extends PassiveAbility {
    public ElementalImmunityAbility(String id, Map<StatTypes, Integer> requirements) {
    super(id, requirements);
}

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MAGIC_RESISTANCE,
                Mamy.id("elemental_immunity_magic_resist"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.07 // +7% magic resistance
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Elemental Immunity").formatted(Formatting.LIGHT_PURPLE);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Immunity to fire damage and high elemental resistance. +7% Magic Resistance");
    }
}
