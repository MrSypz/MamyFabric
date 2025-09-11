package com.sypztep.mamy.common.system.classkill.acolyte;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.util.SkillUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class CureSkill extends Skill implements CastableSkill {

    public CureSkill(Identifier identifier, List<SkillRequirement> skillRequirements) {
        super(identifier, "Cure", "Removes poison, blindness, confusion and other negative status effects from target.",
                15f, 6f,
                1, // Max level 1
                Mamy.id("skill/cure"), skillRequirements);
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return 20; // 1 second (20 ticks)
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 0; // No fixed cast time
    }

    @Override
    public boolean shouldLockMovement() {
        return true;
    }

    @Override
    public Identifier getCastAnimation() {
        return Mamy.id("pray");
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        // Find target using raycast (same targeting as heal)
        LivingEntity target = SkillUtil.findTargetEntity(player, 9);
        if (target == null) target = player;

        // List of negative status effects to remove
        List<RegistryEntry<StatusEffect>> negativeEffects = List.of(
                StatusEffects.POISON,
                StatusEffects.WITHER,
                StatusEffects.BLINDNESS,
                StatusEffects.NAUSEA
        );

        boolean removedAny = false;

        // Remove all negative status effects
        for (RegistryEntry<StatusEffect> effect : negativeEffects) {
            if (target.hasStatusEffect(effect)) {
                target.removeStatusEffect(effect);
                removedAny = true;
            }
        }

        if (removedAny) {
            serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    12, 0.5, 0.5, 0.5, 0.1);
            serverWorld.spawnParticles(ParticleTypes.END_ROD,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    6, 0.3, 0.3, 0.3, 0.05);

            serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.5f, 1.5f);
        } else {
            serverWorld.spawnParticles(ParticleTypes.POOF,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    5, 0.2, 0.2, 0.2, 0.05);

            // Neutral sound
            serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 0.3f, 1.2f);
        }

        return true;
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        List<Text> tooltip = super.generateTooltip(player, skillLevel, isLearned, context);

        // Add detailed effect information
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("Removes Status Effects:").formatted(Formatting.GOLD));
        tooltip.add(Text.literal("• Poison & Wither").formatted(Formatting.GREEN));
        tooltip.add(Text.literal("• Blindness").formatted(Formatting.GREEN));
        tooltip.add(Text.literal("• Nausea").formatted(Formatting.GREEN));

        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("Target Range: ").formatted(Formatting.GRAY)
                .append(Text.literal("9 blocks").formatted(Formatting.YELLOW)));
        tooltip.add(Text.literal("Cooldown: ").formatted(Formatting.GRAY)
                .append(Text.literal("6 seconds").formatted(Formatting.YELLOW)));

        // Usage tip
        if (context == TooltipContext.LEARNING_SCREEN) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("💡 Tip: ").formatted(Formatting.YELLOW)
                    .append(Text.literal("Essential support skill for removing debuffs").formatted(Formatting.GRAY)));
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
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ACOLYTE;
    }
}