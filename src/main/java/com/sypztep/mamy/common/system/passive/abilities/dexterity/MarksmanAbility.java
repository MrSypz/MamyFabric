package com.sypztep.mamy.common.system.passive.abilities.dexterity;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.AttributeModification;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;

import java.util.Map;

public class MarksmanAbility extends PassiveAbility {
    public MarksmanAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE,
                Mamy.id("marksman"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 4.0
        ));
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.HEADSHOT_DAMAGE,
                Mamy.id("marksman"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 2.0
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Marksman");
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle(
                "+4.0 Projectile Damage Persistent projectiles bypass target invulnerability frames, And shots to the head, deal 2x damage, enabling repeated damage."
        );
    }
}
