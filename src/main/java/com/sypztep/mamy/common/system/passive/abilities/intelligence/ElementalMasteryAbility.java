package com.sypztep.mamy.common.system.passive.abilities.intelligence;

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

// INT 75: +15 Magic Damage, +10% to all Elemental Damage
public class ElementalMasteryAbility extends PassiveAbility {
    public ElementalMasteryAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT,
                Mamy.id("elemental_mastery_magic"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 15.0
        ));
        // Add 10% to all elemental damages (similar to tier 20 but stronger)
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.FIRE_ATTACK_DAMAGE_MULT,
                Mamy.id("elemental_mastery_fire"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.10
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.COLD_ATTACK_DAMAGE_MULT,
                Mamy.id("elemental_mastery_cold"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.10
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_MULT,
                Mamy.id("elemental_mastery_electric"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.10
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.WATER_ATTACK_DAMAGE_MULT,
                Mamy.id("elemental_mastery_water"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.10
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.WIND_ATTACK_DAMAGE_MULT,
                Mamy.id("elemental_mastery_wind"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.10
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.HOLY_ATTACK_DAMAGE_MULT,
                Mamy.id("elemental_mastery_holy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.10
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Elemental Mastery").formatted(Formatting.DARK_PURPLE);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Command the elements. +15 Magic Damage, +10% to all Elemental Damage");
    }
}
