package com.sypztep.mamy.common.entity.entity.skill;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import com.sypztep.mamy.common.init.*;
import com.sypztep.mamy.common.system.skill.config.SkillConfig;

public class BloodLustEntity extends BaseSkillEntity {

    public BloodLustEntity(World world, LivingEntity owner, SkillConfig config) {
        super(ModEntityTypes.BLOOD_LUST, world, owner, config,null);
        this.withManaRegen(2.0f)
                .withHealthRegen(0.5f);
    }

    public BloodLustEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world, createDefaultBloodLustConfig());
        this.withManaRegen(2.0f)
                .withHealthRegen(0.5f);
    }

    private static SkillConfig createDefaultBloodLustConfig() {
        return new SkillConfig.Builder()
                .damage(4F) // Default base damage
                .damageType(ModDamageTypes.BLOODLUST)
                .slashHitBox(5,0.2f)
                .maxHitCount(5)
                .iframeTime(2)
                .build();
    }

    @Override
    protected void onCustomTick() {
        for (float x = -1.5F; x <= 1.5F; x = (float)((double)x + 0.1)) {
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
        for(int i = 0; i < 30; ++i) {
            this.getWorld().addParticle(
                    ModParticles.BLOOD_BUBBLE_SPLATTER,
                    this.getX() + this.random.nextGaussian() * 1.5 * Math.cos(this.getYaw()),
                    this.getY(),
                    this.getZ() + this.random.nextGaussian() * 1.5 * Math.sin(this.getYaw()),
                    this.random.nextGaussian() / 10.0,
                    this.random.nextFloat() / 2.0F,
                    this.random.nextGaussian() / 10.0
            );
        }
    }

    @Override
    protected void onSkillEnd() {
        if (getHitSound() != null) {
            getWorld().playSound(null, getX(), getY(), getZ(), getHitSound(), getSoundCategory(), 1.0F, 1.0F);
        }
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