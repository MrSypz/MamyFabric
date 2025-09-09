package com.sypztep.mamy.common.statuseffect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class EnvenomWeaponStatusEffect extends StatusEffect {

    public EnvenomWeaponStatusEffect(StatusEffectCategory category ){
        super(category, 0); // Green color for poison
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        return false;
    }
}
