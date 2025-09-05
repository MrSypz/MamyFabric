package com.sypztep.mamy.common.system.classkill.archer.passive;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.PassiveSkill;
import com.sypztep.mamy.common.util.AttributeModification;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class OwlsEyePassiveSkill extends PassiveSkill {
    private final float PJ_DAMAGE = 0.02f;
    public OwlsEyePassiveSkill(Identifier id) {
        super(id, "Owl's Eye", "Increases Accuracy by up to 10. and projectile inflict more damage", ModClasses.ARCHER, 1, 1, 10, false, Mamy.id("skill/passive/owls_eye"));
    }

    @Override
    protected void initializePassiveEffects() {
        addAttributeModification(AttributeModification.addValue(ModEntityAttributes.ACCURACY, Mamy.id("owls_eye_dex_bonus"), skillLevel -> skillLevel));
        addAttributeModification(AttributeModification.addValue(ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE_FLAT, Mamy.id("owls_eye_dex_bonus"), skillLevel -> (PJ_DAMAGE * skillLevel)));
    }

    @Override
    protected void addPassiveEffectsDescription(List<Text> tooltip, int skillLevel) {
        tooltip.add(Text.literal("Accuracy: +" + skillLevel).formatted(Formatting.GREEN));
        tooltip.add(Text.literal("Projectile Damage: +" + String.format("%.1f",(PJ_DAMAGE * skillLevel))).formatted(Formatting.GREEN));
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ARCHER;
    }
}