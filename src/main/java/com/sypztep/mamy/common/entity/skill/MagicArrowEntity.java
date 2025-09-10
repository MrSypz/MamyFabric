package com.sypztep.mamy.common.entity.skill;

import com.google.common.collect.Maps;
import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;

public class MagicArrowEntity extends PersistentProjectileEntity {
    private float damage = 0;
    private int maxTargets = 1;
    private int arrowIndex = 0; // Which arrow in the sequence (0, 1, 2)
    private boolean hasSpawnedSiblings = false;

    // Multiple hit system similar to ArrowRainEntity
    private final Map<UUID, Integer> hitCounts = Maps.newHashMap();
    private int ticksAlive = 0;
    private int damageTimer = 0;
    private static final int DAMAGE_INTERVAL = 4; // Every 4 ticks like ArrowRain
    private static final int MAX_LIFETIME = 40; // 2 seconds
    private boolean hasHitTarget = false;
    private LivingEntity hitTarget = null;

    public MagicArrowEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public MagicArrowEntity(World world, LivingEntity owner, float damage, int maxTargets, int arrowIndex) {
        super(ModEntityTypes.MAGIC_ARROW, owner, world, ItemStack.EMPTY, null);
        this.damage = damage;
        this.maxTargets = maxTargets;
        this.arrowIndex = arrowIndex;

        // Set initial velocity with slight spread for multiple arrows
        Vec3d baseDirection = owner.getRotationVec(1.0f);
        float spreadAngle = (arrowIndex - 1) * 5.0f; // -5°, 0°, +5° spread

        // Apply horizontal spread
        double radians = Math.toRadians(spreadAngle);
        Vec3d spread = new Vec3d(
                baseDirection.x * Math.cos(radians) - baseDirection.z * Math.sin(radians),
                baseDirection.y,
                baseDirection.x * Math.sin(radians) + baseDirection.z * Math.cos(radians)
        );

        this.setVelocity(spread.multiply(1));
        this.velocityModified = true;
    }

    @Override
    public void tick() {
        if (!hasSpawnedSiblings && !getWorld().isClient && arrowIndex == 0) {
            spawnSiblingArrows();
            hasSpawnedSiblings = true;
        }

        if (!getWorld().isClient && age > 2) {
            adjustAimTowardsTarget();
        }

        super.tick();
        ticksAlive++;
        damageTimer++;

        // Particle effects
        if (this.getWorld().isClient && this.age % 2 == 0) {
            this.getWorld().addParticle(ParticleTypes.ENCHANT,
                    this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }

        // Multiple hit system when arrow has hit a target
        if (hasHitTarget && hitTarget != null && hitTarget.isAlive() && !getWorld().isClient) {
            if (damageTimer >= DAMAGE_INTERVAL) {
                dealDamageToTarget();
                damageTimer = 0;
            }
        }

        // Remove after lifetime or if target is dead
        if (ticksAlive >= MAX_LIFETIME || (hasHitTarget && (hitTarget == null || !hitTarget.isAlive()))) {
            discard();
        }

        if (!getWorld().isClient && age > 100 && !hasHitTarget) {
            discard();
        }
    }

    private void spawnSiblingArrows() {
        if (getOwner() instanceof LivingEntity owner) {
            for (int i = 1; i <= 2; i++) {
                MagicArrowEntity sibling = new MagicArrowEntity(getWorld(), owner, damage, maxTargets, i);
                sibling.setPosition(getX(), getY(), getZ());
                getWorld().spawnEntity(sibling);
            }
        }
    }

    private void adjustAimTowardsTarget() {
        if (hasHitTarget) return; // Don't adjust if already hit something

        LivingEntity target = findNearestTarget();
        if (target != null) {
            Vec3d currentVel = getVelocity();

            // Target center of entity (body/chest area) instead of feet
            Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.5, 0);
            Vec3d toTarget = targetPos.subtract(getPos()).normalize();

            // Reduced adjustment for smoother homing - less aggressive
            Vec3d adjustedVel = currentVel.multiply(0.85).add(toTarget.multiply(0.15));
            setVelocity(adjustedVel.normalize().multiply(currentVel.length()));
        }
    }

    private LivingEntity findNearestTarget() {
        return getWorld().getEntitiesByClass(LivingEntity.class,
                        getBoundingBox().expand(8.0),
                        e -> e != getOwner() && e.isAlive() &&
                                (getOwner() == null || !e.isTeammate(getOwner())))
                .stream()
                .min((a, b) -> Double.compare(squaredDistanceTo(a), squaredDistanceTo(b)))
                .orElse(null);
    }

    private void dealDamageToTarget() {
        if (hitTarget == null || !hitTarget.isAlive()) return;

        UUID targetId = hitTarget.getUuid();
        int currentHits = hitCounts.getOrDefault(targetId, 0);

        // Calculate max hits based on skill level
        int maxHitsForThisTarget = getMaxHitsForSkillLevel();

        // Check if entity has reached hit limit
        if (currentHits >= maxHitsForThisTarget) {
            return; // Skip - hit limit reached
        }

        // Deal normal damage (no final burst multiplier)
        boolean damageDealt = hitTarget.damage(
                getWorld().getDamageSources().create(ModDamageTypes.MAGIC_ARROW, this, getOwner()),
                damage
        );

        if (damageDealt) {
            // Increment hit count
            int newHitCount = currentHits + 1;
            hitCounts.put(targetId, newHitCount);

            // Play hit sound with different pitch based on hit count
            float pitch = 1.0f + (newHitCount * 0.1f) + (this.random.nextFloat() - this.random.nextFloat()) * 0.1f;

            getWorld().playSound(null, hitTarget.getBlockPos(),
                    SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, net.minecraft.sound.SoundCategory.PLAYERS,
                    0.5f, pitch);
        }
    }

    private int getMaxHitsForSkillLevel() {
        // Based on your requirement: level 10 should do damage twice
        if (maxTargets >= 10) {
            return 2; // Level 10+ gets 2 hits total
        } else if (maxTargets >= 5) {
            return 1; // Level 5-9 gets 1 hit total
        } else {
            return 1; // Level 1-4 gets 1 hit total
        }
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE;
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (!getWorld().isClient) {
            playSound(getHitSound(), 1.0f, 1.2f / (random.nextFloat() * 0.2f + 0.9f));
            discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!getWorld().isClient && entityHitResult.getEntity() instanceof LivingEntity target) {
            this.hasHitTarget = true;
            this.hitTarget = target;
            this.damageTimer = 0;

            dealDamageToTarget();
            createHitEffect();
        }
    }

    private void createHitEffect() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        // Create magical hit particles
        for (int i = 0; i < 8; i++) {
            double offsetX = (random.nextDouble() - 0.5);
            double offsetY = (random.nextDouble() - 0.5);
            double offsetZ = (random.nextDouble() - 0.5);

            serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.1);
        }
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }
}