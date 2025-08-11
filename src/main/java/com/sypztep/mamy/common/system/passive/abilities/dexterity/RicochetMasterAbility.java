package com.sypztep.mamy.common.system.passive.abilities.dexterity;

import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.TextStyleHelper;
import net.minecraft.text.Text;

import java.util.Map;

public class RicochetMasterAbility extends PassiveAbility {
    public RicochetMasterAbility(String id, Map<StatTypes, Integer> requirements) {
        super(id, requirements);
    }

    @Override
    protected void initializeEffects() {
    }


    @Override
    public Text getDisplayName() {
        return Text.literal("Ricochet Master");
    }

    @Override
    public Text getDescription() {
        return TextStyleHelper.autoStyle("Your projectiles bounce off surfaces and seek enemies after bouncing, allowing trick shots around corners.");
    }
}