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

// STR 30: +15% Back Attack, +25% Crit Damage
public class BackBreakerAbility extends PassiveAbility {
    public BackBreakerAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.BACK_ATTACK,
                Mamy.id("back_breaker_back"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.15
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_DAMAGE,
                Mamy.id("back_breaker_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.25
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Back Breaker").formatted(Formatting.DARK_RED);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Punish the weak. +15% Back Attack Damage, +25% Crit Damage. Deal 50% more damage to enemies below 25% health");
    }
}
