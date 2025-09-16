package com.sypztep.mamy.common.system.classkill.swordman.passive;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.PassiveSkill;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.List;

public class SwordMasteryPassiveSkill extends PassiveSkill {

    public SwordMasteryPassiveSkill(Identifier id) {
        super(id, "Sword Mastery", "Extensive training with bladed weapons increases your melee damage.",
                10,
                Mamy.id("skill/sword_mastery"));
    }

    @Override
    protected void initializePassiveEffects() {
        addAttributeModification(AttributeModification.addValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, Mamy.id("sword_mastery_damage"), skillLevel -> skillLevel * .5f // +0.5 damage per level
        ));
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();
        
        // Configure for passive skills - hide damage/resources/cooldown by default in the renderer
        data.hideDamage = true;
        data.hideResourceCost = true;
        data.hideCooldown = true;
        
        return data;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
}