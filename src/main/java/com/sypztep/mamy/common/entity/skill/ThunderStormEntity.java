package com.sypztep.mamy.common.entity.skill;

import com.sypztep.mamy.client.particle.complex.SparkParticleEffect;
import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.init.ModParticles;
import com.sypztep.mamy.common.init.ModSoundEvents;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.util.MultiHitRecord;
import com.sypztep.mamy.common.util.SkillUtil;
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

import java.util.ArrayList;
import java.util.List;

public class ThunderStormEntity extends PersistentProjectileEntity {
    private static final int MAX_LIFETIME = 25; // 1.25 seconds
    private static final int DAMAGE_INTERVAL = 4; // Every 5 ticks
    private static final int MAX_DAMAGE_HITS = 10; // 5 lightning strikes
    private static final double STORM_RADIUS = 8.0; // Storm area radius

    private final MultiHitRecord hitTracker;
    private final float baseDamage;
    private final int skillLevel;

    private int ticksAlive = 0;
    private int damageTimer = 0;
    private int damageHitCount = 0;

    public ThunderStormEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
        this.baseDamage = 0f;
        this.skillLevel = 1;
        this.hitTracker = new MultiHitRecord(MAX_DAMAGE_HITS);
    }

    public ThunderStormEntity(World world, LivingEntity owner, float baseDamage, int skillLevel) {
        super(ModEntityTypes.THUNDERSTORM, owner, world, ItemStack.EMPTY, null);
        this.baseDamage = baseDamage;
        this.skillLevel = skillLevel;

        this.hitTracker = new MultiHitRecord(MAX_DAMAGE_HITS);

        this.setNoGravity(true);
        this.setVelocity(Vec3d.ZERO);
    }

    @Override
    public void tick() {
        super.tick();
        ticksAlive++;
        damageTimer++;

        if (getWorld().isClient) {
            createStormEffects();
        } else {
            if (ticksAlive % 3 == 0) createServerStormEffects();
        }

        if (damageTimer >= DAMAGE_INTERVAL && !getWorld().isClient && damageHitCount < MAX_DAMAGE_HITS) {
            dealLightningStrike();
            damageTimer = 0;
            damageHitCount++;
        }

        if (ticksAlive >= MAX_LIFETIME) discard();
    }

    private void createStormEffects() {
        for (int i = 0; i < 4; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 12.0;
            double offsetZ = (random.nextDouble() - 0.5) * 12.0;
            double cloudHeight = 12.0 + (random.nextDouble() * 4.0);

            getWorld().addParticle(ModParticles.CLOUD,
                    getX() + offsetX, getY() + cloudHeight, getZ() + offsetZ,
                    0, 0, 0);
        }

        // Electric particles in the storm area
        for (int i = 0; i < 2; i++) {
            double offsetX = (random.nextDouble() - 0.5) * STORM_RADIUS * 2;
            double offsetZ = (random.nextDouble() - 0.5) * STORM_RADIUS * 2;

            getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    getX() + offsetX, getY() + 0.1, getZ() + offsetZ,
                    0.0, 0.1, 0.0);
        }

        if (ticksAlive % 10 == 0) {
            getWorld().playSound(getX(), getY(), getZ(),
                    ModSoundEvents.ENTITY_ELECTRIC_BIG_EXPLODE, SoundCategory.PLAYERS,
                    1f, 0.8f + (random.nextFloat() * 0.4f), false);
        }
    }

    private void createServerStormEffects() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d center = getPos();

        // สร้าง ambient lightning แบบสุ่มในพื้นที่พายุ
        if (serverWorld.getRandom().nextFloat() < 0.3f) {
            double offsetX = (serverWorld.getRandom().nextDouble() - 0.5) * STORM_RADIUS * 1.5;
            double offsetZ = (serverWorld.getRandom().nextDouble() - 0.5) * STORM_RADIUS * 1.5;

            Vec3d skyPos = center.add(offsetX, 12 + serverWorld.getRandom().nextDouble() * 4, offsetZ);
            Vec3d groundPos = center.add(offsetX, 0, offsetZ);

            // สร้าง vertical lightning จากฟ้าลงดิน
            SparkParticleEffect verticalSpark = new SparkParticleEffect(groundPos);
            serverWorld.spawnParticles(verticalSpark,
                    skyPos.x, skyPos.y, skyPos.z,
                    1, 0.0, 0.0, 0.0, 0.0); // velocityX = 0 สำหรับ normal spark
        }

        // สร้าง horizontal lightning ระหว่างเมฆ
        if (serverWorld.getRandom().nextFloat() < 0.2f) {
            double cloudHeight = 10 + serverWorld.getRandom().nextDouble() * 3;
            Vec3d cloudStart = center.add(
                    (serverWorld.getRandom().nextDouble() - 0.5) * STORM_RADIUS * 2,
                    cloudHeight,
                    (serverWorld.getRandom().nextDouble() - 0.5) * STORM_RADIUS * 2
            );
            Vec3d cloudEnd = center.add(
                    (serverWorld.getRandom().nextDouble() - 0.5) * STORM_RADIUS * 2,
                    cloudHeight + (serverWorld.getRandom().nextDouble() - 0.5) * 2,
                    (serverWorld.getRandom().nextDouble() - 0.5) * STORM_RADIUS * 2
            );

            SparkParticleEffect cloudSpark = new SparkParticleEffect(cloudEnd);
            serverWorld.spawnParticles(cloudSpark,
                    cloudStart.x, cloudStart.y, cloudStart.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void dealLightningStrike() {
        Box damageBox = SkillUtil.makeBox(getPos(), STORM_RADIUS * 2, 15, STORM_RADIUS * 2);

        List<LivingEntity> potentialTargets = getWorld().getEntitiesByClass(
                LivingEntity.class,
                damageBox,
                entity -> entity != getOwner() && entity.isAlive() &&
                        (getOwner() == null || !entity.isTeammate(getOwner())) &&
                        entity.getPos().distanceTo(getPos()) <= STORM_RADIUS
        );

        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d center = getPos();

        int strikeCount = 2 + serverWorld.getRandom().nextInt(3);
        List<Vec3d> strikePositions = new ArrayList<>();

        float targetingChance = 0.5f + (0.05f * skillLevel);
        targetingChance = Math.min(targetingChance, 0.95f); // Cap at 95% to maintain some randomness

        for (int i = 0; i < strikeCount; i++) {
            Vec3d strikePos;

            if (!potentialTargets.isEmpty() && serverWorld.getRandom().nextFloat() < targetingChance) {
                LivingEntity target = potentialTargets.get(serverWorld.getRandom().nextInt(potentialTargets.size()));
                strikePos = target.getPos();

                // Deal damage
                if (hitTracker.canHit(target)) {
                    boolean damageDealt = target.damage(
                            ModDamageTypes.create(getWorld(), ModDamageTypes.LIGHTING, this, getOwner()),
                            baseDamage
                    );

                    if (damageDealt) {
                        target.addStatusEffect(new StatusEffectInstance(
                                StatusEffects.SLOWNESS, 60, 1,
                                false, true, true
                        ));

                        int hitCount = hitTracker.recordHitAndGet(target);

                        float pitch = 0.8f + (hitCount * 0.1f) + (serverWorld.getRandom().nextFloat() * 0.4f);
                        getWorld().playSound(null, target.getBlockPos(),
                                ModSoundEvents.ENTITY_GENERIC_HEADSHOT, SoundCategory.PLAYERS,
                                1.5f, pitch);
                    }
                }
            } else {
                // Strike random ground position
                strikePos = center.add(
                        (serverWorld.getRandom().nextDouble() - 0.5) * STORM_RADIUS * 1.6,
                        0,
                        (serverWorld.getRandom().nextDouble() - 0.5) * STORM_RADIUS * 1.6
                );
            }

            strikePositions.add(strikePos);
        }

        // Create lightning strike effects
        createMultipleLightningStrikes(serverWorld, strikePositions);

        // Main thunder sound for each strike wave
        float pitch = 1.25f + (damageHitCount * 0.05f);
        getWorld().playSound(null, getBlockPos(),
                ModSoundEvents.ENTITY_ELECTRIC_BLAST, SoundCategory.PLAYERS,
                1.2f, pitch);
    }

    private void createMultipleLightningStrikes(ServerWorld serverWorld, List<Vec3d> strikePositions) {
        for (int i = 0; i < strikePositions.size(); i++) {
            Vec3d strikePos = strikePositions.get(i);

            // สร้าง main vertical lightning strike
            double skyHeight = 15 + serverWorld.getRandom().nextDouble() * 5;
            Vec3d skyStart = strikePos.add(
                    (serverWorld.getRandom().nextDouble() - 0.5) * 2.0,
                    skyHeight,
                    (serverWorld.getRandom().nextDouble() - 0.5) * 2.0
            );

            // Main lightning bolt จากฟ้าลงดิน
            SparkParticleEffect mainStrike = new SparkParticleEffect(strikePos);
            serverWorld.spawnParticles(mainStrike,
                    skyStart.x, skyStart.y, skyStart.z,
                    1, 1.0, 0.0, 0.0, 0.0); // velocityX = 1.0 สำหรับ explosion spark

            // สร้าง branching lightning
            createBranchingLightning(serverWorld, skyStart, strikePos);

            // Ground impact effects
            createGroundImpactEffect(serverWorld, strikePos);

            // สร้าง connection lightning ระหว่าง strikes
            if (i > 0 && serverWorld.getRandom().nextFloat() < 0.4f) {
                Vec3d prevStrike = strikePositions.get(i - 1);
                Vec3d connectionStart = prevStrike.add(0, 2 + serverWorld.getRandom().nextDouble() * 3, 0);
                Vec3d connectionEnd = strikePos.add(0, 2 + serverWorld.getRandom().nextDouble() * 3, 0);

                SparkParticleEffect connectionSpark = new SparkParticleEffect(connectionEnd);
                serverWorld.spawnParticles(connectionSpark,
                        connectionStart.x, connectionStart.y, connectionStart.z,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    private void createBranchingLightning(ServerWorld serverWorld, Vec3d skyStart, Vec3d groundEnd) {
        // สร้าง 2-4 branch lightning
        int branchCount = 2 + serverWorld.getRandom().nextInt(3);

        for (int i = 0; i < branchCount; i++) {
            // Branch ออกจากจุดกลางของ main bolt
            double branchHeight = skyStart.y * 0.3 + (serverWorld.getRandom().nextDouble() * skyStart.y * 0.4);
            Vec3d branchStart = new Vec3d(
                    skyStart.x + (groundEnd.x - skyStart.x) * 0.5,
                    branchHeight,
                    skyStart.z + (groundEnd.z - skyStart.z) * 0.5
            );

            Vec3d branchEnd = branchStart.add(
                    (serverWorld.getRandom().nextDouble() - 0.5) * 4.0,
                    -(branchHeight * 0.5),
                    (serverWorld.getRandom().nextDouble() - 0.5) * 4.0
            );

            SparkParticleEffect branchSpark = new SparkParticleEffect(branchEnd);
            serverWorld.spawnParticles(branchSpark,
                    branchStart.x, branchStart.y, branchStart.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void createGroundImpactEffect(ServerWorld serverWorld, Vec3d pos) {
        // สร้าง radial ground sparks
        int sparkCount = 6 + serverWorld.getRandom().nextInt(4);
        for (int i = 0; i < sparkCount; i++) {
            double angle = (i / (double) sparkCount) * 2 * Math.PI;
            double distance = 1.0 + serverWorld.getRandom().nextDouble() * 1.5;

            Vec3d sparkEnd = pos.add(
                    Math.cos(angle) * distance,
                    0.1,
                    Math.sin(angle) * distance
            );

            SparkParticleEffect groundSpark = new SparkParticleEffect(sparkEnd);
            serverWorld.spawnParticles(groundSpark,
                    pos.x, pos.y + 0.1, pos.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        // Flash at ground impact
        serverWorld.spawnParticles(ParticleTypes.FLASH,
                pos.x, pos.y, pos.z,
                2, 0.0, 0.0, 0.0, 0.0);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        setVelocity(Vec3d.ZERO);
        this.inGround = true;
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }
}