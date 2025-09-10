package com.sypztep.mamy.common.statuseffect;

import net.minecraft.entity.effect.StatusEffectCategory;

public abstract class CleanUpEffect extends ModStatusEffect implements EffectRemoval {
    protected CleanUpEffect(StatusEffectCategory category) {
        super(category);
    }
}
