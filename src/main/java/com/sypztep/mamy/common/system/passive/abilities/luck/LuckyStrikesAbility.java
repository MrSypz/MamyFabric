package com.sypztep.mamy.common.system.passive.abilities.luck;

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
// LUCK PASSIVES (CRITICAL HITS + BALANCE)
// Total: +122 Accuracy across all 6 passives (half of DEX for balance)
// =====================================

// LUK 10: +5% Crit Chance, +12 Accuracy (3% hit boost)
public class LuckyStrikesAbility extends PassiveAbility {
    public LuckyStrikesAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_CHANCE,
                Mamy.id("lucky_strikes_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.05
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("lucky_strikes_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 12.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Lucky Strikes").formatted(Formatting.YELLOW);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Fortune favors the bold. +5% Crit Chance, +12 Accuracy (3% hit rate)");
    }
}

