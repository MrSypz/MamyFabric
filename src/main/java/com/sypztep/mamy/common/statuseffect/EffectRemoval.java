package com.sypztep.mamy.common.statuseffect;

import net.minecraft.entity.LivingEntity;
//TODO intregate into other effect instead of hardcode
public interface EffectRemoval {
    void onRemoved(LivingEntity entity);
}
