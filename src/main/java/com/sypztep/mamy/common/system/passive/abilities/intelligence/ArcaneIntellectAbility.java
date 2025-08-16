package com.sypztep.mamy.common.system.passive.abilities.intelligence;

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

// INT 30: +6 Magic Damage, +30% Resource
public class ArcaneIntellectAbility extends PassiveAbility {
    public ArcaneIntellectAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT,
                Mamy.id("arcane_intellect_magic"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 6.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.RESOURCE,
                Mamy.id("arcane_intellect_resource"),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                baseValue -> 0.30
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Arcane Intellect").formatted(Formatting.LIGHT_PURPLE);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Deep magical understanding. +6 Magic Damage, +30% Max Resource");
    }
}
