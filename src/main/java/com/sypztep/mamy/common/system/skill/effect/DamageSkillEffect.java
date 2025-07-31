package com.sypztep.mamy.common.system.skill.effect;

import com.sypztep.mamy.common.entity.BaseSkillEntity;
import com.sypztep.mamy.common.system.skill.SkillEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;

public class DamageSkillEffect extends SkillEffect {
    private final float damage;
    private final RegistryKey<DamageType> damageType;

    public DamageSkillEffect(String id, float damage, RegistryKey<DamageType> damageType) {
        super(id);
        this.damage = damage;
        this.damageType = damageType;
    }

    @Override
    public void apply(LivingEntity target, BaseSkillEntity skill) {
        target.damage(
                target.getWorld().getDamageSources().create(damageType, skill, skill.getOwner()),
                damage
        );
    }
}
