package com.sypztep.mamy.common.entity.skill;

import com.sypztep.mamy.client.particle.complex.SparkParticleEffect;
import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.init.ModSoundEvents;
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
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ThunderBoltEntity extends PersistentProjectileEntity {
    private float damage = 0;
    private int ticksAlive = 0;
    private static final int MAX_LIFETIME = 45; // 2.25 seconds
    private static final int MAX_TARGETS = 5; // Hit up to 5 targets
    private boolean hasDealtDamage = false;

    public ThunderBoltEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public ThunderBoltEntity(World world, LivingEntity owner, float damage) {
        super(ModEntityTypes.THUNDER_BOLT, owner, world, ItemStack.EMPTY, null);
        this.damage = damage;

        Vec3d direction = owner.getRotationVec(1.0f);
        this.setVelocity(direction.multiply(2.0)); // Very fast lightning bolt
        this.velocityModified = true;
    }

    @Override
    public void tick() {
        super.tick();
        ticksAlive++;

        if (getWorld().isClient) {
            createLightningTrail();
        } else {
            // Server-side: สร้าง lightning trail ทุกๆ 2 ticks
            if (ticksAlive % 2 == 0) {
                createServerLightningTrail();
            }
        }

        if (ticksAlive >= MAX_LIFETIME) {
            discard();
        }

        if (!getWorld().isClient && age > 100) {
            discard();
        }
    }

    private void createLightningTrail() {
    }

    private void createServerLightningTrail() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        // สร้าง jagged lightning trail หลัง projectile
        Vec3d currentPos = getPos();
        Vec3d velocity = getVelocity();
        Vec3d trailStart = currentPos.subtract(velocity.normalize().multiply(1.5)); // เริ่มหลัง projectile

        // สร้าง spark ที่เป็น trail
        SparkParticleEffect trailSpark = new SparkParticleEffect(currentPos);
        serverWorld.spawnParticles(trailSpark,
                trailStart.x, trailStart.y, trailStart.z,
                1, 0.0, 0.0, 0.0, 0.0); // velocityX = 0 สำหรับ normal spark
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!getWorld().isClient && !hasDealtDamage) {
            dealChainLightning(getPos());
            hasDealtDamage = true;
        }
        discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (!getWorld().isClient && !hasDealtDamage) {
            dealChainLightning(blockHitResult.getPos());
            hasDealtDamage = true;
            createBlockHitEffect();
        }
        discard();
    }

    private void dealChainLightning(Vec3d hitPos) {
        // Find up to 5 targets in a 8 block radius
        List<LivingEntity> targets = getWorld().getEntitiesByClass(
                LivingEntity.class,
                SkillUtil.makeBox(hitPos,16,16,16),
                entity -> entity != getOwner() && entity.isAlive() &&
                        (getOwner() == null || !entity.isTeammate(getOwner())) &&
                        entity.getPos().distanceTo(hitPos) <= 8.0
        );

        // Sort by distance and take up to 5 closest targets
        targets.sort(Comparator.comparingDouble(a -> a.getPos().distanceTo(hitPos)));
        int hitCount = Math.min(targets.size(), MAX_TARGETS);

        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        List<Vec3d> hitPositions = new ArrayList<>();
        hitPositions.add(hitPos);

        // สร้าง chain lightning ระหว่าง targets
        for (int i = 0; i < hitCount; i++) {
            LivingEntity target = targets.get(i);
            Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0); // Center of target
            hitPositions.add(targetPos);

            boolean damageDealt = target.damage(
                    ModDamageTypes.create(getWorld(), ModDamageTypes.LIGHTING, this, getOwner()),
                    damage
            );

            if (damageDealt) {
                // Apply slowness effect (Speed -40%)
                target.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS, 60, 1, // 3 seconds, level 2 (40% speed reduction)
                        false, true, true
                ));

                // Lightning hit sound
                getWorld().playSound(null, getBlockPos(),
                        ModSoundEvents.ENTITY_GENERIC_HEADSHOT, SoundCategory.PLAYERS,
                        1.5f, 1f + (i * 0.1f));

                createLightningHitEffect(targetPos, false);
            }
        }

        // สร้าง chain lightning connections ระหว่าง targets
        createChainLightningEffects(serverWorld, hitPositions);
    }

    private void createChainLightningEffects(ServerWorld serverWorld, List<Vec3d> hitPositions) {
        // สร้าง spark connections ระหว่างจุดที่โดน
        for (int i = 0; i < hitPositions.size() - 1; i++) {
            Vec3d start = hitPositions.get(i);
            Vec3d end = hitPositions.get(i + 1);

            // สร้าง main chain lightning
            SparkParticleEffect chainSpark = new SparkParticleEffect(end);
            serverWorld.spawnParticles(chainSpark,
                    start.x, start.y, start.z,
                    1, 1.0, 0.0, 0.0, 0.0); // velocityX = 1.0 สำหรับ explosion spark

            // สร้าง secondary arcs (50% chance)
            if (serverWorld.getRandom().nextFloat() < 0.5f && i < hitPositions.size() - 2) {
                Vec3d secondaryEnd = hitPositions.get(i + 2);
                SparkParticleEffect secondarySpark = new SparkParticleEffect(secondaryEnd);
                serverWorld.spawnParticles(secondarySpark,
                        start.x, start.y, start.z,
                        1, 0.0, 0.0, 0.0, 0.0); // velocityX = 0 สำหรับ normal spark
            }
        }

        // สร้าง random branching lightning รอบๆ จุดหลัก
        for (Vec3d hitPos : hitPositions) {
            for (int i = 0; i < 3; i++) {
                Vec3d branchEnd = hitPos.add(
                        (serverWorld.getRandom().nextDouble() - 0.5) * 4.0,
                        (serverWorld.getRandom().nextDouble() - 0.5) * 2.0,
                        (serverWorld.getRandom().nextDouble() - 0.5) * 4.0
                );

                SparkParticleEffect branchSpark = new SparkParticleEffect(branchEnd);
                serverWorld.spawnParticles(branchSpark,
                        hitPos.x, hitPos.y, hitPos.z,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    private void createLightningHitEffect(Vec3d pos, boolean isBlockHit) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        // สร้าง radial lightning ออกจากจุดโดน
        int sparkCount = isBlockHit ? 8 : 6;
        for (int i = 0; i < sparkCount; i++) {
            double angle = (i / (double) sparkCount) * 2 * Math.PI;
            double distance = 1.5 + serverWorld.getRandom().nextDouble();

            Vec3d sparkEnd = pos.add(
                    Math.cos(angle) * distance,
                    (serverWorld.getRandom().nextDouble() - 0.5),
                    Math.sin(angle) * distance
            );

            SparkParticleEffect hitSpark = new SparkParticleEffect(sparkEnd);
            serverWorld.spawnParticles(hitSpark,
                    pos.x, pos.y, pos.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        // Bright flash
        serverWorld.spawnParticles(ParticleTypes.FLASH,
                pos.x, pos.y, pos.z,
                1, 0.0, 0.0, 0.0, 0.0);

        getWorld().playSound(null, getBlockPos(),
                ModSoundEvents.ENTITY_ELECTRIC_BLAST, SoundCategory.PLAYERS,
                2f, 1);
    }

    private void createBlockHitEffect() {
        createLightningHitEffect(getPos(), true);
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected float getDragInWater() {
        return 0.99f; // Lightning barely affected by water
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }
}