package com.sypztep.mamy.common.system.passive.abilities;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.AttributeModification;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public class HeadhunterAbility extends PassiveAbility {
    public HeadhunterAbility() {
        super("headhunter", Map.of(StatTypes.DEXTERITY, 20));
    }

    @Override
    protected void initializeEffects() {
        // We'll handle the headshot multiplier through events/mixins
        // But we can add some accuracy bonus
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("headhunter_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 5.0 // +5 accuracy for precision
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Headhunter").formatted(Formatting.RED);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Precision shots to the head deal 10x damage. +5 Accuracy");
    }
}
