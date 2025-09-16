package com.sypztep.mamy.common.system.classkill.swordman;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModStatusEffects;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.util.SkillUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class ProvokeSkill extends Skill {

    public ProvokeSkill(Identifier identifier) {
        super(identifier, "Provoke", "Taunt an enemy, increasing their attack while reducing their defense",
                4f, 2f,
                10,
                Mamy.id("skill/provoke"));
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return super.getResourceCost(skillLevel) + skillLevel;
    }


    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = 0;
        data.damageType = DamageTypeRef.PHYSICAL;
        data.maxHits = 0;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        // Find target
        LivingEntity target = SkillUtil.findTargetEntity(player,8);
        if (target == null) return false;

        // Calculate success rate (53% + level - 1)
        int successRate = 53 + skillLevel - 1;
        if (player.getRandom().nextInt(100) >= successRate) {
            // Failed to provoke
            failureEffect(serverWorld, player, target);
            return true; // Skill was used but failed
        }

        // Success - apply provoke effects
        applyProvokeEffects(serverWorld, player, target, skillLevel);

        return true;
    }

    private void applyProvokeEffects(ServerWorld world, PlayerEntity player, LivingEntity target, int skillLevel) {
        int duration = 30 * 20; // 30 seconds in ticks

        // Apply the ProvokeEffect status effect
        StatusEffectInstance provokeEffect = new StatusEffectInstance(
                ModStatusEffects.PROVOKE, // You'll need to add this to ModStatusEffects
                duration,
                skillLevel - 1, // Amplifier (level 1 = amplifier 0)
                false, // Not ambient
                true,  // Show particles
                true   // Show icon
        );

        target.addStatusEffect(provokeEffect);

        // Make the target focus on the player (set target if it's a hostile entity)
        if (target instanceof HostileEntity hostileEntity) hostileEntity.setTarget(player);

        // Success effects
        successEffect(world, player, target);
    }

    private void successEffect(ServerWorld world, PlayerEntity player, LivingEntity target) {
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_WOLF_GROWL, SoundCategory.PLAYERS,
                1.0f, 1.2f);

        // Target gets angry sound
        world.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ENTITY_HOSTILE_HURT, SoundCategory.HOSTILE,
                0.8f, 0.8f);
    }

    private void failureEffect(ServerWorld world, PlayerEntity player, LivingEntity target) {
        // Failure particles (less impressive)
        world.spawnParticles(ParticleTypes.SMOKE,
                target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                5, 0.3, 0.3, 0.3, 0.05);

        // Failure sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.PLAYERS,
                0.5f, 0.8f);
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        List<Text> tooltip = super.generateTooltip(player, skillLevel, isLearned, context);

        if (skillLevel > 0 || context == TooltipContext.LEARNING_SCREEN) {
            tooltip.add(Text.literal("")); // spacing

            tooltip.add(Text.literal("Provoke Effects:").formatted(Formatting.YELLOW, Formatting.BOLD));
            int atkBonus = 5 + (skillLevel - 1) * 3;
            int defReduction = 10 + (skillLevel - 1) * 5;
            int successRate = 53 + skillLevel - 1;

            tooltip.add(Text.literal("• Target ATK: +" + atkBonus + "%").formatted(Formatting.RED));
            tooltip.add(Text.literal("• Target DEF: -" + defReduction + "%").formatted(Formatting.GREEN));
            tooltip.add(Text.literal("• Success Rate: " + successRate + "%").formatted(Formatting.GOLD));
            tooltip.add(Text.literal("• Duration: 30 seconds").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("• Range: 8 blocks").formatted(Formatting.GRAY));

            if (skillLevel < getMaxSkillLevel()) {
                tooltip.add(Text.literal(""));
                tooltip.add(Text.literal("Next Level:").formatted(Formatting.DARK_GRAY));
                int nextAtkBonus = 5 + skillLevel * 3;
                int nextDefReduction = 10 + skillLevel * 5;
                int nextSuccessRate = 53 + skillLevel;
                tooltip.add(Text.literal("• Target ATK: +" + nextAtkBonus + "%").formatted(Formatting.RED));
                tooltip.add(Text.literal("• Target DEF: -" + nextDefReduction + "%").formatted(Formatting.GREEN));
                tooltip.add(Text.literal("• Success Rate: " + nextSuccessRate + "%").formatted(Formatting.GOLD));
            }

            if (context == TooltipContext.LEARNING_SCREEN) {
                tooltip.add(Text.literal(""));
                tooltip.add(Text.literal("💡 Tip: ").formatted(Formatting.YELLOW)
                        .append(Text.literal("Use on strong enemies to protect allies or bait attacks. Beware: They are not gonna like you again!").formatted(Formatting.GRAY)));
            }
        }
        return tooltip;
    }
}