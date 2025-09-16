package com.sypztep.mamy.common.entity.skill;

import com.sypztep.mamy.client.particle.complex.SparkParticleEffect;
import com.sypztep.mamy.client.sound.ThunderSphereSoundInstance;
import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.init.ModSoundEvents;
import com.sypztep.mamy.common.network.client.CameraShakePayloadS2C;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class ThunderSphereEntity extends PersistentProjectileEntity {
    private static final int MAX_LIFETIME = 100; // 5 seconds
    private static final double AOE_RANGE = 5; // 10x10 area (5 radius)
    private static final double CAMERA_SHAKE_RANGE = 30.0;
    private static final int DAMAGE_INTERVAL = 4; // Every 10 ticks for AOE damage

    private final float baseDamage;

    private int ticksAlive = 0;
    private boolean hasExploded = false;
    private boolean soundStarted = false;

    public ThunderSphereEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
        this.baseDamage = 0f;
    }

    public ThunderSphereEntity(World world, LivingEntity owner, float baseDamage) {
        super(ModEntityTypes.THUNDER_SPHERE, owner, world, ItemStack.EMPTY, null);
        this.baseDamage = baseDamage;

        Vec3d direction = owner.getRotationVec(1.0f);
        this.setVelocity(direction.multiply(1.5)); // Faster speed
        this.velocityModified = true;
    }

    @Override
    public void tick() {
        super.tick();
        ticksAlive++;

        if (getWorld().isClient) {
            createElectricSphereEffects();

            if (!soundStarted && ticksAlive == 1) {
                soundStarted = true;
                MinecraftClient.getInstance().getSoundManager()
                        .play(new ThunderSphereSoundInstance(this));
            }
        } else {
            if (!hasExploded && ticksAlive % DAMAGE_INTERVAL == 0) {
                dealAOEDamage();
            }

            // Create spark effects
            if (ticksAlive % 3 == 0) {
                createSparkEffects();
            }
        }

        // Remove after lifetime
        if (ticksAlive >= MAX_LIFETIME) {
            if (!hasExploded) {
                explode();
            } else {
                discard();
            }
        }
    }

    private void createElectricSphereEffects() {
        // Electric sphere aura while traveling
        for (int i = 0; i < 6; i++) {
            double angle = (i / 6.0) * 2 * Math.PI + (ticksAlive * 0.15);
            double radius = 1.2 + Math.sin((ticksAlive + i * 10) * 0.12) * 0.3;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            double offsetY = Math.sin((ticksAlive + i * 15) * 0.18) * 0.4;

            getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    0.0, 0.0, 0.0);
        }

        // Core electric energy
        if (ticksAlive % 2 == 0) {
            for (int i = 0; i < 3; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.8;
                double offsetY = (random.nextDouble() - 0.5) * 0.8;
                double offsetZ = (random.nextDouble() - 0.5) * 0.8;

                getWorld().addParticle(ParticleTypes.END_ROD,
                        getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                        0.0, 0.0, 0.0);
            }
        }
    }

    private void createSparkEffects() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d center = getPos();

        // Create rotating spark ring around the sphere
        int sparkCount = 8;
        for (int i = 0; i < sparkCount; i++) {
            double angle = (i / (double) sparkCount) * 2 * Math.PI + (ticksAlive * 0.2);
            double radius = 1.8;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + Math.sin(angle * 2 + ticksAlive * 0.15) * 0.5;

            Vec3d sparkPos = new Vec3d(x, y, z);

            // Create spark effect towards center
            SparkParticleEffect sparkEffect = new SparkParticleEffect(center);
            serverWorld.spawnParticles(sparkEffect, sparkPos.x, sparkPos.y, sparkPos.z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        // Vertical sparks
        if (ticksAlive % 5 == 0) {
            Vec3d topPoint = center.add(0, 2, 0);
            Vec3d bottomPoint = center.add(0, -1, 0);

            SparkParticleEffect upwardSpark = new SparkParticleEffect(topPoint);
            serverWorld.spawnParticles(upwardSpark, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);

            SparkParticleEffect downwardSpark = new SparkParticleEffect(bottomPoint);
            serverWorld.spawnParticles(downwardSpark, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void dealAOEDamage() {
        Box damageBox = Box.of(getPos(), AOE_RANGE * 2, AOE_RANGE * 2, AOE_RANGE * 2);

        List<LivingEntity> targets = getWorld().getEntitiesByClass(
                LivingEntity.class,
                damageBox,
                entity -> entity != getOwner() && entity.isAlive() &&
                        (getOwner() == null || !entity.isTeammate(getOwner()))
        );

        if (targets.isEmpty()) return;

        // Calculate damage per entity (damage split among all targets)
        float damagePerEntity = baseDamage / targets.size();

        for (LivingEntity target : targets) {
            double distance = target.getPos().distanceTo(getPos());

            if (distance <= AOE_RANGE) {
                boolean damageDealt = target.damage(
                        ModDamageTypes.create(getWorld(), ModDamageTypes.LIGHTING, this, getOwner()),
                        damagePerEntity
                );

                if (damageDealt) {
                    // Apply brief slowness effect
                    target.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.SLOWNESS, 20, 0, // 1 second, level 1
                            false, true, true
                    ));

                    // Hit sound
                    getWorld().playSound(null, target.getBlockPos(),
                            ModSoundEvents.ENTITY_ELECTRIC_BLAST, SoundCategory.PLAYERS,
                            0.5f, 1.5f);

                }
            }
        }
    }


    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!getWorld().isClient) {
            entityHitResult.getEntity().damage(
                    ModDamageTypes.create(getWorld(), ModDamageTypes.LIGHTING, this, getOwner()),
                    baseDamage * 2 // Double damage for direct hit
            );
            explode();
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (!getWorld().isClient) {
            explode();
        }
    }

    private void explode() {
        if (hasExploded) return;

        hasExploded = true;
        setVelocity(Vec3d.ZERO);

        getWorld().playSound(null, getBlockPos(),
                ModSoundEvents.ENTITY_ELECTRIC_BIG_EXPLODE, SoundCategory.PLAYERS,
                1.5f, 1f);

        createExplosionEffects();

        if (getWorld() instanceof ServerWorld serverWorld) {
            PlayerLookup.around(serverWorld, getPos(), CAMERA_SHAKE_RANGE)
                    .forEach(player -> CameraShakePayloadS2C.send(player,
                            getX(), getY(), getZ(), 18, CAMERA_SHAKE_RANGE, 20));
        }

        dealFinalExplosion();

        discard();
    }

    private void dealFinalExplosion() {
        Box damageBox = Box.of(getPos(), AOE_RANGE * 3, AOE_RANGE * 3, AOE_RANGE * 3);

        List<LivingEntity> targets = getWorld().getEntitiesByClass(
                LivingEntity.class,
                damageBox,
                entity -> entity != getOwner() && entity.isAlive() &&
                        (getOwner() == null || !entity.isTeammate(getOwner()))
        );

        if (targets.isEmpty()) return;

        float explosionDamage = baseDamage * 1.5f / targets.size(); // falloff base on target hit

        for (LivingEntity target : targets) {
            double distance = target.getPos().distanceTo(getPos());

            if (distance <= AOE_RANGE * 1.5) {
                boolean damageDealt = target.damage(
                        ModDamageTypes.create(getWorld(), ModDamageTypes.LIGHTING, this, getOwner()),
                        explosionDamage
                );

                if (damageDealt) {
                    target.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.SLOWNESS, 100, 1, // 5 seconds, level 2
                            false, true, true
                    ));

                    createExplosionHitEffect(target.getPos());
                }
            }
        }
    }

    private void createExplosionEffects() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d center = getPos();

        // Multiple expanding rings of sparks
        for (int ring = 1; ring <= 5; ring++) {
            int pointsInRing = ring * 8;
            double ringRadius = ring * 1.0;

            for (int i = 0; i < pointsInRing; i++) {
                double angle = (i / (double) pointsInRing) * 2 * Math.PI;
                double x = center.x + Math.cos(angle) * ringRadius;
                double z = center.z + Math.sin(angle) * ringRadius;
                double y = center.y + (random.nextDouble() - 0.5) * 2.0;

                Vec3d sparkEnd = new Vec3d(x, y, z);
                SparkParticleEffect explosionSpark = new SparkParticleEffect(sparkEnd);
                serverWorld.spawnParticles(explosionSpark, center.x, center.y, center.z, 1, 1.0, 0.0, 0.0, 0.0);
            }
        }

        // Central pillar of sparks
        for (int y = -2; y <= 4; y++) {
            Vec3d pillarPos = center.add(0, y, 0);
            serverWorld.spawnParticles(ParticleTypes.FLASH,
                    pillarPos.x, pillarPos.y, pillarPos.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        // Random chaotic sparks
        for (int i = 0; i < 20; i++) {
            Vec3d randomStart = center.add(
                    (random.nextDouble() - 0.5) * 3.0,
                    (random.nextDouble() - 0.5) * 2.0,
                    (random.nextDouble() - 0.5) * 3.0
            );

            Vec3d randomEnd = center.add(
                    (random.nextDouble() - 0.5) * 6.0,
                    (random.nextDouble() - 0.5) * 3.0,
                    (random.nextDouble() - 0.5) * 6.0
            );

            SparkParticleEffect chaoticSpark = new SparkParticleEffect(randomEnd);
            serverWorld.spawnParticles(chaoticSpark, randomStart.x, randomStart.y, randomStart.z, 1, 1.0, 0.0, 0.0, 0.0);
        }
    }

    private void createExplosionHitEffect(Vec3d pos) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        // Lightning pillar at hit location
        for (int y = 0; y <= 2; y++) {
            serverWorld.spawnParticles(ParticleTypes.END_ROD,
                    pos.x, pos.y + y, pos.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        for (int i = 0; i < 5; i++) {
            double offsetX = (random.nextDouble() - 0.5);
            double offsetY = (random.nextDouble() - 0.5);
            double offsetZ = (random.nextDouble() - 0.5);

            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected float getDragInWater() {
        return 0.95f;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }
}