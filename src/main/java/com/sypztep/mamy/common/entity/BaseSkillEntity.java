package com.sypztep.mamy.common.entity;

import com.google.common.collect.Sets;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import com.sypztep.mamy.common.system.skill.SkillConfig;
import com.sypztep.mamy.common.system.skill.SkillHitTracker;

import java.util.Iterator;
import java.util.Set;

public abstract class BaseSkillEntity extends PersistentProjectileEntity {
    protected final SkillConfig skillConfig;
    protected final SkillHitTracker hitTracker;
    private final Set<StatusEffectInstance> effects = Sets.newHashSet();
    private int ticksUntilRemove = 5;

    public BaseSkillEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world, SkillConfig config) {
        super(entityType, world);
        this.skillConfig = config;
        this.hitTracker = new SkillHitTracker(config.maxHitCount, config.bypassIframe, config.iframeTime);
    }

    public BaseSkillEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world, LivingEntity owner, SkillConfig config) {
        super(entityType, owner, world, ItemStack.EMPTY, null);
        this.skillConfig = config;
        this.hitTracker = new SkillHitTracker(config.maxHitCount, config.bypassIframe, config.iframeTime);
        this.setOwner(owner);
    }

    public void addEffect(StatusEffectInstance effect) {
        this.effects.add(effect);
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();

        // Custom tick behavior (particles, etc)
        onCustomTick();

        // Explosion logic when hitting ground or after time
        if (this.inGround || this.age > 20) {
            onSkillActivate();
            --this.ticksUntilRemove;
        }

        if (this.ticksUntilRemove <= 0) {
            onSkillEnd();
            this.discard();
        }

        // Hit detection with proper iframe and maxHit control
        if (!this.getWorld().isClient) {
            performAreaHitDetection();
        }
    }

    protected void performAreaHitDetection() {
        long currentTime = this.getWorld().getTime();

        for (LivingEntity target : this.getWorld().getEntitiesByClass(
                LivingEntity.class,
                this.getBoundingBox().expand(skillConfig.hitRange),
                (livingEntity) -> this.getOwner() != livingEntity && livingEntity.isAlive()
        )) {
            // Check if we can hit this entity (iframe and maxHit check)
            if (!hitTracker.canHit(target, currentTime)) {
                continue;
            }

            // Deal damage
            if (skillConfig.damage > 0) {
                target.damage(
                        getWorld().getDamageSources().create(skillConfig.damageType, this, getOwner()),
                        skillConfig.damage
                );
            }

            // Apply all effects
            for (StatusEffectInstance effect : this.effects) {
                target.addStatusEffect(effect);
            }

            // Record the hit
            hitTracker.recordHit(target, currentTime);

            // Custom hit behavior
            onEntityHit(target);
        }
    }

    @Override
    protected float getDragInWater() {
        return 0.99f;
    }

    @Override
    protected SoundEvent getHitSound() {
        return null; // Override in subclasses
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        // Empty - allows projectile to pass through
    }

    // Abstract methods for customization
    protected abstract void onCustomTick();
    protected abstract void onSkillActivate();
    protected abstract void onEntityHit(LivingEntity target);
    protected abstract void onSkillEnd();

    // Getters
    public SkillConfig getConfig() { return skillConfig; }
    public SkillHitTracker getHitTracker() { return hitTracker; }
    public Set<StatusEffectInstance> getEffects() { return effects; }
    public int getTicksUntilRemove() { return ticksUntilRemove; }
}