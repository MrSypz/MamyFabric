package com.sypztep.mamy.common.system.passive.abilities.Intelligence;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sypztep.tyrannus.common.util.AttributeModification;

import java.util.Map;

public class ArcanePowerAbility extends PassiveAbility {
    public ArcanePowerAbility() {
        super("arcane_power", Map.of(StatTypes.INTELLIGENCE, 15));
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MAGIC_ATTACK_DAMAGE,
                Mamy.id("arcane_power_magic"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.20 // +20% magic damage
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Arcane Power").formatted(Formatting.BLUE);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Channel raw magical energy. +20% Magic Damage");
    }
}
