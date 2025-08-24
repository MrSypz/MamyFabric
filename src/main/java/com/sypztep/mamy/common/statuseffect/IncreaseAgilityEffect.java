package com.sypztep.mamy.common.statuseffect;

import com.sypztep.mamy.Mamy;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class IncreaseAgilityEffect extends StatusEffect {
    public IncreaseAgilityEffect(StatusEffectCategory category) {
        super(category, 0);
        this.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, Mamy.id("increase_agility_effect"), 0.01D, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, Mamy.id("increase_agility_effect"), 0.05D, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
}