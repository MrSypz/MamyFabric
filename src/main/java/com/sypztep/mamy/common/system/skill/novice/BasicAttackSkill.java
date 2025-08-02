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
                5f, 2f, ModClasses.NOVICE, 0, 2, 5); // 0 cost to learn, 2 points per upgrade, max level 5
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public void use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return;

        // Damage scales with skill level
        float damage = 3.0f + (skillLevel * 2.0f);
        double range = 1.5 + (skillLevel * 0.5); // Range increases with level

        var nearbyEntities = player.getWorld().getEntitiesByClass(LivingEntity.class,
                player.getBoundingBox().expand(range),
                entity -> entity != player && entity.isAlive());

        for (LivingEntity target : nearbyEntities) {
            target.damage(player.getWorld().getDamageSources().playerAttack(player), damage);
        }

        // Effects scale with skill level
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            int particleCount = 3 + skillLevel;
            serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                    player.getX(), player.getY() + 1, player.getZ(),
                    particleCount, range * 0.5, 0.5, range * 0.5, 0.1);
        }

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS,
                0.8f + (skillLevel * 0.1f), 1.0f + (skillLevel * 0.1f));
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return true; // Available to all classes
    }

    @Override
    public String getDescription(int skillLevel) {
        float damage = 3.0f + (skillLevel * 2.0f);
        double range = 1.5 + (skillLevel * 0.5);
        return String.format("Deals %.1f damage in a %.1f block radius (Level %d)",
                damage, range, skillLevel);
    }
}