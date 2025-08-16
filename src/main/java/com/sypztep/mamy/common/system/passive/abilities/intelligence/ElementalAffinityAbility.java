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

// INT 20: +3 Magic Damage, +5% to all Elemental Damage
public class ElementalAffinityAbility extends PassiveAbility {
    public ElementalAffinityAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT,
                Mamy.id("elemental_affinity_magic"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 3.0
        ));
        // Add to all elemental multipliers
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.FIRE_ATTACK_DAMAGE_MULT,
                Mamy.id("elemental_affinity_fire"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.05
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.COLD_ATTACK_DAMAGE_MULT,
                Mamy.id("elemental_affinity_cold"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.05
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_MULT,
                Mamy.id("elemental_affinity_electric"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.05
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.WATER_ATTACK_DAMAGE_MULT,
                Mamy.id("elemental_affinity_water"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.05
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.WIND_ATTACK_DAMAGE_MULT,
                Mamy.id("elemental_affinity_wind"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.05
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.HOLY_ATTACK_DAMAGE_MULT,
                Mamy.id("elemental_affinity_holy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.05
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Elemental Affinity").formatted(Formatting.LIGHT_PURPLE);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Harmony with elements. +3 Magic Damage, +5% to all Elemental Damage");
    }
}
