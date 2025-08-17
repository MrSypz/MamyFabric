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

// VIT 99: +12 Health, +6% Damage Reduction, +0.5 Health Regen - ULTIMATE DEFENSE
public class ImmortalFortressAbility extends PassiveAbility {
    public ImmortalFortressAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_MAX_HEALTH,
                Mamy.id("immortal_fortress_health"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 12.0  // +6 hearts - respectable endgame bonus
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.DAMAGE_REDUCTION,
                Mamy.id("immortal_fortress_dr"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.06  // Strong defensive capstone
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.HEALTH_REGEN,
                Mamy.id("immortal_fortress_regen"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.5  // Complete the regen package
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Immortal Fortress").formatted(Formatting.DARK_RED);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Ultimate vitality. +12 Max Health, +6% Damage Reduction, +0.5 Health Regen/sec, share damage with allies");
    }
}