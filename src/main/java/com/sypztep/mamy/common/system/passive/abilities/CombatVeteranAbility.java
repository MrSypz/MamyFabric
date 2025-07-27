package com.sypztep.mamy.common.system.passive.abilities;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.AttributeModification;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public class CombatVeteranAbility extends PassiveAbility {
    public CombatVeteranAbility() {
        super("combat_veteran", Map.of(
                StatTypes.STRENGTH, 25,
                StatTypes.AGILITY, 20,
                StatTypes.VITALITY, 20
        ));
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                Mamy.id("combat_veteran_damage"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 3.0 // +3 attack damage
        ));

        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_ARMOR,
                Mamy.id("combat_veteran_armor"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 3.0 // +3 armor
        ));

        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_CHANCE,
                Mamy.id("combat_veteran_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.10 // +10% crit chance
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Combat Veteran").formatted(Formatting.DARK_RED);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Battle-hardened warrior. +3 Attack Damage, +3 Armor, +10% Crit Chance");
    }
}
