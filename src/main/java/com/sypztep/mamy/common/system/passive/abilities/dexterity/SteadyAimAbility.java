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

// DEX 30: +35 Accuracy (8.75% hit boost) - Keep existing bow shake reduction logic
public class SteadyAimAbility extends PassiveAbility {
    public SteadyAimAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("steady_aim_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 35.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE_FLAT,
                Mamy.id("steady_aim_projectile"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 3.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Steady Aim").formatted(Formatting.GOLD);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Perfect control. +35 Accuracy (8.75% hit rate), +3 Projectile Damage. Reduced bow shake");
    }
}