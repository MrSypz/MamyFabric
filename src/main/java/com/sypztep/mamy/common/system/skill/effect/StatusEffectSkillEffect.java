package com.sypztep.mamy.common.system.skill.effect;

import com.sypztep.mamy.common.entity.BaseSkillEntity;
import com.sypztep.mamy.common.system.skill.SkillEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public class StatusEffectSkillEffect extends SkillEffect {
    private final StatusEffectInstance effect;

    public StatusEffectSkillEffect(String id, StatusEffectInstance effect) {
        super(id);
        this.effect = effect;
    }

    @Override
    public void apply(LivingEntity target, BaseSkillEntity skill) {
        target.addStatusEffect(new StatusEffectInstance(effect));
    }
}
