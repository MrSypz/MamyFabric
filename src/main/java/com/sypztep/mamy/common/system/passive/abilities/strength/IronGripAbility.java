package com.sypztep.mamy.common.system.passive.abilities.strength;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.AttributeModification;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;

import java.util.Map;

public class IronGripAbility extends PassiveAbility {
    public IronGripAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                Mamy.id("iron_grip"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 2.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Iron Grip");
    }

    @Override
    public Text getDescription() {
        return Text.literal("Your strong grip increases attack damage by +2");
    }
}
