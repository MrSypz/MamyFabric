package com.sypztep.mamy.common.system.passive.abilities.agility;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.Map;

public class QuickReflexPassive extends PassiveAbility {

    public QuickReflexPassive(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.EVASION,
                Mamy.id("quick_reflex"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 80 // +20% Evasion
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Quick Reflex").formatted(Formatting.GREEN);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Your quick reflexes increase evasion by 20%");
    }
}
