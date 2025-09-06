package com.sypztep.mamy.common.entity.skill;

import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class ArrowStrafeEntity extends PersistentProjectileEntity {
    private static final int MAX_LIFETIME = 100;
    private static final int DAMAGE_DELAY_TICKS = 4; // 0.2 seconds

    private final float baseDamage;
    private final float damageMultiplier;
    private final List<StatusEffectInstance> arrowEffects;
    private final LivingEntity targetEntity;

    private int ticksAlive = 0;
    private int arrowsFired = 0;

    public ArrowStrafeEntity(World world, LivingEntity target, float baseDamage, float damageMultiplier, List<StatusEffectInstance> arrowEffects) {
        super(ModEntityTypes.ARROW_STRAFE, world);
        this.targetEntity = target;
        this.baseDamage = baseDamage;
        this.damageMultiplier = damageMultiplier;
        this.arrowEffects = arrowEffects != null ? arrowEffects : List.of();
        this.setVelocity(Vec3d.ZERO);
    }

    public ArrowStrafeEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
        this.targetEntity = null;
        this.baseDamage = 0f;
        this.damageMultiplier = 1.0f;
        this.arrowEffects = List.of();
    }

    @Override
    protected ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();
        ticksAlive++;

        if (targetEntity != null && targetEntity.isAlive()) {
            double distanceToTarget = this.squaredDistanceTo(targetEntity);

            if (arrowsFired == 0 && (ticksAlive == 1 || distanceToTarget < 0.5f)) {
                fireArrow(false);
                arrowsFired++;
            } else if (arrowsFired == 1 && ticksAlive >= DAMAGE_DELAY_TICKS) {
                fireArrow(true);
                arrowsFired++;
            }
        }

        if (arrowsFired >= 2 && ticksAlive >= DAMAGE_DELAY_TICKS + 10) this.discard();

        if (ticksAlive >= MAX_LIFETIME) this.discard();

    }

    private void fireArrow(boolean isSecondArrow) {
        if (this.getWorld().isClient || targetEntity == null || !targetEntity.isAlive()) return;

        // Calculate damage
        float finalDamage = baseDamage * damageMultiplier;

        // Deal damage directly to target
        boolean damageDealt = targetEntity.damage(
                getWorld().getDamageSources().create(ModDamageTypes.ARROW_RAIN, this, getOwner()),
                finalDamage
        );

        if (damageDealt) {
            if (!arrowEffects.isEmpty()) {
                for (StatusEffectInstance effect : arrowEffects) {
                    StatusEffectInstance newEffect = new StatusEffectInstance(
                            effect.getEffectType(),
                            effect.getDuration(),
                            effect.getAmplifier(),
                            effect.isAmbient(),
                            effect.shouldShowParticles(),
                            effect.shouldShowIcon()
                    );
                    targetEntity.addStatusEffect(newEffect);
                }
            }

            // Play hit sound with different pitch for second arrow
            float pitch = isSecondArrow ? 1.2f : 1.0f;
            getWorld().playSound(null, targetEntity.getBlockPos(),
                    SoundEvents.ENTITY_ARROW_HIT, SoundCategory.PLAYERS,
                    0.8f, pitch);
        }

        // Play shooting sound
        float shootPitch = isSecondArrow ? 1.1f : 0.9f;
        getWorld().playSound(null, this.getBlockPos(),
                SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS,
                0.6f, shootPitch);
    }

    public LivingEntity getTarget() {
        return targetEntity;
    }

    public int getTicksAlive() {
        return ticksAlive;
    }

    public int getGroundTime() {
        return this.inGroundTime;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
    }
}