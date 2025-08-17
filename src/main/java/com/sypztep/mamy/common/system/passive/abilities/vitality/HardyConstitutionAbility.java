package com.sypztep.mamy.common.system.passive.abilities.vitality;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.AttributeModification;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
// =====================================
// VITALITY PASSIVES (SURVIVABILITY)
// =====================================

// VIT 10: +8 Health, +0.5 Health Regen - EARLY SURVIVABILITY
public class HardyConstitutionAbility extends PassiveAbility {
    public HardyConstitutionAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_MAX_HEALTH,
                Mamy.id("hardy_constitution_health"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 8.0  // +4 hearts - doubles Novice health
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.HEALTH_REGEN,
                Mamy.id("hardy_constitution_regen"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.5  // Gentle regeneration
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Hardy Constitution").formatted(Formatting.GREEN);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Robust health. +8 Max Health, +0.5 Health Regen/sec, bypass damage timer for regeneration");
    }
}

