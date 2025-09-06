package com.sypztep.mamy.common.entity.skill;

import com.sypztep.mamy.common.init.ModEntityTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class HealingLightEntity extends PersistentProjectileEntity {
    private static final int MAX_LIFETIME = 60; // 3 seconds at 20 TPS
    private static final float CYLINDER_RADIUS = 2.5f;
    private static final float CYLINDER_HEIGHT = 4.0f;
    private static final int PARTICLES_PER_TICK = 4;

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

            // If too far away, teleport instead of lerp (prevents getting stuck)
            if (distance > 5) this.setPos(prevX, prevY, prevZ);
            else {
                double newX = MathHelper.lerp(1.0f, currentX, prevX);
                double newY = MathHelper.lerp(1.0f, currentY, prevY);
                double newZ = MathHelper.lerp(1.0f, currentZ, prevZ);

                this.setPos(newX, newY, newZ);
            }
        }

        if (this.getWorld().isClient) {
            renderHealingAura();
        }
    }

    private void renderHealingAura() {
        Vec3d center = this.getPos();

        // Render cylinder outline with spark particles
        renderCylinderOutline(center);

        // Render floating spark particles inside cylinder
        renderInternalSparks(center);

        // Render ground circle effect
        renderGroundCircle(center);
    }

    private void renderCylinderOutline(Vec3d center) {
        int circumferencePoints = 32;
        float angleStep = (float) (2 * Math.PI / circumferencePoints);

        // Top and bottom circles
        for (int ring = 0; ring <= 1; ring++) {
            float y = (ring == 0) ? (float) (-CYLINDER_HEIGHT / 2) : (float) (CYLINDER_HEIGHT / 2);

            for (int i = 0; i < circumferencePoints; i++) {
                if (random.nextFloat() > 0.3f * intensity) continue; // Sparse outline

                float angle = i * angleStep;
                float x = CYLINDER_RADIUS * (float) Math.cos(angle);
                float z = CYLINDER_RADIUS * (float) Math.sin(angle);

                // Add Gaussian noise for organic look
                x += (float) (random.nextGaussian() * 0.1f);
                z += (float) (random.nextGaussian() * 0.1f);
                y += (float) (random.nextGaussian() * 0.05f);

                Vector3f color = new Vector3f(1.0f, 0.8f + random.nextFloat() * 0.2f, 0.3f);
                color.mul(intensity);

                this.getWorld().addParticle(new DustParticleEffect(color, 0.8f * intensity), center.x + x, center.y + y, center.z + z, 0, 0.02, 0);
            }
        }

        // Vertical lines connecting top and bottom
        int verticalLines = 8;
        for (int i = 0; i < verticalLines; i++) {
            float angle = i * (float) (2 * Math.PI / verticalLines);
            float x = CYLINDER_RADIUS * (float) Math.cos(angle);
            float z = CYLINDER_RADIUS * (float) Math.sin(angle);

            int heightPoints = 12;
            for (int h = 0; h < heightPoints; h++) {
                if (random.nextFloat() > 0.4f * intensity) continue;

                float y = -CYLINDER_HEIGHT / 2 + (h / (float) heightPoints) * CYLINDER_HEIGHT;
                y += (float) (random.nextGaussian() * 0.08f);

                Vector3f color = new Vector3f(0.9f, 0.9f + random.nextFloat() * 0.1f, 0.5f);
                color.mul(intensity * 0.8f);

                this.getWorld().addParticle(new DustParticleEffect(color, 0.6f * intensity), center.x + x + random.nextGaussian() * 0.08, center.y + y, center.z + z + random.nextGaussian() * 0.08, 0, 0, 0);
            }
        }
    }

    private void renderInternalSparks(Vec3d center) {
        for (int i = 0; i < PARTICLES_PER_TICK * intensity; i++) {
            float angle = random.nextFloat() * (float) (2 * Math.PI);
            float radius = (float) Math.abs(random.nextGaussian()) * CYLINDER_RADIUS * 0.4f;
            radius = Math.min(radius, CYLINDER_RADIUS * 0.9f); // Clamp to stay inside

            float x = radius * (float) Math.cos(angle);
            float z = radius * (float) Math.sin(angle);
            float y = (float) (random.nextGaussian() * CYLINDER_HEIGHT * 0.3f);

            // Clamp Y to cylinder bounds
            y = Math.max(-CYLINDER_HEIGHT / 2, Math.min(CYLINDER_HEIGHT / 2, y));

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

    private void renderGroundCircle(Vec3d center) {
        if (lifetime % 3 != 0) return; // Don't spawn every tick

        BlockPos groundPos = BlockPos.ofFloored(center.x, center.y - CYLINDER_HEIGHT / 2, center.z);

        int circlePoints = 24;
        float angleStep = (float) (2 * Math.PI / circlePoints);

        for (int i = 0; i < circlePoints; i++) {
            if (random.nextFloat() > 0.4f * intensity) continue;

            float angle = i * angleStep;
            float radius = CYLINDER_RADIUS * (0.8f + random.nextFloat() * 0.4f);

            float x = radius * (float) Math.cos(angle);
            float z = radius * (float) Math.sin(angle);

            // Add Gaussian noise for organic edge
            x += (float) (random.nextGaussian() * 0.2f);
            z += (float) (random.nextGaussian() * 0.2f);

            Vector3f color = new Vector3f(1.0f, 0.9f, 0.4f);
            color.mul(intensity * 0.7f);

            this.getWorld().addParticle(new DustParticleEffect(color, 1.2f * intensity), groundPos.getX() + 0.5 + x, groundPos.getY() + 0.1, groundPos.getZ() + 0.5 + z, 0, 0, 0);
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