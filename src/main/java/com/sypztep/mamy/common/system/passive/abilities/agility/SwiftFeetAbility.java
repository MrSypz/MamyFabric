package com.sypztep.mamy.common.system.passive.abilities.agility;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.Map;

public class SwiftFeetAbility extends PassiveAbility {
    public SwiftFeetAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                Mamy.id("swift_feet_speed"),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                baseValue -> 0.5 // +5% movement speed
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Swift Feet").formatted(Formatting.GREEN);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Your nimble steps grant +5% Movement Speed, letting you outrun the wind");
    }
}