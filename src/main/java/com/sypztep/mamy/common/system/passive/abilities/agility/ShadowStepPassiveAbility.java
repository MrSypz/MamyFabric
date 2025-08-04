package com.sypztep.mamy.common.system.passive.abilities.agility;

import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
// === SHADOW STEP ABILITIES ===
// Stats Level Required: AGILITY: 50
// Passive Description: No longer have footstep sound

public class ShadowStepPassiveAbility extends PassiveAbility {
    public ShadowStepPassiveAbility() {
        super("shadow_step", Map.of(StatTypes.AGILITY, 50));
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