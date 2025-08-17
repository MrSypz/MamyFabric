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

// INT 99: +25 Magic Damage, +50% Resource
public class ArchmagePowerAbility extends PassiveAbility {
    public ArchmagePowerAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT,
                Mamy.id("archmage_power_magic"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 25.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.RESOURCE,
                Mamy.id("archmage_power_resource"),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                baseValue -> 0.50
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Archmage Power").formatted(Formatting.DARK_PURPLE);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Ultimate arcane ascension. +25 Magic Damage, +50% Max Resource. Why part seas when you can walk across?");
    }
}
