package com.sypztep.mamy.common.system.passive.abilities.dexterity;

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

// DEX 99: +55 Accuracy (13.75% hit boost) - Keep existing ricochet logic
public class RicochetMasterAbility extends PassiveAbility {
    public RicochetMasterAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.ACCURACY,
                Mamy.id("ricochet_master_accuracy"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 55.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE_FLAT,
                Mamy.id("ricochet_master_projectile"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 10.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Ricochet Master").formatted(Formatting.DARK_RED);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Master of angles. +55 Accuracy (13.75% hit rate), +10 Projectile Damage. Projectiles ricochet and home to targets");
    }
}