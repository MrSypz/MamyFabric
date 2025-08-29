package com.sypztep.mamy.common.statuseffect;

import com.sypztep.mamy.Mamy;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ProvokeEffect extends StatusEffect {
    public ProvokeEffect(StatusEffectCategory category) {
        super(category, 0xFF6B4423); // Red-orange color
        this.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                Mamy.id("provoke_attack_effect"), 0.05D,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.addAttributeModifier(EntityAttributes.GENERIC_ARMOR,
                Mamy.id("provoke_defense_effect"), -0.10D,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
}