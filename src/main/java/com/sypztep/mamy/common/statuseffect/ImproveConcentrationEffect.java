package com.sypztep.mamy.common.statuseffect;

import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.StatModifierHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ImproveConcentrationEffect extends CleanUpEffect {
    private static final String MODIFIER_SOURCE = "ImproveConcentrationEffect";
    public ImproveConcentrationEffect(StatusEffectCategory category) {
        super(category);
    }
    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);

        short statBoost = (short) (3 + amplifier + 1); // 3 + (amplifier + 1)

        StatModifierHelper.applyTemporaryModifier(entity, StatTypes.AGILITY, MODIFIER_SOURCE, statBoost, true);
        StatModifierHelper.applyTemporaryModifier(entity, StatTypes.DEXTERITY, MODIFIER_SOURCE, statBoost, true);
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        StatModifierHelper.removeTemporaryModifier(entity, StatTypes.AGILITY, MODIFIER_SOURCE, true);
        StatModifierHelper.removeTemporaryModifier(entity, StatTypes.DEXTERITY, MODIFIER_SOURCE, true);
    }
}