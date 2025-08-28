package com.sypztep.mamy.common.statuseffect;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.StatModifierHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.tag.EntityTypeTags;

public class BlessingEffect extends StatusEffect {
    private static final String MODIFIER_SOURCE = "BlessingEffect";

    public BlessingEffect(StatusEffectCategory category) {
        super(category, 0);
        this.addAttributeModifier(ModEntityAttributes.ACCURACY, Mamy.id("blessing_hit_bonus"), 2.0D, EntityAttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);

        int statBonus = amplifier + 1;

        boolean isUndeadOrDemon = isUndeadOrDemon(entity);

        if (isUndeadOrDemon) {
            // For undead/demons: reduce their DEX and INT instead of buffing
            applyUndeadDebuff(entity, amplifier);
        } else {
            // Normal blessing effects: increase STR, DEX, INT
            StatModifierHelper.applyTemporaryModifier(entity, StatTypes.STRENGTH, MODIFIER_SOURCE + "_STR", (short) statBonus, true);
            StatModifierHelper.applyTemporaryModifier(entity, StatTypes.DEXTERITY, MODIFIER_SOURCE + "_DEX", (short) statBonus, true);
            StatModifierHelper.applyTemporaryModifier(entity, StatTypes.INTELLIGENCE, MODIFIER_SOURCE + "_INT", (short) statBonus, true);
        }
    }

    private void applyUndeadDebuff(LivingEntity entity, int amplifier) {
        int reductionAmount = (amplifier + 1) * 2; // More reduction at higher levels

        StatModifierHelper.applyTemporaryModifier(entity, StatTypes.DEXTERITY, MODIFIER_SOURCE + "_UNDEAD_DEX", (short) -reductionAmount, true);
        StatModifierHelper.applyTemporaryModifier(entity, StatTypes.INTELLIGENCE, MODIFIER_SOURCE + "_UNDEAD_INT", (short) -reductionAmount, true);
    }

    private boolean isUndeadOrDemon(LivingEntity entity) {
        if (entity.getType().isIn(EntityTypeTags.UNDEAD)) {
            return true;
        }
        String entityName = entity.getType().toString().toLowerCase();
        return entityName.contains("wither") || entityName.contains("blaze") || entityName.contains("ghast") || entityName.contains("demon") || entityName.contains("devil"); // If you have custom demon mobs
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        StatModifierHelper.removeTemporaryModifier(entity, StatTypes.STRENGTH, MODIFIER_SOURCE + "_STR", true);
        StatModifierHelper.removeTemporaryModifier(entity, StatTypes.DEXTERITY, MODIFIER_SOURCE + "_DEX", true);
        StatModifierHelper.removeTemporaryModifier(entity, StatTypes.INTELLIGENCE, MODIFIER_SOURCE + "_INT", true);

        // Remove undead debuffs
        StatModifierHelper.removeTemporaryModifier(entity, StatTypes.DEXTERITY, MODIFIER_SOURCE + "_UNDEAD_DEX", true);
        StatModifierHelper.removeTemporaryModifier(entity, StatTypes.INTELLIGENCE, MODIFIER_SOURCE + "_UNDEAD_INT", true);

        return super.applyUpdateEffect(entity, amplifier);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration <= 1;
    }
}