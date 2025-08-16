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

// LUK 20: +10% Crit Damage, +15 Evasion (3.75% dodge)
public class FortuneFinderAbility extends PassiveAbility {
    public FortuneFinderAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_DAMAGE,
                Mamy.id("fortune_finder_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.10
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.EVASION,
                Mamy.id("fortune_finder_evasion"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 15.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Fortune Finder").formatted(Formatting.YELLOW);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Blessed by fortune. +10% Crit Damage, +15 Evasion (3.75% dodge)");
    }
}
