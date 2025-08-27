package com.sypztep.mamy.common.statuseffect;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.StatModifierHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class IncreaseAgilityEffect extends StatusEffect {
    private static final String MODIFIER_SOURCE = "increase_agility_effect";
    public IncreaseAgilityEffect(StatusEffectCategory category) {
        super(category, 0);
        this.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, Mamy.id("increase_agility_effect"), 0.01D, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, Mamy.id("increase_agility_effect"), 0.05D, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);
        short agiBonus = (short) (amplifier + 1);
        StatModifierHelper.applyTemporaryModifier(entity, StatTypes.AGILITY, MODIFIER_SOURCE, agiBonus, true);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        StatModifierHelper.removeTemporaryModifier(entity, StatTypes.AGILITY, MODIFIER_SOURCE, true);
        return super.applyUpdateEffect(entity, amplifier);
    }
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration <= 1;
    }
}