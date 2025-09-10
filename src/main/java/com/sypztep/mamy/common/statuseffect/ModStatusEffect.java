package com.sypztep.mamy.common.statuseffect;


import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public abstract class ModStatusEffect extends StatusEffect {
    protected ModStatusEffect(StatusEffectCategory category) {
        super(category,0);
    }
}
