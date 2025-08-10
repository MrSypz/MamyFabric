package com.sypztep.mamy.common.system.passive.abilities.dexterity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.Map;

public class SteadyAimAbility extends PassiveAbility {
    public SteadyAimAbility(String id, Map<StatTypes, Integer> requirements) {
    super(id, requirements);
}

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE,
                Mamy.id("steady_aim_projectile"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 5.0
        ));

        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("steady_aim_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 20.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Steady Aim").formatted(Formatting.GOLD);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Perfect control over ranged weapons. +5 Projectile Damage, +20 Accuracy, And move faster when draw bow | crossbow");
    }
}
