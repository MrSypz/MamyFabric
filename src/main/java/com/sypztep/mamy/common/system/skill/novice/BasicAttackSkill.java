package com.sypztep.mamy.common.system.skill.novice;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.system.classes.ClassRegistry;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class BasicAttackSkill extends Skill {

    public BasicAttackSkill() {
        super(Mamy.id("basic_attack"), "Basic Attack", "A simple melee attack",
                5f, 20, 1, ModClasses.NOVICE, 1);
    }

    @Override
    public boolean canUse(LivingEntity caster) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public void use(LivingEntity caster, int level) {
        if (!(caster instanceof PlayerEntity player)) return;

        // Simple area attack around player
        var nearbyEntities = player.getWorld().getEntitiesByClass(LivingEntity.class,
                player.getBoundingBox().expand(2.0),
                entity -> entity != player && entity.isAlive());

        for (LivingEntity target : nearbyEntities) {
            target.damage(player.getWorld().getDamageSources().playerAttack(player), 5.0f);
        }

        // Effects
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                    player.getX(), player.getY() + 1, player.getZ(),
                    5, 1.0, 0.5, 1.0, 0.1);
        }

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.NOVICE;
    }
}