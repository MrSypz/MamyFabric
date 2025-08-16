package com.sypztep.mamy.common.system.passive.abilities.agility;

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

// AGI 50: +55 Evasion (13.75% dodge boost) - Keep existing no walk noise logic (enhance it)
public class ShadowStepAbility extends PassiveAbility {
    public ShadowStepAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.EVASION,
                Mamy.id("shadow_step_evasion"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 55.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Shadow Step").formatted(Formatting.DARK_PURPLE);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Silent movement. +55 Evasion (13.75% dodge). No walking sounds, enhanced stealth");
    }
}