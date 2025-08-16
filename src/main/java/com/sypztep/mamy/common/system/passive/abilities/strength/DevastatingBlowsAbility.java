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

// STR 20: +6 Melee Damage, +5% Crit Chance
public class DevastatingBlowsAbility extends PassiveAbility {
    public DevastatingBlowsAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT,
                Mamy.id("devastating_blows_damage"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 6.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_CHANCE,
                Mamy.id("devastating_blows_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.05
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Devastating Blows").formatted(Formatting.RED);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Crushing power. +6 Melee Damage, +5% Crit Chance");
    }
}
