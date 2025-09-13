package com.sypztep.mamy.common.entity.skill;

import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.network.client.CameraShakePayloadS2C;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FireboltEntity extends PersistentProjectileEntity {
    private float damage = 0;
    private int ticksAlive = 0;
    private static final int MAX_LIFETIME = 45; // 2.25 seconds
    private final float range = 10;

    public FireboltEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public FireboltEntity(World world, LivingEntity owner, float damage, int maxTargets) {
        super(ModEntityTypes.FIREBOLT, owner, world, ItemStack.EMPTY, null);
        this.damage = damage;

        Vec3d direction = owner.getRotationVec(1.0f);
        this.setVelocity(direction.multiply(1.5)); // Fast projectile
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
        // Small flame trail
        for (int i = 0; i < 2; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.15;
            double offsetY = (random.nextDouble() - 0.5) * 0.15;
            double offsetZ = (random.nextDouble() - 0.5) * 0.15;

            getWorld().addParticle(ParticleTypes.FLAME,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    -getVelocity().x * 0.08, -getVelocity().y * 0.08, -getVelocity().z * 0.08);
        }

        // Occasional smoke
        if (random.nextFloat() < 0.4f) {
            double offsetX = (random.nextDouble() - 0.5) * 0.1;
            double offsetY = (random.nextDouble() - 0.5) * 0.1;
            double offsetZ = (random.nextDouble() - 0.5) * 0.1;

            getWorld().addParticle(ParticleTypes.SMOKE,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    -getVelocity().x * 0.03, -getVelocity().y * 0.03, -getVelocity().z * 0.03);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!getWorld().isClient && entityHitResult.getEntity() instanceof LivingEntity target) {
            // Deal single damage
            boolean damageDealt = target.damage(
                    getWorld().getDamageSources().inFire(),
                    damage
            );

            if (damageDealt) {
                target.setOnFireFor(2); // 2 seconds fire
                playSound(SoundEvents.ITEM_FIRECHARGE_USE, 1.5f, 1.4f);
                createHitEffect();
            }

            Vec3d blockPos = entityHitResult.getPos();
            PlayerLookup.around((ServerWorld) getWorld(), blockPos, range)
                    .forEach(player -> CameraShakePayloadS2C.send(player,
                            blockPos.x, blockPos.y, blockPos.z, 8, range, 9));
        }
        discard(); // Disappear immediately after hit
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (!getWorld().isClient) {
            playSound(SoundEvents.ITEM_FIRECHARGE_USE, 1.5f, 1.4f);
            createBlockHitEffect();

            Vec3d blockPos = blockHitResult.getPos();
            PlayerLookup.around((ServerWorld) getWorld(), blockPos, range)
                    .forEach(player -> CameraShakePayloadS2C.send(player,
                            blockPos.x, blockPos.y, blockPos.z, 8, range, 9));
        }

        discard();
    }

    private void createHitEffect() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        // Small flame burst
        for (int i = 0; i < 8; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.8;
            double offsetY = (random.nextDouble() - 0.5) * 0.8;
            double offsetZ = (random.nextDouble() - 0.5) * 0.8;

            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.1);
        }

        // Small flash
        serverWorld.spawnParticles(ParticleTypes.FLASH,
                getX(), getY(), getZ(),
                1, 0.0, 0.05, 0.0, 0.1);

        // Bit of smoke
        for (int i = 0; i < 2; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.4;
            double offsetY = (random.nextDouble() - 0.5) * 0.4;
            double offsetZ = (random.nextDouble() - 0.5) * 0.4;

            serverWorld.spawnParticles(ParticleTypes.SMOKE,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.03);
        }
    }

    private void createBlockHitEffect() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        // Block hit flames
        for (int i = 0; i < 10; i++) {
            double offsetX = (random.nextDouble() - 0.5);
            double offsetY = (random.nextDouble() - 0.5);
            double offsetZ = (random.nextDouble() - 0.5);

            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.15);
        }

        // Small flash
        serverWorld.spawnParticles(ParticleTypes.FLASH,
                getX(), getY(), getZ(),
                1, 0.0, 0.0, 0.0, 0.08);

        // Smoke
        for (int i = 0; i < 3; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.5;
            double offsetY = (random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (random.nextDouble() - 0.5) * 0.5;

            serverWorld.spawnParticles(ParticleTypes.SMOKE,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    1, 0.0, 0.08, 0.0, 0.08);
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