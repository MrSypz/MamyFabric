package com.sypztep.mamy.common.system.classkill.thief;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.util.SkillUtil;
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
                ModClasses.THIEF, 1,
                1, 1,
                false, Mamy.id("skill/detoxify"), skillRequirements);
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return 10f;
    }

    @Override
    public float getCooldown(int skillLevel) {
        return 0f;
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        List<Text> tooltip = super.generateTooltip(player, skillLevel, isLearned, context);

        // Add description
        if (skillLevel > 0 || context == TooltipContext.LEARNING_SCREEN) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Removes:").formatted(Formatting.GOLD));
            tooltip.add(Text.literal("• Poison").formatted(Formatting.GREEN));
            tooltip.add(Text.literal("• Wither").formatted(Formatting.GREEN));

            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Target Range: ").formatted(Formatting.GRAY)
                    .append(Text.literal("9 blocks").formatted(Formatting.YELLOW)));
            tooltip.add(Text.literal("Cooldown: ").formatted(Formatting.GRAY)
                    .append(Text.literal("None").formatted(Formatting.YELLOW)));

            // Usage tip
            if (context == TooltipContext.LEARNING_SCREEN) {
                tooltip.add(Text.literal(""));
                tooltip.add(Text.literal("💡 Tip: ").formatted(Formatting.YELLOW)
                        .append(Text.literal("Quick poison and wither removal for yourself or allies").formatted(Formatting.GRAY)));
            }
        }

        return tooltip;
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = 0;
        data.damageType = DamageType.HEAL;
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
                    // Play effects
                    ServerWorld serverWorld = (ServerWorld) player.getWorld();

                    // Particle effects around target
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