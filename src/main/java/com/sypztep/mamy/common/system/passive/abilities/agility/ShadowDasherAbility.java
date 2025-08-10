package com.sypztep.mamy.common.system.passive.abilities.agility;

import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public class ShadowDasherAbility extends PassiveAbility {
    public ShadowDasherAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() { }

    @Override
    public Text getDisplayName() {
        return Text.literal("Shadow Dasher").formatted(Formatting.DARK_GRAY);
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Vanish and reappear in an instant, dashing forward in the blink of an eye.");
    }
}
