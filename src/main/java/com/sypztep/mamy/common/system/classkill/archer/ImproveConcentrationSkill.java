package com.sypztep.mamy.common.system.classkill.archer;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModStatusEffects;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.List;

public class ImproveConcentrationSkill extends Skill {

    public ImproveConcentrationSkill(Identifier identifier, List<SkillRequirement> prerequisites) {
        super(identifier, "Improve Concentration", "Temporarily boosts AGI and DEX to enhance combat precision and reflexes.",
                20f, 2f,
                10,
                Mamy.id("skill/improve_concentration"), prerequisites);
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return super.getResourceCost(skillLevel) + (skillLevel * 5f); // SP Cost: 20 + (Skill Level × 5)
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = 0;
        data.damageType = DamageTypeRef.HEAL; // Use heal type for beneficial effects
        data.maxHits = 0; // Self-target, not hitting anyone

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;

        int durationTicks = (60 + (skillLevel * 20)) * 20; // Convert seconds to ticks (20 ticks = 1 second)

        StatusEffectInstance concentrationEffect = new StatusEffectInstance(ModStatusEffects.IMPROVE_CONCENTRATION, durationTicks, skillLevel - 1,false, false, false);

        player.addStatusEffect(concentrationEffect);

        // Visual and audio effects
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            for (int i = 0; i < 15; i++) {
                double offsetX = (player.getRandom().nextDouble() - 0.5) * 2.0;
                double offsetY = player.getRandom().nextDouble() * 2.0;
                double offsetZ = (player.getRandom().nextDouble() - 0.5) * 2.0;

                serverWorld.spawnParticles(ParticleTypes.ENCHANT, player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ, 1, 0, 0, 0, 0.1);
            }

            // Sound effect
            serverWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.7f, 1.2f);
        }

        return true;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ARCHER;
    }
}