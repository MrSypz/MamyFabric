package com.sypztep.mamy.common.system.passive.abilities.vitality;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.AttributeModification;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

// VIT 20: +2% Damage Reduction, +15% Heal Effectiveness - RECOVERY FOCUS
public class RapidRegenerationAbility extends PassiveAbility {
    public RapidRegenerationAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.HEAL_EFFECTIVE,
                Mamy.id("rapid_regeneration_heal"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.15  // Better healing from all sources
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.DAMAGE_REDUCTION,
                Mamy.id("rapid_regeneration_dr"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.02  // Small but noticeable
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Rapid Regeneration").formatted(Formatting.GREEN);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Enhanced recovery. +15% Heal Effectiveness, +2% Damage Reduction, immunity to hunger/poison");
    }
}

