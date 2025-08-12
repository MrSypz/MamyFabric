package com.sypztep.mamy.common.system.passive.abilities.agility;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.AttributeModification;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public class PhantomWalkerAbility extends PassiveAbility {

    public PhantomWalkerAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                Mamy.id("phantom_walker"),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                baseValue -> 0.25 // +25% movement speed
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Phantom Walker").formatted(Formatting.AQUA);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Your speed surges by 25%, and you can perform an extra leap while airborne");
    }
}
