package com.sypztep.mamy.common.system.passive.abilities;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sypztep.tyrannus.common.util.AttributeModification;

import java.util.Map;

public class CriticalExpertAbility extends PassiveAbility {
    public CriticalExpertAbility() {
        super("critical_expert", Map.of(StatTypes.LUCK, 25));
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_CHANCE,
                Mamy.id("critical_expert_chance"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.12 // +12% crit chance
        ));

        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_DAMAGE,
                Mamy.id("critical_expert_damage"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.30 // +30% crit damage
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Critical Expert").formatted(Formatting.GOLD);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Master of critical strikes. +12% Crit Chance, +30% Crit Damage");
    }
}
