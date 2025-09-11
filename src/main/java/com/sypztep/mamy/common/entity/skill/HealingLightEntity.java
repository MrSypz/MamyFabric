package com.sypztep.mamy.common.entity.skill;

import com.sypztep.mamy.common.init.ModEntityTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class HealingLightEntity extends PersistentProjectileEntity {
    private static final int MAX_LIFETIME = 60;
    private static final float SQUARE_RADIUS = 2.5f;
    private static final float SQUARE_HEIGHT = 4.0f;
    private static final int PARTICLES_PER_TICK = 2;

    private int lifetime = 0;
    private float intensity = 1.0f;

    public HealingLightEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }
    public HealingLightEntity(LivingEntity owner, World world) {
        super(ModEntityTypes.HEALING_LIGHT, owner, world, ItemStack.EMPTY, null);
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;

        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        // Smooth fade-out in last 20 ticks (1 second)
        float fadeStart = MAX_LIFETIME - 20;
        if (lifetime > fadeStart) {
            intensity = 1.0f - (lifetime - fadeStart) / 20.0f;
        }

        LivingEntity owner = (LivingEntity) this.getOwner();

        if (owner != null && owner.isAlive()) {
            prevX = owner.getX();
            prevY = owner.getY();
            prevZ = owner.getZ();

            double currentX = this.getX();
            double currentY = this.getY();
            double currentZ = this.getZ();

            double distance = Math.sqrt(Math.pow(prevX - currentX, 2) + Math.pow(prevY - currentY, 2) + Math.pow(prevZ - currentZ, 2));

            if (distance > 5) this.setPos(prevX, prevY, prevZ);
            else {
                double newX = MathHelper.lerp(0.5f, currentX, prevX);
                double newY = MathHelper.lerp(0.5f, currentY, prevY);
                double newZ = MathHelper.lerp(0.5f, currentZ, prevZ);

                this.setPos(newX, newY, newZ);
            }
        }

        if (this.getWorld().isClient) {
            renderHealingAura();
        }
    }

    private void renderHealingAura() {
        Vec3d center = this.getPos();
        renderInternalSparks(center);
    }


    private void renderInternalSparks(Vec3d center) {
        for (int i = 0; i < PARTICLES_PER_TICK * intensity; i++) {
            float angle = random.nextFloat() * (float) (2 * Math.PI);
            float radius = (float) Math.abs(random.nextGaussian()) * SQUARE_RADIUS * 0.4f;
            radius = Math.min(radius, SQUARE_RADIUS * 0.9f); // Clamp to stay inside

            float x = radius * (float) Math.cos(angle);
            float z = radius * (float) Math.sin(angle);
            float y = (float) (random.nextGaussian() * SQUARE_HEIGHT * 0.3f);

            // Clamp Y to cylinder bounds
            y = Math.max(-SQUARE_HEIGHT / 2, Math.min(SQUARE_HEIGHT / 2, y));

            // Upward motion with some randomness
            double velocityX = random.nextGaussian() * 0.01;
            double velocityY = 0.03 + random.nextGaussian() * 0.02;
            double velocityZ = random.nextGaussian() * 0.01;

            // Color variations - gold to white
            Vector3f color = new Vector3f(0.9f + random.nextFloat() * 0.1f, 0.7f + random.nextFloat() * 0.3f, 0.2f + random.nextFloat() * 0.3f);
            color.mul(intensity);

            this.getWorld().addParticle(ParticleTypes.END_ROD, center.x + x, center.y + y, center.z + z, velocityX, velocityY, velocityZ);

            // Add occasional bright flashes
            if (random.nextFloat() < 0.1f * intensity) {
                this.getWorld().addParticle(ParticleTypes.GLOW, center.x + x, center.y + y, center.z + z, 0, 0.05, 0);
            }
        }
    }


    public float getIntensity() {
        return intensity;
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        this.setVelocity(Vec3d.ZERO);
        this.inGround = false;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    protected SoundEvent getHitSound() {
        return null;
    }

    @Override
    protected boolean canHit(Entity entity) {
        return false;
    }
}