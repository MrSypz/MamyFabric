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

public class FortuneFavorAbility extends PassiveAbility {
    public FortuneFavorAbility() {
        super("fortune_favor", Map.of(StatTypes.LUCK, 10));
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_CHANCE,
                Mamy.id("fortune_favor_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.08 // +8% crit chance
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Fortune's Favor").formatted(Formatting.YELLOW);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Luck smiles upon you. +8% Critical Hit Chance");
    }
}
