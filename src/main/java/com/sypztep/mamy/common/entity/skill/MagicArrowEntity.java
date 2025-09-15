package com.sypztep.mamy.common.entity.skill;

import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.init.ModSoundEvents;
import com.sypztep.mamy.common.network.client.CameraShakePayloadS2C;
import com.sypztep.mamy.common.util.MultiHitRecord;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
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


public class MagicArrowEntity extends PersistentProjectileEntity {
    private float damage = 0;
    private int maxTargets = 1;
    private int arrowIndex = 0;
    private boolean hasSpawnedSiblings = false;
    private static final int MAX_HIT = 2;

    private final MultiHitRecord hitRecord;
    private int ticksAlive = 0;
    private int damageTimer = 0;
    private static final int DAMAGE_INTERVAL = 4;
    private static final int MAX_LIFETIME = 40;
    private boolean hasHitTarget = false;
    private LivingEntity hitTarget = null;

    public MagicArrowEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
        hitRecord = new MultiHitRecord(MAX_HIT);
    }

    public MagicArrowEntity(World world, LivingEntity owner, float damage, int maxTargets, int arrowIndex) {
        super(ModEntityTypes.MAGIC_ARROW, owner, world, ItemStack.EMPTY, null);
        this.damage = damage;
        this.maxTargets = maxTargets;
        this.arrowIndex = arrowIndex;

        Vec3d baseDirection = owner.getRotationVec(1.0f);
        float spreadAngle = (arrowIndex - 1) * 5.0f;
        double radians = Math.toRadians(spreadAngle);
        Vec3d spread = new Vec3d(
                baseDirection.x * Math.cos(radians) - baseDirection.z * Math.sin(radians),
                baseDirection.y,
                baseDirection.x * Math.sin(radians) + baseDirection.z * Math.cos(radians)
        );

        hitRecord = new MultiHitRecord(MAX_HIT);
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

        if (getWorld().isClient) {
            createTrailParticles();
        }

        if (hasHitTarget && hitTarget != null && hitTarget.isAlive() && !getWorld().isClient) {
            if (damageTimer >= DAMAGE_INTERVAL && hitRecord.getHitCount(hitTarget) <= MAX_HIT) {
                dealDamageToTarget();
                damageTimer = 0;
            }
        }

        if (ticksAlive >= MAX_LIFETIME || (hasHitTarget && (hitTarget == null || !hitTarget.isAlive()))) {
            discard();
        }

        if (!getWorld().isClient && age > 100 && !hasHitTarget) {
            discard();
        }
    }

    private void createTrailParticles() {
        for (int i = 0; i < 3; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.2;
            double offsetY = (random.nextDouble() - 0.5) * 0.2;
            double offsetZ = (random.nextDouble() - 0.5) * 0.2;

            getWorld().addParticle(ParticleTypes.ENCHANT,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    0.0, 0.0, 0.0);
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
        if (hasHitTarget) return;

        LivingEntity target = findNearestTarget();
        if (target != null) {
            Vec3d currentVel = getVelocity();
            Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.5, 0);
            Vec3d toTarget = targetPos.subtract(getPos()).normalize();
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

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!getWorld().isClient && entityHitResult.getEntity() instanceof LivingEntity target) {
            this.hasHitTarget = true;
            this.hitTarget = target;
            this.damageTimer = 0;
            playSound(ModSoundEvents.ENTITY_GENERIC_MAGIC_ARROW_EXPLODE, 2f, 1.2f);
            dealDamageToTarget();
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (!getWorld().isClient) {
            playSound(ModSoundEvents.ENTITY_GENERIC_MAGIC_ARROW_EXPLODE, 2f, 1.2f);
            Vec3d blockPos = blockHitResult.getPos();
            double range = 15;
            PlayerLookup.around((ServerWorld) getWorld(), blockPos, range)
                    .forEach(player -> CameraShakePayloadS2C.send(player,
                            blockPos.x, blockPos.y, blockPos.z, 10, range, 10));
            createBlockHitEffect();
        }
        discard();
    }

    private void dealDamageToTarget() {
        if (hitTarget == null || !hitTarget.isAlive()) return;

        int currentHits = hitRecord.getHitCount(hitTarget);
        int maxHitsForThisTarget = getMaxHitsForSkillLevel();

        if (currentHits >= maxHitsForThisTarget) {
            return;
        }

        boolean damageDealt = hitTarget.damage(
                getWorld().getDamageSources().create(ModDamageTypes.MAGIC_ARROW, this, getOwner()),
                damage
        );

        if (damageDealt) {
            int newHitCount = hitRecord.recordHitAndGet(hitTarget);

            float pitch = 1.0f + (newHitCount * 0.15f) + (random.nextFloat() - random.nextFloat()) * 0.1f;
            getWorld().playSound(null, hitTarget.getBlockPos(),
                    SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, net.minecraft.sound.SoundCategory.PLAYERS,
                    0.7f, pitch);

            getWorld().playSound(null, hitTarget.getBlockPos(),
                    SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, net.minecraft.sound.SoundCategory.PLAYERS,
                    0.4f, 1.5f + (newHitCount * 0.1f));

            createHitEffect();
        }
    }

    private int getMaxHitsForSkillLevel() {
        if (maxTargets >= 10) {
            return 2;
        } else if (maxTargets >= 5) {
            return 1;
        } else {
            return 1;
        }
    }

    private void createHitEffect() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        for (int i = 0; i < 15; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 1.2;
            double offsetY = (random.nextDouble() - 0.5) * 1.2;
            double offsetZ = (random.nextDouble() - 0.5) * 1.2;

            serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.15);
        }

        for (int i = 0; i < 3; i++) {
            double offsetX = (random.nextDouble() - 0.5);
            double offsetY = (random.nextDouble() - 0.5);
            double offsetZ = (random.nextDouble() - 0.5);

            serverWorld.spawnParticles(ParticleTypes.FLASH,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    1, 0.0, 0.1, 0.0, 0.2);
        }

        for (int i = 0; i < 5; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.6;
            double offsetY = (random.nextDouble() - 0.5) * 0.6;
            double offsetZ = (random.nextDouble() - 0.5) * 0.6;

            serverWorld.spawnParticles(ParticleTypes.DRAGON_BREATH,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.05);
        }
    }

    private void createBlockHitEffect() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        for (int i = 0; i < 20; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 1.5;
            double offsetY = (random.nextDouble() - 0.5) * 1.5;
            double offsetZ = (random.nextDouble() - 0.5) * 1.5;

            serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.2);
        }

        for (int i = 0; i < 3; i++) {
            double offsetX = (random.nextDouble() - 0.5);
            double offsetY = (random.nextDouble() - 0.5);
            double offsetZ = (random.nextDouble() - 0.5);

            serverWorld.spawnParticles(ParticleTypes.FLASH,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    1, offsetX * 0.3, offsetY * 0.3, offsetZ * 0.3, 0.1);
        }

        for (int i = 0; i < 8; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.8;
            double offsetY = (random.nextDouble() - 0.5) * 0.8;
            double offsetZ = (random.nextDouble() - 0.5) * 0.8;

            serverWorld.spawnParticles(ParticleTypes.WITCH,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    1, 0.0, 0.1, 0.0, 0.1);
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
    protected float getDragInWater() {
        return 0.9f;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }
}