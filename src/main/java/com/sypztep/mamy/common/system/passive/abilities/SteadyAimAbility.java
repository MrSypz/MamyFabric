package com.sypztep.mamy.common.system.passive.abilities;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sypztep.tyrannus.common.util.AttributeModification;

import java.util.Map;

public class SteadyAimAbility extends PassiveAbility {
    public SteadyAimAbility() {
        super("steady_aim", Map.of(StatTypes.DEXTERITY, 35));
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE,
                Mamy.id("steady_aim_projectile"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.25 // +25% projectile damage
        ));

        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("steady_aim_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 8.0 // +8 accuracy
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Steady Aim").formatted(Formatting.GOLD);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Perfect control over ranged weapons. +25% Projectile Damage, +8 Accuracy");
    }
}
