package com.sypztep.mamy.common.system.passive.abilities.intelligence;

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
// =====================================
// INTELLIGENCE PASSIVES (MAGIC DAMAGE)
// =====================================

// INT 10: +20% Resource, +3 Resource Regen
public class ManaEfficiencyAbility extends PassiveAbility {
    public ManaEfficiencyAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.RESOURCE,
                Mamy.id("mana_efficiency_resource"),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                baseValue -> 0.20
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.RESOURCE_REGEN,
                Mamy.id("mana_efficiency_regen"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 3.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Mana Efficiency").formatted(Formatting.BLUE);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Improved mana control. +20% Max Resource, +3 Resource Regen/sec");
    }
}

