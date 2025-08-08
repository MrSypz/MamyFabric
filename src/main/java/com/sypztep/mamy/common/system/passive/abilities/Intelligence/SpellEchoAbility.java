package com.sypztep.mamy.common.system.passive.abilities.intelligence;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sypztep.tyrannus.common.util.AttributeModification;

import java.util.Map;

public class SpellEchoAbility extends PassiveAbility {
    public SpellEchoAbility(String id, Map<StatTypes, Integer> requirements) {
    super(id, requirements);
}

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.MAGIC_ATTACK_DAMAGE,
                Mamy.id("spell_echo_magic"),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                baseValue -> 0.35 // +35% magic damage
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Spell Echo").formatted(Formatting.LIGHT_PURPLE);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Spells resonate with power, occasionally casting twice. +35% Magic Damage");
    }
}
