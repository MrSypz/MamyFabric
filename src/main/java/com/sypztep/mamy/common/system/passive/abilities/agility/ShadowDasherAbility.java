package com.sypztep.mamy.common.system.passive.abilities.agility;

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

// AGI 99: +55 Evasion (13.75% dodge boost) + Utility
public class ShadowDasherAbility extends PassiveAbility {
    public ShadowDasherAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.EVASION,
                Mamy.id("shadow_dasher_evasion"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 55.0
        ));
        // Add some utility stats for the ultimate AGI passive
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                Mamy.id("shadow_dasher_speed"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.15
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Shadow Dasher").formatted(Formatting.DARK_PURPLE);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Ultimate agility. +55 Evasion (13.75% dodge), +15% attack speed. Master of all movement");
    }
}

