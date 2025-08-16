package com.sypztep.mamy.common.system.passive.abilities.strength;

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

// STR 50: +12 Melee Damage, +15% Double Attack
public class BerserkerRageAbility extends PassiveAbility {
    public BerserkerRageAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT,
                Mamy.id("berserker_rage_damage"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 12.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.DOUBLE_ATTACK_CHANCE,
                Mamy.id("berserker_rage_double"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.15
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Berserker Rage").formatted(Formatting.DARK_RED);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Fury in battle. +12 Melee Damage, +15% Double Attack Chance");
    }
}
