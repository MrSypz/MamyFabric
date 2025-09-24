package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.common.system.damage.DamageComponent;
import com.sypztep.mamy.common.system.damage.HybridDamageSource;

import java.util.List;

public interface SkillDamage extends HybridDamageSource {
    @Override
    List<DamageComponent> getDamageComponents();
}