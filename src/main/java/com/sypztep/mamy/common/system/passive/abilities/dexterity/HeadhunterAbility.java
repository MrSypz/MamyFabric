package com.sypztep.mamy.common.system.passive.abilities.dexterity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.Map;

public class HeadhunterAbility extends PassiveAbility {
    public HeadhunterAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("headhunter_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 40.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Headhunter").formatted(Formatting.RED);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Precision shots to the head, Explode ande deal 5x target max health and bleeding to dead. +40 Accuracy");
    }
}
