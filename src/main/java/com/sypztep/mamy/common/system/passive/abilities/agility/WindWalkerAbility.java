package com.sypztep.mamy.common.system.passive.abilities.agility;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.Map;

public class WindWalkerAbility extends PassiveAbility {

    public WindWalkerAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                Mamy.id("wind_walker_speed"),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                baseValue -> 0.15 // +15% movement speed
        ));

        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                Mamy.id("wind_walker_attack_speed"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.5 // +0.5 attack speed
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Wind Walker").formatted(Formatting.AQUA);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("One with the wind itself — gain +15% Movement Speed, +0.5 Attack Speed, and improved air contro");
    }
}
