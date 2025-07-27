package com.sypztep.mamy.common.system.passive.abilities;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.AttributeModification;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public class DodgeMasterAbility extends PassiveAbility {
    public DodgeMasterAbility() {
        super("dodge_master", Map.of(StatTypes.AGILITY, 15));
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.EVASION,
                Mamy.id("dodge_master_evasion"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 10.0 // +10 evasion
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Dodge Master").formatted(Formatting.GREEN);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Master of evasion, attacks slip past you like shadows. +10 Evasion");
    }
}
