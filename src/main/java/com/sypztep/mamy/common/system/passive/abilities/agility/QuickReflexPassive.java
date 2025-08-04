package com.sypztep.mamy.common.system.passive.abilities.agility;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sypztep.tyrannus.common.util.AttributeModification;

import java.util.Map;

public class QuickReflexPassive extends PassiveAbility {
    public QuickReflexPassive() {
        super("quick_reflex", Map.of(StatTypes.AGILITY, 20));
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.EVASION,
                Mamy.id("quick_reflex"),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
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
