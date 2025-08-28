package com.sypztep.mamy.common.statuseffect;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class AngelusEffect extends StatusEffect {

    public AngelusEffect(StatusEffectCategory category) {
        super(category, 0x87CEEB); // Sky blue color for angelus blessing

        // Add 5% damage reduction per level (soft defense)
        this.addAttributeModifier(ModEntityAttributes.DAMAGE_REDUCTION,
                Mamy.id("angelus_def_bonus"), 0.05D,
                EntityAttributeModifier.Operation.ADD_VALUE);

        // Add 50 max HP per level
        this.addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH,
                Mamy.id("angelus_hp_bonus"), 50.0D,
                EntityAttributeModifier.Operation.ADD_VALUE);
    }
}