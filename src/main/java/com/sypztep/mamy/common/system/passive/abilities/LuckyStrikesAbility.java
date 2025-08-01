package com.sypztep.mamy.common.system.passive.abilities;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sypztep.tyrannus.common.util.AttributeModification;

import java.util.Map;

public class LuckyStrikesAbility extends PassiveAbility {
    public LuckyStrikesAbility() {
        super("lucky_strikes", Map.of(StatTypes.LUCK, 40));
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_CHANCE,
                Mamy.id("lucky_strikes_chance"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.20 // +20% crit chance
        ));

        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("lucky_strikes_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 10.0 // +10 accuracy
        ));
    }

    @Override
    protected void  onApply(PlayerEntity player) {
        // Could add chance for double hits or other lucky effects
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Lucky Strikes").formatted(Formatting.LIGHT_PURPLE);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Extraordinary fortune guides your attacks. +20% Crit Chance, +10 Accuracy");
    }
}
