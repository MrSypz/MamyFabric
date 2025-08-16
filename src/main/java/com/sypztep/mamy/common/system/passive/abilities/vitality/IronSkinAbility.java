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

// VIT 30: +5% Damage Reduction, +3 Health Regen
public class IronSkinAbility extends PassiveAbility {
    public IronSkinAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.DAMAGE_REDUCTION,
                Mamy.id("iron_skin_dr"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.05
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.HEALTH_REGEN,
                Mamy.id("iron_skin_regen"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 3.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Iron Skin").formatted(Formatting.GRAY);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Tough as metal. +5% Damage Reduction, +3 Health Regen/sec");
    }
}
