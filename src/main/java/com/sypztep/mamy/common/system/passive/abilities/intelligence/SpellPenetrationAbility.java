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

// INT 50: +10 Magic Damage, +15% Magic Resistance
public class SpellPenetrationAbility extends PassiveAbility {
    public SpellPenetrationAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT,
                Mamy.id("spell_penetration_magic"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 10.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MAGIC_RESISTANCE,
                Mamy.id("spell_penetration_resist"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.05
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Spell Penetration").formatted(Formatting.DARK_PURPLE);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Pierce magical defenses. +10 Magic Damage, +5% Magic Resistance. 20% chance spells cost no mana");
    }
}
