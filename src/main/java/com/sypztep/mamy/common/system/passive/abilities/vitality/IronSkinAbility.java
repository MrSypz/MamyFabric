package com.sypztep.mamy.common.system.passive.abilities.vitality;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.Map;

public class IronSkinAbility extends PassiveAbility {
    public IronSkinAbility(String id, Map<StatTypes, Integer> requirements) {
    super(id, requirements);
}

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_ARMOR,
                Mamy.id("iron_skin_armor"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 8.0 // +2 armor
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Iron Skin").formatted(Formatting.GRAY);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Your skin hardens like iron, deflecting blows. +8 Armor");
    }
}
