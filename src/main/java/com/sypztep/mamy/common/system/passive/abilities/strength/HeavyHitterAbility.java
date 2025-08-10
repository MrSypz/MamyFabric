package com.sypztep.mamy.common.system.passive.abilities.strength;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.Map;

public class HeavyHitterAbility extends PassiveAbility {
    public HeavyHitterAbility(String id, Map<StatTypes, Integer> requirements) {
    super(id, requirements);
}

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MELEE_ATTACK_DAMAGE,
                Mamy.id("heavy_hitter_melee"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 1.5 // +150% melee damage
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Heavy Hitter").formatted(Formatting.RED);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Your strikes pack devastating force. +150% Melee Damage");
    }
}
