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

// VIT 50: +18 Health, +5% Damage Reduction - MID-GAME POWER SPIKE
public class GuardianSpiritAbility extends PassiveAbility {
    public GuardianSpiritAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_MAX_HEALTH,
                Mamy.id("guardian_spirit_health"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 18.0  // +9 hearts - significant but not broken
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.DAMAGE_REDUCTION,
                Mamy.id("guardian_spirit_dr"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.05  // Meaningful defensive upgrade
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Guardian Spirit").formatted(Formatting.GOLD);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Spiritual protection. +18 Max Health, +5% Damage Reduction, immunity to instant death effects");
    }
}
