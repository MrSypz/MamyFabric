package com.sypztep.mamy.common.statuseffect;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.StatModifierHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;

public class IncreaseAgilityEffect extends CleanUpEffect {
    private static final String MODIFIER_SOURCE = "IncreaseAgilityEffect";
    public IncreaseAgilityEffect(StatusEffectCategory category) {
        super(category);
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
    public void onRemoved(LivingEntity entity) {
        StatModifierHelper.removeTemporaryModifier(entity, StatTypes.AGILITY, MODIFIER_SOURCE, true);
    }
}