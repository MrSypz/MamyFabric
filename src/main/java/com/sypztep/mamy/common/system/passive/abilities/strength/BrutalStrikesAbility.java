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

// =====================================
// STRENGTH PASSIVES (MELEE DAMAGE)
// =====================================

// STR 10: +3 Melee Damage, +10% Back Attack
public class BrutalStrikesAbility extends PassiveAbility {
    public BrutalStrikesAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT,
                Mamy.id("brutal_strikes_damage"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 3.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.BACK_ATTACK,
                Mamy.id("brutal_strikes_back"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.10
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Brutal Strikes").formatted(Formatting.RED);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Powerful attacks. +3 Melee Damage, +10% Back Attack Damage");
    }
}

