package com.sypztep.mamy.common.system.passive.abilities.strength;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sypztep.tyrannus.common.util.AttributeModification;

import java.util.Map;

public class WeaponMasterAbility extends PassiveAbility {
    public WeaponMasterAbility(String id, Map<StatTypes, Integer> requirements) {
    super(id, requirements);
}

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                Mamy.id("weapon_master_damage"),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                baseValue -> 0.30 // +30% attack damage
        ));

        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MELEE_ATTACK_DAMAGE,
                Mamy.id("weapon_master_melee"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.20 // +20% melee damage
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Weapon Master").formatted(Formatting.GOLD);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Mastery over all weapons. +30% Attack Damage, +20% Melee Damage");
    }
}
