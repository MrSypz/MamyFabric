package com.sypztep.mamy.common.entity.entity.skill;

import com.google.common.collect.Maps;
import com.sypztep.mamy.common.init.ModParticles;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.init.ModDamageTypes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArrowRainEntity extends PersistentProjectileEntity {
    private static final int MAX_LIFETIME = 20; // 1 second
    private static final int MAX_HITS_PER_TARGET = 4;

    private final Map<UUID, Integer> hitCounts = Maps.newHashMap();
    private final float baseDamage;
    private final int skillLevel;
    private final List<StatusEffectInstance> arrowEffects;

    private int damage_interval; // Every 4 ticks
    private int ticksAlive = 0;
    private int damageTimer = 0;

    public ArrowRainEntity(World world, float baseDamage, int skillLevel,int damage_interval, List<StatusEffectInstance> arrowEffects) {
        super(ModEntityTypes.ARROW_RAIN, world);
        this.baseDamage = baseDamage;
        this.skillLevel = skillLevel;
        this.arrowEffects = arrowEffects != null ? arrowEffects : List.of();
        this.damage_interval = damage_interval;
        this.setNoGravity(true);
        this.setVelocity(Vec3d.ZERO);
    }

    public ArrowRainEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
        this.baseDamage = 0f;
        this.skillLevel = 1;
        this.arrowEffects = List.of();
    }

    public int getSkillLevel() {
        return skillLevel;
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
        damageTimer++;
        getWorld().playSound(null, this.getBlockPos(),
                SoundEvents.WEATHER_RAIN, SoundCategory.PLAYERS,
                0.5f, 1.25f);

        if (!this.getWorld().isClient) createFallingArrowEffects();

        // Deal damage every 4 ticks
        if (damageTimer >= damage_interval && !this.getWorld().isClient) {
            dealAreaDamage(false); // Normal damage
            damageTimer = 0;
        }

        // Remove after 1 second with final burst
        if (ticksAlive >= MAX_LIFETIME) {
            if (!this.getWorld().isClient) {
                dealAreaDamage(true); // Final burst with 2x damage
                createFinalBurst();
            }
            this.discard();
        }
    }

    private void createFallingArrowEffects() {
        if (this.getWorld().isClient || !(this.getWorld() instanceof ServerWorld serverWorld)) return;

        float areaSize = getAreaSize();

        if (this.random.nextFloat() < 0.4f) {
            double offsetX = (this.random.nextDouble() - 0.5) * areaSize;
            double offsetZ = (this.random.nextDouble() - 0.5) * areaSize;

            serverWorld.spawnParticles(ModParticles.ARROW_IMPACT,
                    this.getX() + offsetX, this.getY() + 0.1, this.getZ() + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.0); // Static, no velocity
        }

        float pitch = 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f;
        getWorld().playSound(null, this.getBlockPos(),
                SoundEvents.ENTITY_ARROW_HIT, SoundCategory.PLAYERS,
                0.5f, pitch);
    }

    private void createFinalBurst() {
        if (this.getWorld().isClient || !(this.getWorld() instanceof ServerWorld serverWorld)) return;

        float areaSize = getAreaSize();
        int burstParticles = 10 + (skillLevel * 2); // More particles at higher levels

        for (int i = 0; i < burstParticles; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * areaSize;
            double offsetZ = (this.random.nextDouble() - 0.5) * areaSize;

            serverWorld.spawnParticles(ModParticles.ARROW_IMPACT,
                    this.getX() + offsetX, this.getY() + 0.2, this.getZ() + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.0); // Static, no velocity
        }

        // Play final impact sound
        serverWorld.playSound(null, this.getBlockPos(),
                SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS,
                1.0f, 1.2f);
    }

    private float getAreaSize() {
        return skillLevel >= 6 ? 5.0f : 3.0f;
    }

    private float calculateFinalDamage(boolean isFinalBurst) {
        return isFinalBurst ? baseDamage * 2.0f : baseDamage;
    }

    private void dealAreaDamage(boolean isFinalBurst) {
        float areaSize = getAreaSize();
        float halfSize = areaSize / 2.0f;

        Box damageBox = new Box(
                this.getX() - halfSize, this.getY() - 0.5, this.getZ() - halfSize,
                this.getX() + halfSize, this.getY() + 15, this.getZ() + halfSize
        );

        float finalDamage = calculateFinalDamage(isFinalBurst);

        for (LivingEntity target : this.getWorld().getEntitiesByClass(
                LivingEntity.class,
                damageBox,
                (entity) -> {
                    return this.getOwner() == null || !entity.getUuid().equals(this.getOwner().getUuid());// Include all other entities for hit checking
                }
        )) {
            UUID targetId = target.getUuid();
            int currentHits = hitCounts.getOrDefault(targetId, 0);

            // Determine max hits for this attack
            int maxHitsForThisAttack = isFinalBurst ? MAX_HITS_PER_TARGET + 1 : MAX_HITS_PER_TARGET;

            // Check if entity has reached hit limit
            if (currentHits >= maxHitsForThisAttack) {
                continue; // Skip this entity - hit limit reached
            }

            // Deal damage to entity
            boolean damageDealt = target.damage(
                    getWorld().getDamageSources().create(ModDamageTypes.ARROW_RAIN, this, getOwner()),
                    finalDamage
            );

            if (damageDealt) {
                // Apply arrow effects if any
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
                        target.addStatusEffect(newEffect);
                    }
                }

                // Increment hit count only after successful damage
                int newHitCount = currentHits + 1;
                hitCounts.put(targetId, newHitCount);

                // Play hit sound with different pitch based on hit count
                float pitch = 1.0f + (newHitCount * 0.1f) + (this.random.nextFloat() - this.random.nextFloat()) * 0.1f;
                if (isFinalBurst) {
                    pitch += 0.3f; // Higher pitch for final burst
                }

                getWorld().playSound(null, target.getBlockPos(),
                        SoundEvents.ENTITY_ARROW_HIT, SoundCategory.PLAYERS,
                        isFinalBurst ? 0.8f : 0.5f, pitch);
            }
        }
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        this.setVelocity(Vec3d.ZERO);
        this.inGround = true;
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < 64.0 * 64.0;
    }
}