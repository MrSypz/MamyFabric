package com.sypztep.mamy.common.system.passive.abilities.agility;

import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public class ShadowStepPassiveAbility extends PassiveAbility {

    public ShadowStepPassiveAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {

    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Shadow Step").formatted(Formatting.GREEN);
    }

    @Override
    public Text getDescription() {
        return Text.literal("No longer have footstep sound");
    }
}