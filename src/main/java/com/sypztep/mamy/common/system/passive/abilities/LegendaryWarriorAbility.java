package com.sypztep.mamy.common.system.passive.abilities;

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

public class LegendaryWarriorAbility extends PassiveAbility {
    public LegendaryWarriorAbility() {
        super("legendary_warrior", Map.of(
                StatTypes.STRENGTH, 50,
                StatTypes.AGILITY, 40,
                StatTypes.VITALITY, 45,
                StatTypes.DEXTERITY, 35,
                StatTypes.INTELLIGENCE, 30,
                StatTypes.LUCK, 40
        ));
    }

    @Override
    protected void initializeEffects() {
        // Massive bonuses for legendary status
        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                Mamy.id("legendary_warrior_damage"),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                baseValue -> 0.50 // +50% attack damage
        ));

        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                Mamy.id("legendary_warrior_speed"),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                baseValue -> 0.25 // +25% movement speed
        ));

        addAttributeEffect(new AttributeModification(
                EntityAttributes.GENERIC_MAX_HEALTH,
                Mamy.id("legendary_warrior_health"),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                baseValue -> 0.30 // +30% max health
        ));

        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.CRIT_CHANCE,
                Mamy.id("legendary_warrior_crit"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.20 // +20% crit chance
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Legendary Warrior").formatted(Formatting.GOLD, Formatting.BOLD);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Ascended beyond mortal limits. Massive bonuses to all combat abilities.");
    }
}
