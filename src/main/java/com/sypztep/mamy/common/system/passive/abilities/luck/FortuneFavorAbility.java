package com.sypztep.mamy.common.system.passive.abilities.luck;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sypztep.tyrannus.common.util.AttributeModification;

import java.util.Map;

public class FortuneFavorAbility extends PassiveAbility {
    public FortuneFavorAbility(String id, Map<StatTypes, Integer> requirements) {
    super(id, requirements);
}

    @Override
    protected void initializeEffects() {
        addAttributeEffect(new AttributeModification(
                ModEntityAttributes.SPECIAL_ATTACK,
                Mamy.id("fortune_favor_special_damage"),
                EntityAttributeModifier.Operation.ADD_VALUE,
                baseValue -> 0.15 // +15% special damage
        ));
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Fortune's Favor").formatted(Formatting.YELLOW);
    }

    @Override
    public Text getDescription() {
        return Text.literal("Fortune’s blessing sharpens your strikes, letting you deal +15% Special Damage.");
    }
}
