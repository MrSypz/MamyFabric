package com.sypztep.mamy.common.entity.skill;

import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.network.client.CameraShakePayloadS2C;
import com.sypztep.mamy.common.util.SkillUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FireballEntity extends PersistentProjectileEntity {
    private float damage = 0;
    private int ticksAlive = 0;
    private static final int MAX_LIFETIME = 60; // 3 seconds
    private final float range = 25;

    public FireballEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public FireballEntity(World world, LivingEntity owner, float damage, int maxTargets) {
        super(ModEntityTypes.FIREBALL, owner, world, ItemStack.EMPTY, null);
        this.damage = damage;

        Vec3d direction = owner.getRotationVec(1.0f);
        this.setVelocity(direction.multiply(1.2));
        this.velocityModified = true;
    }

    @Override
    public void tick() {
        super.tick();
        ticksAlive++;

        if (getWorld().isClient) {
            createTrailParticles();
        }

        if (ticksAlive >= MAX_LIFETIME) {
            discard();
        }

        if (!getWorld().isClient && age > 100) {
            discard();
        }
    }

    private void createTrailParticles() {
        for (int i = 0; i < 4; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.3;
            double offsetY = (random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (random.nextDouble() - 0.5) * 0.3;

            getWorld().addParticle(ParticleTypes.FLAME,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    -getVelocity().x * 0.1, -getVelocity().y * 0.1, -getVelocity().z * 0.1);
        }

        if (random.nextFloat() < 0.3f) {
            getWorld().addParticle(ParticleTypes.LARGE_SMOKE,
                    getX(), getY(), getZ(),
                    (random.nextDouble() - 0.5) * 0.1,
                    (random.nextDouble() - 0.5) * 0.1,
                    (random.nextDouble() - 0.5) * 0.1);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!getWorld().isClient) {
            playSound(SoundEvents.ITEM_FIRECHARGE_USE, 2f, 1.0f);
            explodeArea(getPos());

            Vec3d blockPos = entityHitResult.getPos();
            PlayerLookup.around((ServerWorld) getWorld(), blockPos, range)
                    .forEach(player -> CameraShakePayloadS2C.send(player,
                            blockPos.x, blockPos.y, blockPos.z, 15, range, 30));
        }
        discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (!getWorld().isClient) {
            playSound(SoundEvents.ITEM_FIRECHARGE_USE, 2f, 1.0f);
            explodeArea(blockHitResult.getPos());

            // Camera shake for explosion
            Vec3d blockPos = blockHitResult.getPos();
            PlayerLookup.around((ServerWorld) getWorld(), blockPos, range)
                    .forEach(player -> CameraShakePayloadS2C.send(player,
                            blockPos.x, blockPos.y, blockPos.z, 15, range, 30));
        }
        discard();
    }

    private void explodeArea(Vec3d centerPos) {
        if (getWorld().isClient) return;

        double aoeRadius = 3.0; // 6x6 area
        Box damageArea = SkillUtil.makeBox(centerPos,aoeRadius * 2, aoeRadius * 2, aoeRadius * 2,true);

        getWorld().getEntitiesByClass(LivingEntity.class,
                damageArea,
                entity -> entity != getOwner() && entity.isAlive() &&
                        (getOwner() == null || !entity.isTeammate(getOwner()))
        ).forEach(entity -> {
            double distance = entity.getPos().distanceTo(centerPos);
            if (distance <= aoeRadius) {
                float damageMultiplier = (float) Math.max(0.5, 1.0 - (distance / aoeRadius) * 0.5);
                float aoeDamage = damage * damageMultiplier;

                boolean damageDealt = entity.damage(
                        ModDamageTypes.create(getWorld(), ModDamageTypes.FIREBALL, getOwner()),
                        aoeDamage
                );

                if (damageDealt) {
                    entity.setOnFireFor(2 + (int)(damageMultiplier * 3));

                    getWorld().playSound(null, entity.getBlockPos(),
                            SoundEvents.ENTITY_BLAZE_HURT, net.minecraft.sound.SoundCategory.PLAYERS,
                            0.6f, 1.0f + (float)(Math.random() * 0.4 - 0.2));

                    createHitEffect(entity.getPos());
                }
            }
        });

        createExplosionEffect(centerPos);
    }

    private void createHitEffect(Vec3d pos) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        for (int i = 0; i < 5; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.8;
            double offsetY = (random.nextDouble() - 0.5) * 0.8;
            double offsetZ = (random.nextDouble() - 0.5) * 0.8;

            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    1, 0.0, 0.1, 0.0, 0.1);
        }
    }

    private void createExplosionEffect(Vec3d centerPos) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        // Large fire explosion
        for (int i = 0; i < 40; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 4.0;
            double offsetY = (random.nextDouble() - 0.5) * 4.0;
            double offsetZ = (random.nextDouble() - 0.5) * 4.0;

            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    centerPos.x + offsetX, centerPos.y + offsetY, centerPos.z + offsetZ,
                    1, offsetX * 0.1, offsetY * 0.1, offsetZ * 0.1, 0.2);
        }

        // Smoke cloud
        for (int i = 0; i < 20; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 3.0;
            double offsetY = (random.nextDouble() - 0.5) * 3.0;
            double offsetZ = (random.nextDouble() - 0.5) * 3.0;

            serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE,
                    centerPos.x + offsetX, centerPos.y + offsetY, centerPos.z + offsetZ,
                    1, offsetX * 0.05, offsetY * 0.05, offsetZ * 0.05, 0.15);
        }

        // Flash
        for (int i = 0; i < 8; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2.0;
            double offsetY = (random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0;

            serverWorld.spawnParticles(ParticleTypes.FLASH,
                    centerPos.x + offsetX, centerPos.y + offsetY, centerPos.z + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        // Lava drops
        for (int i = 0; i < 15; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 3.5;
            double offsetY = (random.nextDouble() - 0.5) * 3.5;
            double offsetZ = (random.nextDouble() - 0.5) * 3.5;

            serverWorld.spawnParticles(ParticleTypes.FALLING_DRIPSTONE_LAVA,
                    centerPos.x + offsetX, centerPos.y + offsetY, centerPos.z + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.1);
        }
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