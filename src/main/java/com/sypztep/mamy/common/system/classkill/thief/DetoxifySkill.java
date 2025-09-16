package com.sypztep.mamy.common.system.classkill.thief;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.util.SkillUtil;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.ResourceType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class DetoxifySkill extends Skill {

    public DetoxifySkill(Identifier identifier, List<SkillRequirement> skillRequirements) {
        super(identifier, "Detoxify", "Cures any target within range of Poison and Wither status effects.",
                10f, 0f,
                1,
                Mamy.id("skill/detoxify"), skillRequirements);
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        SkillTooltipData data = getSkillTooltipData(player, skillLevel);
        
        // Configure tooltip data
        data.effects.add("Removes Poison");
        data.effects.add("Removes Wither");
        data.rangeInfo = "9 blocks";
        data.cooldownOverride = "None";
        data.tip = "Quick poison and wither removal for yourself or allies";

        List<Text> tooltip = SkillTooltipRenderer.render(player, skillLevel, isLearned, context, name, description, maxSkillLevel, data);
        
        // Add resource info manually since renderer doesn't handle it yet
        if (!data.hideResourceCost && (skillLevel > 0 || context == TooltipContext.LEARNING_SCREEN)) {
            PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
            ResourceType resourceType = classComponent.getClassManager().getResourceType();

            float cost = getResourceCost(skillLevel);
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Require Resource: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.0f", cost)).formatted(Formatting.YELLOW))
                    .append(Text.literal(" " + resourceType.getDisplayName()).formatted(Formatting.GRAY)));
        }

        // Context-specific info
        addContextInfo(tooltip, player, skillLevel, isLearned, context);

        return tooltip;
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = 0;
        data.damageType = DamageTypeRef.HEAL;
        data.maxHits = 1;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!player.getWorld().isClient()) {
            LivingEntity target = SkillUtil.findTargetEntity(player, 9f);

            if (target != null && target.isAlive()) {
                boolean cured = false;

                // Remove poison effect
                if (target.hasStatusEffect(StatusEffects.POISON)) {
                    target.removeStatusEffect(StatusEffects.POISON);
                    cured = true;
                }

                // Remove wither effect
                if (target.hasStatusEffect(StatusEffects.WITHER)) {
                    target.removeStatusEffect(StatusEffects.WITHER);
                    cured = true;
                }

                if (cured) {
                    ServerWorld serverWorld = (ServerWorld) player.getWorld();

                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                            target.getX(), target.getY() + 1, target.getZ(),
                            10, 0.5, 0.5, 0.5, 0.1);

                    // Sound effect
                    serverWorld.playSound(null, target.getBlockPos(),
                            SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.PLAYERS,
                            1.0f, 1.5f);

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.THIEF;
    }
}