package com.sypztep.mamy.common.system.classkill.acolyte;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModStatusEffects;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import com.sypztep.mamy.common.util.SkillUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class DecreaseAgilitySkill extends Skill implements CastableSkill {

    public DecreaseAgilitySkill(Identifier identifier, List<SkillRequirement> skillRequirements) {
        super(identifier, "Decrease Agility", "Temporarily decrease AGI, Attack Speed and Movement Speed of target.",
                13f, 1f,
                10,
                Mamy.id("skill/decrease_agility"),skillRequirements);
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return super.getResourceCost(skillLevel) + (skillLevel * 2f);
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return 15; // 0.75 seconds
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 5; // 0.25 seconds
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
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        // This is a debuff skill
        data.baseDamage = 0;
        data.damageType = DamageTypeRef.MAGIC; // Use dark type for debuffs
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
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        LivingEntity target = SkillUtil.findTargetEntity(player, 9);
        if (target == null) return false;

        float successRate = calculateSuccessRate(player, target, skillLevel);

        if (LivingEntityUtil.roll(player) > successRate) {
            serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    5, 0.2, 0.2, 0.2, 0.05);
            serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.4f, 1.2f);
            return false; // Skill failed
        }

        int duration;
        if (target instanceof PlayerEntity) {
            duration = (20 + (skillLevel * 5)) * 20; // vs Players: 20s + 5s per level
        } else {
            duration = (30 + (skillLevel * 10)) * 20; // vs Monsters: 30s + 10s per level
        }
        StatusEffectInstance agilityDebuff = new StatusEffectInstance(
                ModStatusEffects.DECREASE_AGILITY,
                duration,
                skillLevel - 1,
                false,
                false,
                true // Show particles
        );

        boolean success = target.addStatusEffect(agilityDebuff);

        if (success) {
            // Dark/harmful particles for debuff
            serverWorld.spawnParticles(ParticleTypes.SMOKE,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    12, 0.4, 0.4, 0.4, 0.1);
            serverWorld.spawnParticles(ParticleTypes.ASH,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    8, 0.3, 0.3, 0.3, 0.05);
            serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ENTITY_WITCH_AMBIENT, SoundCategory.PLAYERS, 0.6f, 0.8f);
        }

        return success;
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        List<Text> tooltip = super.generateTooltip(player, skillLevel, isLearned, context);

        // Add debuff details to tooltip
        if (skillLevel > 0) {
            int agiReduction = 3 + (skillLevel - 1);
            int durationVsMonsters = 30 + (skillLevel * 10);
            int durationVsPlayers = 20 + (skillLevel * 5);

            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Debuff Effects:").formatted(Formatting.DARK_RED));
            tooltip.add(Text.literal("• AGI: ").formatted(Formatting.GRAY)
                    .append(Text.literal("-" + agiReduction).formatted(Formatting.RED)));
            tooltip.add(Text.literal("• Attack Speed: ").formatted(Formatting.GRAY)
                    .append(Text.literal("-" + skillLevel + "%").formatted(Formatting.RED)));
            tooltip.add(Text.literal("• Movement Speed: ").formatted(Formatting.GRAY)
                    .append(Text.literal("-5% per level").formatted(Formatting.RED)));
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Duration:").formatted(Formatting.YELLOW));
            tooltip.add(Text.literal("• vs Monsters: ").formatted(Formatting.GRAY)
                    .append(Text.literal(durationVsMonsters + "s").formatted(Formatting.YELLOW)));
            tooltip.add(Text.literal("• vs Players: ").formatted(Formatting.GRAY)
                    .append(Text.literal(durationVsPlayers + "s").formatted(Formatting.YELLOW)));
        }

        return tooltip;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ACOLYTE;
    }

    /**
     * Calculate success rate using: Success Rate = [BaseRate + (Caster_BaseLvl + Caster_Int) / 5 - Target_MDEF]%
     */
    private float calculateSuccessRate(PlayerEntity caster, LivingEntity target, int skillLevel) {
        float baseRate = (50 + (skillLevel * 3)) / 100.0f; // Convert to 0.0-1.0 range
        var casterLevel = ModEntityComponents.LIVINGLEVEL.get(caster);
        int casterBaseLevel = casterLevel.getLevel();
        int casterIntelligence = casterLevel.getStatValue(StatTypes.INTELLIGENCE);
        float targetMDEF = (float) target.getAttributeValue(ModEntityAttributes.MAGIC_RESISTANCE);
        float successRate = baseRate + ((casterBaseLevel + casterIntelligence) / 5.0f) - targetMDEF;
        return MathHelper.clamp(successRate, 0.0f, 1.0f);
    }
}