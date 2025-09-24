package com.sypztep.mamy.common.system.classkill.acolyte;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModItems;
import com.sypztep.mamy.common.init.ModSoundEvents;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.List;

public class AquaBenedictaSkill extends Skill implements CastableSkill {

    public AquaBenedictaSkill(Identifier identifier) {
        super(identifier, "AquaBenedicta", "Creates Holy Water with 1 Empty Bottle while standing in water.",
                10f, 0.5f,
                1,
                Mamy.id("skill/aquabenedicta"));
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return 16;
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 4;
    }

    @Override
    public boolean shouldLockMovement() {
        return true;
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = 0;
        data.damageType = DamageTypeRef.HEAL; // Use heal type for beneficial effects
        data.maxHits = 1;

        return data;
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        List<Text> tooltip = super.generateTooltip(player, skillLevel, isLearned, context);
        tooltip.add(Text.of("Convert Water into Holy Water"));
        return tooltip;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive() &&
                (caster.isTouchingWater() || caster.isSubmergedInWater());
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(player.getWorld() instanceof ServerWorld)) return false;

        ItemStack handStack = player.getMainHandStack();
        PotionContentsComponent potionContentsComponent = handStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);

        if (potionContentsComponent.matches(Potions.WATER)) {
            ItemStack holyWaterStack = new ItemStack(ModItems.HOLY_WATER, handStack.getCount());
            player.setStackInHand(Hand.MAIN_HAND, holyWaterStack);
            player.playSound(ModSoundEvents.ENTITY_GENERIC_AHHH);
            return true;
        }
        return false;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ACOLYTE;
    }
}
