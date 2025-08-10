package com.sypztep.mamy.common.system.passive.abilities.agility;

import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public class ShadowStepAbility extends PassiveAbility {
    public ShadowStepAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() { }

    @Override
    public Text getDisplayName() {
        return Text.literal("Shadow Step").formatted(Formatting.DARK_GRAY);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("No longer have footstep sound");
    }
}
