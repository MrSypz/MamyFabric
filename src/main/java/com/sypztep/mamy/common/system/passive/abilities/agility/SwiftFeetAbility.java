package com.sypztep.mamy.common.system.passive.abilities.agility;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.Map;

// =====================================
// AGILITY PASSIVES (EVASION FOCUS)
// Total: +245 Evasion across all 6 passives
// =====================================

// AGI 10: +15 Evasion (3.75% dodge boost)
public class SwiftFeetAbility extends PassiveAbility {
    public SwiftFeetAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.EVASION,
                Mamy.id("swift_feet_evasion"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 15.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Swift Feet").formatted(Formatting.GREEN);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Nimble movement. +15 Evasion (3.75% dodge)");
    }
}
