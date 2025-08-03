package com.sypztep.mamy.common.entity;

import com.google.common.collect.Sets;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import com.sypztep.mamy.common.system.skill.config.SkillConfig;
import com.sypztep.mamy.common.system.skill.SkillHitTracker;
import com.sypztep.mamy.common.init.ModEntityComponents;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class BaseSkillEntity extends PersistentProjectileEntity {
    public final SkillConfig skillConfig;
    protected final SkillHitTracker hitTracker;
    private final Set<StatusEffectInstance> effects = Sets.newHashSet();
    private int ticksUntilRemove = 5;

    private float manaRegenPerHit = 0.0f;
    private float healthRegenPerHit = 0.0f;

    public BaseSkillEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world, SkillConfig config) {
        super(entityType, world);
        this.skillConfig = config;
        this.hitTracker = new SkillHitTracker(config.maxHitCount(), config.iframeTime());
    }

    public BaseSkillEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world, LivingEntity owner, SkillConfig config, @Nullable ItemStack shotFrom) {
        super(entityType, owner, world, ItemStack.EMPTY, shotFrom);
        this.skillConfig = config;
        this.hitTracker = new SkillHitTracker(config.maxHitCount(), config.iframeTime());
        this.setOwner(owner);
    }

    // Simplified configuration methods
    public BaseSkillEntity withManaRegen(float manaPerHit) {
        this.manaRegenPerHit = manaPerHit;
        return this;
    }

    public BaseSkillEntity withHealthRegen(float healthPerHit) {
        this.healthRegenPerHit = healthPerHit;
        return this;
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
            this.getHitTracker().reset();
            this.discard();
        }

        // Hit detection with proper iframe and maxHit control
        if (!this.getWorld().isClient) {
            performAreaHitDetection();
        }
    }

    protected void performAreaHitDetection() {
        long currentTime = this.getWorld().getTime();

        // Create hit detection box based on config
        Box entityBox = this.getBoundingBox();
        Box hitDetectionBox;

        if (skillConfig.useCustomBox()) {
            // Custom dimensions
            hitDetectionBox = entityBox.expand(
                    skillConfig.hitWidth(),
                    skillConfig.hitHeight(),
                    skillConfig.hitDepth()
            );
        } else {
            hitDetectionBox = entityBox.expand(skillConfig.hitRange());
        }

        for (LivingEntity target : this.getWorld().getEntitiesByClass(
                LivingEntity.class,
                hitDetectionBox,
                (livingEntity) -> this.getOwner() != livingEntity && livingEntity.isAlive()
        )) {
            if (!hitTracker.canHit(target, currentTime)) {
                continue;
            }

            // Deal damage
            if (skillConfig.damage() > 0) {
                target.damage(
                        getWorld().getDamageSources().create(skillConfig.damageType(), this, getOwner()),
                        skillConfig.damage()
                );
            }

            // Apply all effects
            for (StatusEffectInstance effect : this.effects) {
                target.addStatusEffect(effect);
            }

            // Record the hit
            hitTracker.recordHit(target, currentTime);

            // Built-in regeneration system
            handleRegeneration(target);

            // Custom hit behavior (for subclasses to override if needed)
            onSuccessfulHit(target);
        }
    }

    /**
     * Get the hit detection box for debug rendering
     */
    public Box getHitDetectionBox() {
        Box entityBox = this.getBoundingBox();

        if (skillConfig.useCustomBox()) {
            return entityBox.expand(
                    skillConfig.hitWidth(),
                    skillConfig.hitHeight(),
                    skillConfig.hitDepth()
            );
        } else {
            return entityBox.expand(skillConfig.hitRange());
        }
    }

    private void handleRegeneration(LivingEntity target) {
        if (!(getOwner() instanceof LivingEntity caster)) return;

        int hitCount = hitTracker.getHitCount(target);

        if (manaRegenPerHit > 0) {
            var playerClass = ModEntityComponents.PLAYERCLASS.getNullable(caster);
            if (playerClass != null) {
                playerClass.addResource(hitCount * manaRegenPerHit);
            }
        }

        if (healthRegenPerHit > 0) {
            caster.heal(hitCount * healthRegenPerHit);
        }
    }

    @Override
    protected float getDragInWater() {
        return 0.99f;
    }

    @Override
    protected SoundEvent getHitSound() {
        return null;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
    }

    // Abstract methods for subclasses
    protected abstract void onCustomTick();
    protected abstract void onSkillActivate();
    protected abstract void onSkillEnd();

    /**
     * Called after a successful hit (after damage, effects, and regen are applied)
     * Override this for custom hit behavior
     */
    protected void onSuccessfulHit(LivingEntity target) {
    }

    public Set<StatusEffectInstance> getEffects() { return effects; }
    public SkillHitTracker getHitTracker() { return hitTracker; }
    public float getManaRegenPerHit() { return manaRegenPerHit; }
    public float getHealthRegenPerHit() { return healthRegenPerHit; }
}