package com.sypztep.mamy.common.system.passive.abilities.strength;

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

// STR 75: +18 Melee Damage, +50% Crit Damage
public class WeaponMasterAbility extends PassiveAbility {
    public WeaponMasterAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT,
                Mamy.id("weapon_master_damage"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 18.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_DAMAGE,
                Mamy.id("weapon_master_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.50
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Weapon Master").formatted(Formatting.GOLD);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Perfect technique. +18 Melee Damage, +50% Crit Damage");
    }
}
