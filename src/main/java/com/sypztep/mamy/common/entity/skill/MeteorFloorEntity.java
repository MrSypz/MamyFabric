package com.sypztep.mamy.common.entity.skill;

import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.util.MultiHitRecord;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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

public class MeteorFloorEntity extends PersistentProjectileEntity {
    private static final int MAX_LIFETIME = 20; // 1 second
    private static final int DAMAGE_INTERVAL = 4; // Every 4 ticks
    private static final int MAX_DAMAGE_HITS = 4; // 4 damage hits, then spawn meteor

    private final MultiHitRecord hitRecord;
    private final float baseDamage;

    private int ticksAlive = 0;
    private int damageTimer = 0;
    private int damageHitCount = 0; // Track how many damage cycles we've done
    private boolean hasSpawnedMeteor = false;

    public MeteorFloorEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
        this.baseDamage = 0f;
        hitRecord = new MultiHitRecord(MAX_DAMAGE_HITS);
    }

    public MeteorFloorEntity(World world, LivingEntity owner, float baseDamage) {
        super(ModEntityTypes.METEOR_FLOOR, owner, world, ItemStack.EMPTY, null);
        this.baseDamage = baseDamage;
        hitRecord = new MultiHitRecord(MAX_DAMAGE_HITS);

        this.setNoGravity(true);
        this.setVelocity(Vec3d.ZERO);
    }

    @Override
    public void tick() {
        super.tick();
        ticksAlive++;
        damageTimer++;

        if (getWorld().isClient) {
            createSmokeEffects();
        }

        // Deal damage every 4 ticks, max 3 times
        if (damageTimer >= DAMAGE_INTERVAL && !getWorld().isClient && damageHitCount < MAX_DAMAGE_HITS) {
            dealAreaDamage();
            damageTimer = 0;
            damageHitCount++;
        }

        // After 3 damage hits, spawn meteor on 4th cycle
        if (damageHitCount >= MAX_DAMAGE_HITS && !hasSpawnedMeteor && !getWorld().isClient) {
            spawnMeteor();
            hasSpawnedMeteor = true;
        }

        // Remove after lifetime
        if (ticksAlive >= MAX_LIFETIME) {
            discard();
        }
    }

    private void createSmokeEffects() {
        // Campfire smoke scattered across the 8x8 area
        for (int i = 0; i < 4; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 8.0; // 8x8 area
            double offsetZ = (random.nextDouble() - 0.5) * 8.0;

            getWorld().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    getX() + offsetX, getY() + 0.1, getZ() + offsetZ,
                    0.0, 0.1, 0.0); // Smoke rising up
        }

        // Additional flame particles for danger indication
        if (random.nextFloat() < 0.3f) {
            double offsetX = (random.nextDouble() - 0.5) * 7.0;
            double offsetZ = (random.nextDouble() - 0.5) * 7.0;

            getWorld().addParticle(ParticleTypes.FLAME,
                    getX() + offsetX, getY() + 0.05, getZ() + offsetZ,
                    0.0, 0.05, 0.0);
        }
    }

    private void dealAreaDamage() {
        // Use entity's own hitbox for damage area (8x8)
        Box damageBox = getBoundingBox().expand(0, 15, 0); // Extend vertically to hit flying entities

        for (LivingEntity target : getWorld().getEntitiesByClass(
                LivingEntity.class,
                damageBox,
                entity -> entity != getOwner() && entity.isAlive() &&
                        (getOwner() == null || !entity.isTeammate(getOwner()))
        )) {
            int currentHits = hitRecord.getHitCount(target);

            // Each damage cycle can hit each target once
            if (currentHits >= damageHitCount) {
                continue; // Skip if already hit in this cycle
            }

            boolean damageDealt = target.damage(
                    ModDamageTypes.create(getWorld(), ModDamageTypes.FIREBALL, this, getOwner()),
                    baseDamage
            );

            if (damageDealt) {
                target.setOnFireFor(3); // Set on fire
                hitRecord.recordHit(target);

                // Play hit sound with increasing pitch
                float pitch = 1.0f + (damageHitCount * 0.2f);
                getWorld().playSound(null, target.getBlockPos(),
                        SoundEvents.ENTITY_BLAZE_HURT, SoundCategory.PLAYERS,
                        0.6f, pitch);

                createHitEffect(target.getPos());
            }
        }

        // Play ground rumble sound
        float pitch = 1.0f + (damageHitCount * 0.1f);
        getWorld().playSound(null, getBlockPos(),
                SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS,
                0.4f, pitch);
    }

    private void spawnMeteor() {
        if (getOwner() instanceof LivingEntity owner) {
            MeteorEntity meteor = new MeteorEntity(getWorld(), owner, baseDamage * 2);

            // Spawn meteor high above this position
            meteor.setPosition(getX(), getY() + 50, getZ());
            meteor.setVelocity(0, -1.5, 0); // Fall downward

            getWorld().spawnEntity(meteor);

            // Play summoning sound
            getWorld().playSound(null, getBlockPos(),
                    SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS,
                    1.0f, 0.8f);
        }
    }

    private void createHitEffect(Vec3d pos) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        for (int i = 0; i < 8; i++) {
            double offsetX = (random.nextDouble() - 0.5);
            double offsetY = (random.nextDouble() - 0.5);
            double offsetZ = (random.nextDouble() - 0.5);

            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    1, 0.0, 0.1, 0.0, 0.1);
        }

        // Smoke puff
        for (int i = 0; i < 3; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.8;
            double offsetY = (random.nextDouble() - 0.5) * 0.8;
            double offsetZ = (random.nextDouble() - 0.5) * 0.8;

            serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    1, 0.0, 0.05, 0.0, 0.05);
        }
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