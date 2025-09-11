package com.sypztep.mamy.common.system.classkill.swordman;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModStatusEffects;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.particle.ParticleTypes;

import java.util.List;

public class EndureSkill extends Skill {

    public EndureSkill(Identifier identifier, List<SkillRequirement> skillRequirements) {
        super(identifier, "Endure", "Temporarily increase your armor for enhanced defense",
                10f, 10f,
                10,
                 Mamy.id("skill/endure"), skillRequirements);
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        // This is a buff skill, not a damage skill
        data.baseDamage = 0;
        data.damageType = DamageType.PHYSICAL;
        data.maxHits = 0;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        return player.isAlive() && !player.hasStatusEffect(ModStatusEffects.ENDURE);
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        int duration = (10 + (3 * skillLevel)) * 20; // Convert seconds to ticks

        // Amplifier = skillLevel - 1 (so level 1 = amplifier 0 = +1 armor)
        StatusEffectInstance endureEffect = new StatusEffectInstance(
                ModStatusEffects.ENDURE,
                duration,
                skillLevel - 1, // Amplifier for armor bonus
                false, // Not ambient
                false,  // Show particles
                false   // Show icon
        );

        player.addStatusEffect(endureEffect);

        // Create protective aura particles
        for (int i = 0; i < 34; i++) {
            double angle = (Math.PI * 2 * i) / 20;
            double x = player.getX() + Math.cos(angle) * 1.5;
            double z = player.getZ() + Math.sin(angle) * 1.5;
            serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                    x, player.getY() + 1, z,
                    1, 0.1, 0.1, 0.1, 0.02);
        }

        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS,
                0.8f, 1.5f);

        return true;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
}
