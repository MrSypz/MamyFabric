package com.sypztep.mamy.common.system.passive.abilities;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sypztep.tyrannus.common.util.AttributeModification;

import java.util.Map;

public class ElementalImmunityAbility extends PassiveAbility {
    public ElementalImmunityAbility() {
        super("elemental_immunity", Map.of(
                StatTypes.VITALITY, 25,
                StatTypes.INTELLIGENCE, 15
        ));
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MAGIC_RESISTANCE,
                Mamy.id("elemental_immunity_magic_resist"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.50 // +50% magic resistance
        ));
    }

    @Override
    protected void onApply(PlayerEntity player) {
        // Fire immunity would be handled through damage events
        // Could add other immunities here
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Elemental Immunity").formatted(Formatting.LIGHT_PURPLE);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Immunity to fire damage and high elemental resistance. +50% Magic Resistance");
    }
}
