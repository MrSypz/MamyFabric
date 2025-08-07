package com.sypztep.mamy.common.system.passive.abilities.intelligence;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sypztep.tyrannus.common.util.AttributeModification;

import java.util.Map;

public class ManaShieldAbility extends PassiveAbility {
    public ManaShieldAbility() {
        super("mana_shield", Map.of(StatTypes.INTELLIGENCE, 25));
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MAGIC_RESISTANCE,
                Mamy.id("mana_shield_resist"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.30 // +30% magic resistance
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Mana Shield").formatted(Formatting.AQUA);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Magical barriers protect you. +30% Magic Resistance");
    }
}
