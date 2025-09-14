package com.sypztep.mamy.common.entity.skill;

import com.google.common.collect.Maps;
import com.sypztep.mamy.client.particle.complex.SparkParticleEffect;
import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.network.client.CameraShakePayloadS2C;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ThunderSphereEntity extends PersistentProjectileEntity {
    private static final int MAX_LIFETIME = 60; // 3 seconds
    private static final int DAMAGE_INTERVAL = 10; // Every 10 ticks for 2 hits
    private static final double EXPLOSION_RADIUS = 6.0; // Large sphere radius
    private static final double CAMERA_SHAKE_RANGE = 30.0;
    private static final int CIRCLE_POINTS = 12; // จำนวนจุดในวงกลม

    private final Map<UUID, Integer> hitCounts = Maps.newHashMap();
    private final float baseDamage;
    private final int skillLevel;

    // เก็บตำแหน่งของจุดในวงกลมสำหรับ spark connections
    private final List<Vec3d> circlePoints = new ArrayList<>();

    private int ticksAlive = 0;
    private int damageTimer = 0;
    private int explosionHits = 0; // Track explosion damage hits (max 2)
    private boolean hasExploded = false;

    public ThunderSphereEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
        this.baseDamage = 0f;
        this.skillLevel = 1;
        initializeCirclePoints();
    }

    public ThunderSphereEntity(World world, LivingEntity owner, float baseDamage, int skillLevel) {
        super(ModEntityTypes.THUNDER_SPHERE, owner, world, ItemStack.EMPTY, null);
        this.baseDamage = baseDamage;
        this.skillLevel = skillLevel;
        initializeCirclePoints();

        Vec3d direction = owner.getRotationVec(1.0f);
        this.setVelocity(direction.multiply(1.3)); // Medium speed
        this.velocityModified = true;
    }

    private void initializeCirclePoints() {
        circlePoints.clear();
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            double angle = (i / (double) CIRCLE_POINTS) * 2 * Math.PI;
            circlePoints.add(new Vec3d(angle, 0, 0)); // เก็บเป็น angle ไว้ก่อน
        }
    }

    @Override
    public void tick() {
        super.tick();
        ticksAlive++;

        if (getWorld().isClient) {
            createElectricSphereEffects();
        } else {
            // Server-side: สร้าง spark connections สำหรับ clients ทุกๆ 3 ticks
            if (ticksAlive % 3 == 0) {
                createSparkConnections();
            }
        }

        // If exploded, handle explosion damage cycles
        if (hasExploded && explosionHits < 2 && !getWorld().isClient) {
            damageTimer++;
            if (damageTimer >= DAMAGE_INTERVAL) {
                dealSphereExplosion();
                damageTimer = 0;
                explosionHits++;
            }
        }

        // Remove after lifetime or after 2 explosion hits
        if (ticksAlive >= MAX_LIFETIME || explosionHits >= 2) {
            discard();
        }
    }

    private void createElectricSphereEffects() {
        // Electric sphere aura while traveling - ใช้ built-in particles สำหรับพื้นฐาน
        for (int i = 0; i < 4; i++) {
            double angle = (i / 4.0) * 2 * Math.PI + (ticksAlive * 0.1);
            double radius = 1.0 + Math.sin((ticksAlive + i * 10) * 0.1) * 0.2;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            double offsetY = Math.sin((ticksAlive + i * 15) * 0.15) * 0.3;

            getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                    0.0, 0.0, 0.0);
        }

        // Core electric energy
        if (ticksAlive % 2 == 0) {
            for (int i = 0; i < 2; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.5;
                double offsetY = (random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (random.nextDouble() - 0.5) * 0.5;

                getWorld().addParticle(ParticleTypes.END_ROD,
                        getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                        0.0, 0.0, 0.0);
            }
        }

        // Electric charging sound
        if (ticksAlive % 20 == 0) {
            getWorld().playSound(getX(), getY(), getZ(),
                    SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.PLAYERS,
                    0.2f, 1.8f, false);
        }
    }

    private void createSparkConnections() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d center = getPos();

        // สร้าง 2 วงกลมที่หมุนในทิศทางตรงข้าม
        createRotatingCircle(serverWorld, center, 1.8, ticksAlive * 0.15, false); // วงกลมใหญ่
        createRotatingCircle(serverWorld, center, 1.2, -ticksAlive * 0.2, true);  // วงกลมเล็ก หมุนย้อน

        // สร้าง vertical connections ทุกๆ 5 ticks
        if (ticksAlive % 5 == 0) {
            createVerticalSparks(serverWorld, center);
        }
    }

    private void createRotatingCircle(ServerWorld world, Vec3d center, double radius, double rotationOffset, boolean reverse) {
        List<Vec3d> currentPoints = new ArrayList<>();

        // คำนวณตำแหน่งจุดในวงกลม
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            double angle = (i / (double) CIRCLE_POINTS) * 2 * Math.PI + rotationOffset;
            if (reverse) angle = -angle;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + Math.sin(angle * 3 + ticksAlive * 0.1) * 0.3; // เคลื่อนไหวในแนวตั้ง

            currentPoints.add(new Vec3d(x, y, z));
        }

        // สร้าง spark connections ระหว่างจุดที่ติดกัน
        for (int i = 0; i < currentPoints.size(); i++) {
            Vec3d start = currentPoints.get(i);
            Vec3d end = currentPoints.get((i + 1) % currentPoints.size()); // จุดถัดไป (วนกลับไปจุดแรก)

            // สร้าง SparkParticleEffect ระหว่าง 2 จุด (velocityX = 0 สำหรับ normal spark)
            SparkParticleEffect sparkEffect = new SparkParticleEffect(end);
            world.spawnParticles(sparkEffect, start.x, start.y, start.z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        // สร้างการเชื่อมต่อข้ามวงกลม (เป็นครั้งคราว)
        if (ticksAlive % 8 == 0) {
            for (int i = 0; i < currentPoints.size(); i += 3) {
                Vec3d start = currentPoints.get(i);
                Vec3d end = currentPoints.get((i + 6) % currentPoints.size()); // จุดตรงข้าม

                SparkParticleEffect crossSparkEffect = new SparkParticleEffect(end);
                world.spawnParticles(crossSparkEffect, start.x, start.y, start.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    private void createVerticalSparks(ServerWorld world, Vec3d center) {
        // สร้าง vertical sparks ขึ้นลง
        Vec3d topPoint = center.add(0, 2, 0);
        Vec3d bottomPoint = center.add(0, -1, 0);

        // Spark จากศูนย์กลางขึ้นบน
        SparkParticleEffect upwardSpark = new SparkParticleEffect(topPoint);
        world.spawnParticles(upwardSpark, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);

        // Spark จากศูนย์กลางลงล่าง
        SparkParticleEffect downwardSpark = new SparkParticleEffect(bottomPoint);
        world.spawnParticles(downwardSpark, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!getWorld().isClient) {
            explode();
            entityHitResult.getEntity().damage(
                    ModDamageTypes.create(getWorld(), ModDamageTypes.LIGHTING, this, getOwner()), // Thunder damage type
                    baseDamage * 2
            );
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

        // Play massive electric explosion sound
        getWorld().playSound(null, getBlockPos(),
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS,
                2.5f, 0.8f);

        // Electric discharge sound
        getWorld().playSound(null, getBlockPos(),
                SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS,
                2.0f, 1.2f);

        // Create electric sphere explosion effects
        createElectricExplosionEffects();

        // Camera shake for nearby players
        if (getWorld() instanceof ServerWorld serverWorld) {
            PlayerLookup.around(serverWorld, getPos(), CAMERA_SHAKE_RANGE)
                    .forEach(player -> CameraShakePayloadS2C.send(player,
                            getX(), getY(), getZ(), 15, CAMERA_SHAKE_RANGE, 12));
        }

        // Start explosion damage cycle
        damageTimer = 0;
    }

    private void dealSphereExplosion() {
        Box damageBox = Box.of(getPos(), EXPLOSION_RADIUS * 2, EXPLOSION_RADIUS * 2, EXPLOSION_RADIUS * 2);

        for (LivingEntity target : getWorld().getEntitiesByClass(
                LivingEntity.class,
                damageBox,
                entity -> entity != getOwner() && entity.isAlive() &&
                        (getOwner() == null || !entity.isTeammate(getOwner()))
        )) {
            double distance = target.getPos().distanceTo(getPos());

            if (distance <= EXPLOSION_RADIUS) {
                UUID targetId = target.getUuid();
                int currentHits = hitCounts.getOrDefault(targetId, 0);

                // Each target can be hit once per explosion cycle
                if (currentHits >= explosionHits + 1) {
                    continue;
                }

                // Calculate damage falloff (100% at center, 40% at edge)
                double falloffMultiplier = Math.max(0.4, 1.0 - (distance / EXPLOSION_RADIUS) * 0.6);
                float finalDamage = (float) (baseDamage * falloffMultiplier);

                boolean damageDealt = target.damage(
                        ModDamageTypes.create(getWorld(), ModDamageTypes.FIREBALL, this, getOwner()), // Thunder damage type
                        finalDamage
                );

                if (damageDealt) {
                    // Apply slowness effect (Speed -40%)
                    int duration = (int) (80 + (1.0 - (distance / EXPLOSION_RADIUS)) * 40); // 4-6 seconds based on distance
                    target.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.SLOWNESS, duration, 1,
                            false, true, true
                    ));

                    hitCounts.put(targetId, explosionHits + 1);

                    // Hit sound with pitch based on explosion number
                    float pitch = explosionHits == 0 ? 1.0f : 1.4f;
                    getWorld().playSound(null, target.getBlockPos(),
                            SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS,
                            0.9f, pitch);

                    createElectricHitEffect(target.getPos(), falloffMultiplier);
                }
            }
        }
    }

    private void createElectricExplosionEffects() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d center = getPos();

        // สร้าง explosion sparks แบบวงกลมหลายชั้น
        for (int ring = 1; ring <= 8; ring++) {
            int pointsInRing = ring * 6;
            double ringRadius = ring * 0.8;

            List<Vec3d> ringPoints = new ArrayList<>();

            // คำนวณจุดในแต่ละชั้น
            for (int i = 0; i < pointsInRing; i++) {
                double angle = (i / (double) pointsInRing) * 2 * Math.PI;
                double x = center.x + Math.cos(angle) * ringRadius;
                double z = center.z + Math.sin(angle) * ringRadius;
                double y = center.y + (random.nextDouble() - 0.5) * 2.0;

                ringPoints.add(new Vec3d(x, y, z));
            }

            // สร้าง spark connections ในแต่ละชั้น
            for (int i = 0; i < ringPoints.size(); i++) {
                Vec3d start = ringPoints.get(i);
                Vec3d end = ringPoints.get((i + 1) % ringPoints.size());

                // Spark ระหว่างจุดติดกัน (velocityX = 1.0 สำหรับ explosion spark)
                SparkParticleEffect ringSparkEffect = new SparkParticleEffect(end);
                serverWorld.spawnParticles(ringSparkEffect, start.x, start.y, start.z, 1, 1.0, 0.0, 0.0, 0.0);

                // Spark จากศูนย์กลางไปยังจุดในชั้น
                if (i % 2 == 0) { // ทำแค่บางจุดเพื่อไม่ให้หนาเกินไป
                    SparkParticleEffect centerToRingSpark = new SparkParticleEffect(start);
                    serverWorld.spawnParticles(centerToRingSpark, center.x, center.y, center.z, 1, 1.0, 0.0, 0.0, 0.0);
                }
            }
        }

        // Central electric pillar ด้วย sparks
        for (int y = -3; y <= 6; y++) {
            Vec3d pillarStart = center.add(0, y, 0);
            Vec3d pillarEnd = center.add(0, y + 1, 0);

            SparkParticleEffect pillarSpark = new SparkParticleEffect(pillarEnd);
            serverWorld.spawnParticles(pillarSpark, pillarStart.x, pillarStart.y, pillarStart.z, 1, 1.0, 0.0, 0.0, 0.0);
        }

        // Random chaotic sparks
        for (int i = 0; i < 30; i++) {
            Vec3d randomStart = center.add(
                    (random.nextDouble() - 0.5) * 4.0,
                    (random.nextDouble() - 0.5) * 3.0,
                    (random.nextDouble() - 0.5) * 4.0
            );

            Vec3d randomEnd = center.add(
                    (random.nextDouble() - 0.5) * 8.0,
                    (random.nextDouble() - 0.5) * 4.0,
                    (random.nextDouble() - 0.5) * 8.0
            );

            SparkParticleEffect chaoticSpark = new SparkParticleEffect(randomEnd);
            serverWorld.spawnParticles(chaoticSpark, randomStart.x, randomStart.y, randomStart.z, 1, 1.0, 0.0, 0.0, 0.0);
        }

        // Flash และ electric discharge particles เพิ่มเติม
        for (int i = 0; i < 8; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 4.0;
            double offsetY = (random.nextDouble() - 0.5) * 3.0;
            double offsetZ = (random.nextDouble() - 0.5) * 4.0;

            serverWorld.spawnParticles(ParticleTypes.FLASH,
                    center.x + offsetX, center.y + offsetY, center.z + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void createElectricHitEffect(Vec3d pos, double intensity) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        // สร้าง hit effect ด้วย spark connections
        Vec3d center = pos;

        // สร้างวงกลมเล็กๆ รอบจุดที่โดน
        int points = (int) (6 * intensity);
        List<Vec3d> hitPoints = new ArrayList<>();

        for (int i = 0; i < points; i++) {
            double angle = (i / (double) points) * 2 * Math.PI;
            double radius = 1.0 * intensity;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + (random.nextDouble() - 0.5) * 0.5;

            hitPoints.add(new Vec3d(x, y, z));
        }

        // สร้าง spark connections ระหว่างจุด hit
        for (int i = 0; i < hitPoints.size(); i++) {
            Vec3d start = hitPoints.get(i);
            Vec3d end = hitPoints.get((i + 1) % hitPoints.size());

            SparkParticleEffect hitSpark = new SparkParticleEffect(end);
            serverWorld.spawnParticles(hitSpark, start.x, start.y, start.z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        // Lightning pillar at hit location
        for (int y = 0; y <= 3; y++) {
            serverWorld.spawnParticles(ParticleTypes.END_ROD,
                    pos.x, pos.y + y, pos.z,
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

    @Override
    public boolean shouldRender(double distance) {
        return distance < 96.0 * 96.0;
    }
}