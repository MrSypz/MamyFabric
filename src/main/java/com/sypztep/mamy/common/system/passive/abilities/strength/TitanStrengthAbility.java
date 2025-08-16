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

// STR 99: +25 Melee Damage, +25% Double Attack
public class TitanStrengthAbility extends PassiveAbility {
    public TitanStrengthAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT,
                Mamy.id("titan_strength_damage"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 25.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.DOUBLE_ATTACK_CHANCE,
                Mamy.id("titan_strength_double"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.25
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Titan Strength").formatted(Formatting.DARK_RED);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Legendary might. +25 Melee Damage, +25% Double Attack Chance");
    }
}
