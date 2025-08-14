package com.sypztep.mamy.mixin.core.passive.dexterity.ricochet;

import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.init.ModSoundEvents;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public class ProjectileEntityMixin {
    @Unique
    private boolean hasRicocheted = false;

    @Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
    private void onSingleTargetRicochet(BlockHitResult blockHitResult, CallbackInfo ci) {
        PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;

        // Check ability and if we haven't ricocheted yet
        if (!(projectile.getOwner() instanceof PlayerEntity player)) return;
        if (!PassiveAbilityManager.isActive(player, ModPassiveAbilities.MARKS_MAN)) return;
        if (hasRicocheted) return; // Only one ricochet allowed

        // Ricochet chance
        if (projectile.getWorld().random.nextFloat() < 0.75f) {
            Vec3d velocity = projectile.getVelocity();
            Vec3d normal = getHitNormal(blockHitResult);
            Vec3d ricochetVelocity;

            // Look for nearby target within 10 blocks
            LivingEntity nearbyTarget = findNearbyTarget(projectile, 10.0);

            if (nearbyTarget != null) {
                ricochetVelocity = calculateTargetRedirect(projectile, nearbyTarget);
                spawnTargetRedirectEffects(projectile, blockHitResult);
            } else {
                ricochetVelocity = calculateImprovedRicochet(velocity, normal);
                spawnNormalRicochetEffects(projectile, blockHitResult);
            }

            // Apply velocity with speed reduction
            double speedMultiplier = nearbyTarget != null ? 0.85 : 0.75; // Less reduction for target redirect
            ricochetVelocity = ricochetVelocity.normalize().multiply(velocity.length() * speedMultiplier);
            projectile.setVelocity(ricochetVelocity);

            // Position projectile safely away from surface
            Vec3d offsetDistance = normal.multiply(0.2 + Math.abs(normal.dotProduct(velocity.normalize())) * 0.1);
            Vec3d newPos = blockHitResult.getPos().add(offsetDistance);
            projectile.setPos(newPos.x, newPos.y, newPos.z);

            hasRicocheted = true;
            ci.cancel();
        }
    }

    @Unique
    private LivingEntity findNearbyTarget(PersistentProjectileEntity projectile, double radius) {
        World world = projectile.getWorld();
        Box searchBox = projectile.getBoundingBox().expand(radius);

        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : world.getOtherEntities(projectile, searchBox)) {
            if (entity instanceof LivingEntity living && isValidTarget(living, projectile.getOwner())) {
                double distance = projectile.squaredDistanceTo(entity);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestTarget = living;
                }
            }
        }

        return nearestTarget;
    }

    @Unique
    private Vec3d calculateTargetRedirect(PersistentProjectileEntity projectile, LivingEntity target) {
        // Calculate target position (aim for center mass)
        Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.2, 0);
        Vec3d projectilePos = projectile.getPos();

        // Calculate direct trajectory to target (like multishot crossbow)
        Vec3d toTarget = targetPos.subtract(projectilePos).normalize();

        // Account for gravity and distance for realistic trajectory
        double distance = projectilePos.distanceTo(targetPos);
        // Calculate trajectory with slight arc compensation
        Vec3d trajectory = toTarget;
        if (distance > 5.0) {
            // Add slight upward arc for longer shots
            double arcHeight = Math.min(distance * 0.015, 0.1);
            trajectory = trajectory.add(0, arcHeight, 0).normalize();
        }

        return trajectory;
    }

    @Unique
    private Vec3d calculateImprovedRicochet(Vec3d velocity, Vec3d normal) {
        // Standard reflection
        Vec3d reflection = calculateRicochetVelocity(velocity, normal);

        // Add slight randomization to prevent infinite bouncing in corners
        double randomOffset = 0.1;
        Vec3d randomizedReflection = reflection.add(
                (Math.random() - 0.5) * randomOffset,
                (Math.random() - 0.5) * randomOffset,
                (Math.random() - 0.5) * randomOffset
        ).normalize();

        // Ensure we're moving away from the surface
        if (randomizedReflection.dotProduct(normal) < 0.1) {
            // If too parallel to surface, add more normal component
            randomizedReflection = randomizedReflection.add(normal.multiply(0.3)).normalize();
        }

        return randomizedReflection;
    }

    @Unique
    private boolean isValidTarget(LivingEntity entity, Entity shooter) {
        if (!(shooter instanceof PlayerEntity player)) return false;

        // Don't target the shooter or their pets
        if (entity == shooter) return false;
        if (entity instanceof TameableEntity tameable && tameable.getOwner() == player) return false;

        return entity instanceof LivingEntity && entity.isAlive();
    }

    @Unique
    private Vec3d getHitNormal(BlockHitResult hitResult) {
        return switch (hitResult.getSide()) {
            case UP -> new Vec3d(0, 1, 0);
            case DOWN -> new Vec3d(0, -1, 0);
            case NORTH -> new Vec3d(0, 0, -1);
            case SOUTH -> new Vec3d(0, 0, 1);
            case EAST -> new Vec3d(1, 0, 0);
            case WEST -> new Vec3d(-1, 0, 0);
        };
    }

    @Unique
    private Vec3d calculateRicochetVelocity(Vec3d incoming, Vec3d normal) {
        double dotProduct = incoming.dotProduct(normal);
        return incoming.subtract(normal.multiply(2 * dotProduct));
    }

    @Unique
    private void spawnTargetRedirectEffects(PersistentProjectileEntity projectile, BlockHitResult hitResult) {
        World world = projectile.getWorld();
        if (world.isClient) return;

        Vec3d pos = hitResult.getPos();
        BlockState hitBlock = world.getBlockState(hitResult.getBlockPos());

        // Block break particles
        ((ServerWorld) world).spawnParticles(
                new BlockStateParticleEffect(ParticleTypes.BLOCK, hitBlock),
                pos.x, pos.y, pos.z,
                15,
                0.3, 0.3, 0.3,
                0.1
        );

        ((ServerWorld) world).spawnParticles(
                ParticleTypes.FLAME,
                pos.x, pos.y, pos.z,
                12,
                0.25, 0.25, 0.25,
                0.05
        );

        // Critical hit particles for "locked on" feeling
        ((ServerWorld) world).spawnParticles(
                ParticleTypes.CRIT,
                pos.x, pos.y, pos.z,
                8,
                0.2, 0.2, 0.2,
                0.15
        );

        world.playSound(null, pos.x, pos.y, pos.z,
                ModSoundEvents.ENTITY_GENERIC_RICOCHET, SoundCategory.PLAYERS,
                1f,
                (float) MathHelper.clamp(0.5f + projectile.getVelocity().length() * 0.1f + (projectile.getRandom().nextFloat() * 0.05f), 0.8f, 1.5f));


        world.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS,
                0.8f, 0.4f); // Target acquired sound
    }

    @Unique
    private void spawnNormalRicochetEffects(PersistentProjectileEntity projectile, BlockHitResult hitResult) {
        World world = projectile.getWorld();
        if (world.isClient) return;

        Vec3d pos = hitResult.getPos();
        BlockState hitBlock = world.getBlockState(hitResult.getBlockPos());

        // Block break particles
        ((ServerWorld) world).spawnParticles(
                new BlockStateParticleEffect(ParticleTypes.BLOCK, hitBlock),
                pos.x, pos.y, pos.z,
                10,
                0.2, 0.2, 0.2,
                0.08
        );

        // Normal ricochet sparks
        ((ServerWorld) world).spawnParticles(
                ParticleTypes.ELECTRIC_SPARK,
                pos.x, pos.y, pos.z,
                6,
                0.15, 0.15, 0.15,
                0.12
        );

        // Standard ricochet sound
        world.playSound(null, pos.x, pos.y, pos.z,
                ModSoundEvents.ENTITY_GENERIC_RICOCHET, SoundCategory.PLAYERS,
                1f,
                (float) MathHelper.clamp(0.5f + projectile.getVelocity().length() * 0.1f + (projectile.getRandom().nextFloat() * 0.05f), 0.8f, 1.5f));

        world.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_ARROW_HIT, SoundCategory.PLAYERS,
                0.3f, 0.8f);
    }
}