package com.sypztep.mamy.common.statuseffect;

import com.sypztep.mamy.Mamy;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;

public class EndureEffect extends CooldownEffect {
    public EndureEffect(StatusEffectCategory category) {
        super(category);
        this.addAttributeModifier(EntityAttributes.GENERIC_ARMOR, Mamy.id("endure_skill_effect"), 1.0D, EntityAttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }
}
