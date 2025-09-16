package com.sypztep.mamy.common.entity.skill;

import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.init.ModParticles;
import com.sypztep.mamy.common.init.ModSoundEvents;
import com.sypztep.mamy.common.network.client.CameraShakePayloadS2C;
import com.sypztep.mamy.common.util.MultiHitRecord;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MeteorEntity extends PersistentProjectileEntity {
    private static final int MAX_LIFETIME = 90; // 4.5 seconds
    private static final int DAMAGE_INTERVAL = 6; // Every 6 ticks for 2 hits
    private static final double EXPLOSION_RADIUS = 40; // 30x30 area (15 radius)
    private static final double CAMERA_SHAKE_RANGE = 50.0;
    private static final int MAX_HITS_PER_TARGET = 3;

    private final MultiHitRecord hitRecord;
    private final float baseDamage;

    private int ticksAlive = 0;
    private int damageTimer = 0;
    private int explosionHits = 0; // Track explosion damage hits (max 3)
    private boolean hasExploded = false;

    public MeteorEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
        this.baseDamage = 0f;
        this.hitRecord = new MultiHitRecord(MAX_HITS_PER_TARGET);
    }

    public MeteorEntity(World world, LivingEntity owner, float baseDamage) {
        super(ModEntityTypes.METEOR, owner, world, ItemStack.EMPTY, null);
        this.baseDamage = baseDamage;
        this.hitRecord = new MultiHitRecord(MAX_HITS_PER_TARGET);
    }

    @Override
    public void tick() {
        super.tick();
        ticksAlive++;

        if (getWorld().isClient) createFallingEffects();

        // If exploded, handle explosion damage cycles
        if (hasExploded && explosionHits < MAX_HITS_PER_TARGET && !getWorld().isClient) {
            damageTimer++;
            if (damageTimer >= DAMAGE_INTERVAL) {
                dealExplosionDamage();
                damageTimer = 0;
                explosionHits++;
            }
        }

        // Remove after lifetime or after 2 explosion hits
        if (ticksAlive >= MAX_LIFETIME || explosionHits >= MAX_HITS_PER_TARGET) {
            discard();
        }
    }

    private void createFallingEffects() {
        if (ticksAlive % 4 == 0)
            getWorld().addParticle(ModParticles.METEOR_IMPACT, getX(), getY() - 3, getZ(),
                    0, 0, 0);
        // Large smoke trail
        for (int i = 0; i < 3; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 3.0;
            double offsetY = (random.nextDouble() - 0.5) * 3.0;
            double offsetZ = (random.nextDouble() - 0.5) * 3.0;

            getWorld().addParticle(ParticleTypes.LARGE_SMOKE,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    (random.nextDouble() - 0.5) * 0.2,
                    -getVelocity().y * 0.3,
                    (random.nextDouble() - 0.5) * 0.2);
        }

        // Lava particles for magma effect
        if (random.nextFloat() < 0.6f) {
            double offsetX = (random.nextDouble() - 0.5) * 2.0;
            double offsetY = (random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0;

            getWorld().addParticle(ParticleTypes.LAVA,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    0, 0, 0);
        }

        // Falling sound effect
        if (ticksAlive % 4 == 0) {
            getWorld().playSound(getX(), getY(), getZ(),
                    SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS,
                    1.0f, 0.5f, false);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!getWorld().isClient) {
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
        if (hasExploded) return; // Prevent multiple explosions

        hasExploded = true;
        setVelocity(Vec3d.ZERO); // Stop moving

        // Play massive explosion sound
        getWorld().playSound(null, getBlockPos(),
                SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS,
                3.0f, 0.6f);

        // Additional impact sounds
        getWorld().playSound(null, getBlockPos(),
                ModSoundEvents.ENTITY_GENERIC_SHOCKWAVE, SoundCategory.PLAYERS,
                15.0f, 0.8f + (float) (this.getRandom().nextGaussian() * 0.3f));

        if (getWorld() instanceof ServerWorld serverWorld) {
            PlayerLookup.around(serverWorld, getPos(), CAMERA_SHAKE_RANGE)
                    .forEach(player -> CameraShakePayloadS2C.send(player,
                            getX(), getY(), getZ(), 40, CAMERA_SHAKE_RANGE, 50));
        }
        createExplosionEffects();
        damageTimer = 0;
    }

    private void dealExplosionDamage() {
        Box damageBox = Box.of(getPos(), EXPLOSION_RADIUS * 2, EXPLOSION_RADIUS * 2, EXPLOSION_RADIUS * 2);

        for (LivingEntity target : getWorld().getEntitiesByClass(
                LivingEntity.class,
                damageBox,
                entity -> entity != getOwner() && entity.isAlive() &&
                        (getOwner() == null || !entity.isTeammate(getOwner()))
        )) {
            double distance = target.getPos().distanceTo(getPos());

            if (distance <= EXPLOSION_RADIUS) {
                double falloffMultiplier = Math.max(0.3, 1.0 - (distance / EXPLOSION_RADIUS) * 0.7);
                float finalDamage = (float) (baseDamage * falloffMultiplier);

                boolean damageDealt = target.damage(
                        ModDamageTypes.create(getWorld(), ModDamageTypes.FIREBALL, this, getOwner()),
                        finalDamage
                );

                if (damageDealt) {
                    // Set on fire based on distance (closer = longer fire)
                    int fireDuration = (int) (5 + (1.0 - (distance / EXPLOSION_RADIUS)) * 5);
                    target.setOnFireFor(fireDuration);

                    hitRecord.recordHit(target);

                    // Hit sound with pitch based on explosion number
                    float pitch = explosionHits == 0 ? 0.8f : 1.2f;
                    getWorld().playSound(null, target.getBlockPos(),
                            SoundEvents.ENTITY_BLAZE_HURT, SoundCategory.PLAYERS,
                            1.0f, pitch);

                    createHitEffect(target.getPos(), falloffMultiplier);
                }
            }
        }
    }

    private void createExplosionEffects() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;
        double baseX = this.getX();
        double baseY = this.getY();
        double baseZ = this.getZ();

        // Get all players within 256 blocks of the explosion
        for (ServerPlayerEntity player : PlayerLookup.around(serverWorld, new Vec3d(baseX, baseY, baseZ), 256)) {
            serverWorld.spawnParticles(player, ParticleTypes.EXPLOSION, true, baseX, baseY + 1, baseZ, 15, 1, 1, 1, 0.1);

            serverWorld.spawnParticles(player, ModParticles.METEOR_IMPACT, true, baseX, baseY, baseZ, 1, 0.0, 0.0, 0.0, 0.0);

            // Mushroom cloud stem - vertical smoke column
            for (int i = 0; i < 10; i++) {
                double stemY = baseY + (i * 1.2);
                int color = 0x808080 + (i * 0x080808);
                DustParticleEffect grayDust = new DustParticleEffect(Vec3d.unpackRgb(color).toVector3f(), 2.5f);
                serverWorld.spawnParticles(player, grayDust, true, baseX, stemY, baseZ, 15, 0.4, 0.1, 0.4, 0.03);
                serverWorld.spawnParticles(player, ParticleTypes.LARGE_SMOKE, true, baseX, stemY, baseZ, 25, 0.3, 0.2, 0.3, 0.03);
                serverWorld.spawnParticles(player, ParticleTypes.CAMPFIRE_COSY_SMOKE, true, baseX, stemY, baseZ, 15, 0.2, 0.1, 0.2, 0.02);
                if (i < 3) {
                    serverWorld.spawnParticles(player, ParticleTypes.FLAME, true, baseX, stemY, baseZ, 10, 0.3, 0.1, 0.3, 0.05);
                }
            }

            // Mushroom cloud head - core and layered smoke
            double headHeight = baseY + 20;
            DustParticleEffect orangeGlow = new DustParticleEffect(Vec3d.unpackRgb(0xFF4500).toVector3f(), 2.0f);
            serverWorld.spawnParticles(player, orangeGlow, true, baseX, headHeight, baseZ, 30, 0.5, 0.5, 0.5, 0.01);
            serverWorld.spawnParticles(player, ParticleTypes.SOUL_FIRE_FLAME, true, baseX, headHeight, baseZ, 20, 0.4, 0.4, 0.4, 0.02);

            for (int layer = 0; layer < 14; layer++) {
                double layerY = headHeight + (layer * 0.4) - 2;
                double layerRadius = 9 - (layer * 0.8);
                if (layerRadius < 0) continue;

                for (int i = 0; i < 40; i++) {
                    double angle = (2 * Math.PI * i) / 40;
                    double offsetX = Math.cos(angle) * layerRadius;
                    double offsetZ = Math.sin(angle) * layerRadius;
                    serverWorld.spawnParticles(player, ParticleTypes.CAMPFIRE_COSY_SMOKE, true, baseX + offsetX, layerY, baseZ + offsetZ, 6, 0.4, 0.5, 0.4, 0.03);
                    if (layer < 5) {
                        DustParticleEffect grayDust = new DustParticleEffect(Vec3d.unpackRgb(0xA0A0A0).toVector3f(), 1.5f);
                        serverWorld.spawnParticles(player, grayDust, true, baseX + offsetX, layerY, baseZ + offsetZ, 4, 0.3, 0.3, 0.3, 0.02);
                    }
                }
            }

            // Expanding rings for mushroom head
            for (int ring = 1; ring <= 5; ring++) {
                double radius = ring * 3.5;
                int particleCount = ring * 8;
                double ringHeight = headHeight - (ring * 3.5) + 3;

                for (int i = 0; i < particleCount; i++) {
                    double angle = (2 * Math.PI * i) / particleCount;
                    double offsetX = Math.cos(angle) * radius;
                    double offsetZ = Math.sin(angle) * radius;

                    if (ring <= 2) continue;

                    DustParticleEffect redDust = new DustParticleEffect(Vec3d.unpackRgb(0xFF3333).toVector3f(), 2.5f);
                    serverWorld.spawnParticles(player, redDust, true, baseX + offsetX, ringHeight, baseZ + offsetZ, 25, 0.6, 0.4, 0.6, 0.05);
                    serverWorld.spawnParticles(player, ParticleTypes.FLAME, true, baseX + offsetX, ringHeight, baseZ + offsetZ, 15, 0.4, 0.3, 0.4, 0.04);
                }
            }
        }
    }

    private void createHitEffect(Vec3d pos, double intensity) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        int particleCount = (int) (10 * intensity);

        // Fire burst at hit location
        for (int i = 0; i < particleCount; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2.0;
            double offsetY = (random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0;

            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    1, offsetX * 0.1, offsetY * 0.2, offsetZ * 0.1, 0.15);
        }
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected float getDragInWater() {
        return 0.95f; // Slightly affected by water
    }
}