package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.common.entity.BaseSkillEntity;
import net.minecraft.entity.LivingEntity;

public abstract class SkillEffect {
    protected final String id;

    public SkillEffect(String id) {
        this.id = id;
    }

    public abstract void apply(LivingEntity target, BaseSkillEntity skill);

    public String getId() {
        return id;
    }
}
