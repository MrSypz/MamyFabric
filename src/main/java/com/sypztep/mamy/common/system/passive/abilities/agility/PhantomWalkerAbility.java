package com.sypztep.mamy.common.system.passive.abilities.agility;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.AttributeModification;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

// AGI 75: +60 Evasion (15% dodge boost) - Keep existing +1 jump logic
public class PhantomWalkerAbility extends PassiveAbility {
    public PhantomWalkerAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.EVASION,
                Mamy.id("phantom_walker_evasion"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 60.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Phantom Walker").formatted(Formatting.GRAY);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Ghostly mobility. +60 Evasion (15% dodge). Enhanced jump height, otherworldly movement");
    }
}