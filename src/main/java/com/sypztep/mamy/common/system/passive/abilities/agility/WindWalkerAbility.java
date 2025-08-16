package com.sypztep.mamy.common.system.passive.abilities.agility;

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

// AGI 30: +35 Evasion (8.75% dodge boost) - Keep existing +1 jump + air control
public class WindWalkerAbility extends PassiveAbility {
    public WindWalkerAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.EVASION,
                Mamy.id("wind_walker_evasion"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 35.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Wind Walker").formatted(Formatting.AQUA);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Master of air. +35 Evasion (8.75% dodge). Enhanced jump height and air control");
    }
}

