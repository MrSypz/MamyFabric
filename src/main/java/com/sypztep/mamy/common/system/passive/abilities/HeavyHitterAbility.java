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

public class HeavyHitterAbility extends PassiveAbility {
    public HeavyHitterAbility() {
        super("heavy_hitter", Map.of(StatTypes.STRENGTH, 12));
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MELEE_ATTACK_DAMAGE,
                Mamy.id("heavy_hitter_melee"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.15 // +15% melee damage
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Heavy Hitter").formatted(Formatting.RED);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Your strikes pack devastating force. +15% Melee Damage");
    }
}
