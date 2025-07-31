package com.sypztep.mamy.common.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import com.sypztep.mamy.common.init.*;
import com.sypztep.mamy.common.system.skill.SkillConfig;

public class BloodLustEntity extends BaseSkillEntity {

    public BloodLustEntity(World world, LivingEntity owner) {
        super(ModEntityTypes.BLOOD_LUST, world, owner, createBloodLustConfig());
    }

    public BloodLustEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world, createBloodLustConfig());
    }

    private static SkillConfig createBloodLustConfig() {
        return new SkillConfig.Builder()
                .damage(4F, ModDamageTypes.BLOODLUST)
                .hitRange(3.0)
                .maxHits(5) // Each enemy can only be hit once
                .iframeTime(2) // 0.5 second iframe
                .endOnMaxHits(true) // Continue flying even after hitting
                .build();
    }

    @Override
    protected void onCustomTick() {
        // Blood trail particles
        for (float x = -3.0F; x <= 3.0F; x = (float)((double)x + 0.1)) {
            this.getWorld().addParticle(
                    ModParticles.BLOOD_BUBBLE,
                    this.getX() + (double)x * Math.cos(this.getYaw()),
                    this.getY(),
                    this.getZ() + (double)x * Math.sin(this.getYaw()),
                    this.getVelocity().getX(),
                    this.getVelocity().getY(),
                    this.getVelocity().getZ()
            );
        }
    }

    @Override
    protected void onSkillActivate() {
        // Blood explosion particles
        for(int i = 0; i < 50; ++i) {
            this.getWorld().addParticle(
                    ModParticles.BLOOD_BUBBLE_SPLATTER,
                    this.getX() + this.random.nextGaussian() * 2.0 * Math.cos(this.getYaw()),
                    this.getY(),
                    this.getZ() + this.random.nextGaussian() * 2.0 * Math.sin(this.getYaw()),
                    this.random.nextGaussian() / 10.0,
                    this.random.nextFloat() / 2.0F,
                    this.random.nextGaussian() / 10.0
            );
        }
    }

    @Override
    protected void onEntityHit(LivingEntity target) {
        // Play hit sound
        if (getHitSound() != null) {
            getWorld().playSound(null, getX(), getY(), getZ(), getHitSound(), getSoundCategory(), 1.0F, 1.0F);
        }
    }

    @Override
    protected void onSkillEnd() {
        // Cleanup
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    protected SoundEvent getHitSound() {
        return ModSoundEvents.ENTITY_GENERIC_BLOODHIT;
    }
}