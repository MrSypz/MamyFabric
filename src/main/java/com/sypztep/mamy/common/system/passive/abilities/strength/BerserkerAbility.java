package com.sypztep.mamy.common.system.passive.abilities.strength;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.Map;

public class BerserkerAbility extends PassiveAbility {
    public BerserkerAbility(String id, Map<StatTypes, Integer> requirements) {
    super(id, requirements);
}

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                Mamy.id("berserker_speed"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 1.0 // +1.0 attack speed
        ));

        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_DAMAGE,
                Mamy.id("berserker_crit_damage"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.1 // +10% crit damage
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Berserker").formatted(Formatting.DARK_RED);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Unleash your inner fury. +1.0 Attack Speed, +10% Crit Damage");
    }
}
