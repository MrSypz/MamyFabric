package com.sypztep.mamy.common.component.living.ability;

import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModParticles;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

/**
 * INT 99 - Wall Phasing: Phase through walls with smooth movement
 */
public class WallPhasingComponent implements AutoSyncedComponent, CommonTickingComponent {
    private static final int PHASE_COOLDOWN_TICKS = 20; // 5 seconds
    private static final int MAX_PHASE_BLOCKS = 5; // Maximum wall thickness

    private final PlayerEntity player;

    // Phasing state
    private boolean isPhasing = false;
    private Vec3d phasingTargetPos = null;
    private int cooldownTimer = 0;

    public WallPhasingComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        isPhasing = tag.getBoolean("IsPhasing");
        cooldownTimer = tag.getInt("CooldownTimer");

        if (tag.contains("PhasingTargetPos")) {
            NbtCompound targetPos = tag.getCompound("PhasingTargetPos");
            phasingTargetPos = new Vec3d(
                    targetPos.getDouble("x"),
                    targetPos.getDouble("y"),
                    targetPos.getDouble("z")
            );
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean("IsPhasing", isPhasing);
        tag.putInt("CooldownTimer", cooldownTimer);

        if (phasingTargetPos != null) {
            NbtCompound targetPos = new NbtCompound();
            targetPos.putDouble("x", phasingTargetPos.x);
            targetPos.putDouble("y", phasingTargetPos.y);
            targetPos.putDouble("z", phasingTargetPos.z);
            tag.put("PhasingTargetPos", targetPos);
        }
    }

    @Override
    public void tick() {

        // Server-side cooldown
        if (cooldownTimer > 0) {
            cooldownTimer--;
        }
    }

    @Override
    public void clientTick() {
        tick();

        // Add movement particles during phasing
        if (isPhasing && phasingTargetPos != null) {
            addPhasingTrailParticles(player);
        }
    }

    // =============================================================================
    // Public API
    // =============================================================================

    public boolean tryPhase() {
        if (player.getWorld().isClient()) return false;

        if (!canPhase()) {
            return false;
        }

        if (isOnCooldown()) {
            return false;
        }

        if (isPhasing) {
            return false;
        }

        Direction wallDirection = findNearestWall();
        if (wallDirection == null) {
            return false;
        }

        Vec3d exitPos = findPhaseExit(wallDirection);
        if (exitPos == null) {
            startCooldown();
            return false;
        }

        startPhasing(exitPos);
        return true;
    }

    public void completePhasingMovement() {
        if (!isPhasing) return;

        isPhasing = false;
        phasingTargetPos = null;

        if (!player.getWorld().isClient()) {
            addPhasingCompletionEffects();
            startCooldown();
            sync();
        }

    }

    // =============================================================================
    // Wall Detection
    // =============================================================================

    private Direction findNearestWall() {
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        for (Direction direction : directions) {
            if (isWallInDirection(direction)) {
                return direction;
            }
        }

        return null;
    }

    private boolean isWallInDirection(Direction direction) {
        World world = player.getWorld();
        Vec3d start = player.getEyePos();
        Vec3d directionVec = Vec3d.of(direction.getVector()).normalize();
        Vec3d end = start.add(directionVec.multiply(1.0));

        RaycastContext context = new RaycastContext(
                start, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player
        );

        BlockHitResult hitResult = world.raycast(context);
        return hitResult.getType() == HitResult.Type.BLOCK;
    }

    // =============================================================================
    // Exit Finding
    // =============================================================================

    private Vec3d findPhaseExit(Direction direction) {
        World world = player.getWorld();
        Vec3d start = player.getEyePos();
        Vec3d phaseDirection = Vec3d.of(direction.getVector()).normalize();

        double stepSize = 1.0 / 16.0;
        double totalDistance = 0;
        Vec3d currentPos = start;

        while (totalDistance < MAX_PHASE_BLOCKS) {
            Vec3d nextPos = currentPos.add(phaseDirection.multiply(stepSize));
            totalDistance += stepSize;

            RaycastContext context = new RaycastContext(
                    currentPos, nextPos,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player
            );

            BlockHitResult hitResult = world.raycast(context);

            if (hitResult.getType() == HitResult.Type.MISS) {
                Vec3d playerPos = nextPos.subtract(0, player.getEyeHeight(player.getPose()), 0);

                if (canPlayerFitAt(playerPos)) {
                    return playerPos;
                }
            }

            currentPos = nextPos;
        }

        return null;
    }

    private boolean canPlayerFitAt(Vec3d pos) {
        World world = player.getWorld();
        Box playerBox = player.getBoundingBox().offset(pos.subtract(player.getPos()));
        return world.isSpaceEmpty(player, playerBox) && !world.containsFluid(playerBox);
    }

    // =============================================================================
    // Phasing State Management
    // =============================================================================

    private void startPhasing(Vec3d targetPos) {
        isPhasing = true;
        phasingTargetPos = targetPos;

        addPhasingStartEffects();
        sync();
    }

    private void startCooldown() {
        cooldownTimer = PHASE_COOLDOWN_TICKS;
    }

    // =============================================================================
    // Effects & Particles
    // =============================================================================

    private void addPhasingStartEffects() {
        World world = player.getWorld();
        Vec3d pos = player.getPos();

        // Sound effects
        world.playSound(null, BlockPos.ofFloored(pos),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                SoundCategory.PLAYERS, 0.75f, 1.0f);

        // Start particles
        for (int i = 0; i < 8; i++) {
            world.addParticle(ParticleTypes.REVERSE_PORTAL,
                    pos.x + (player.getRandom().nextDouble() - 0.5) * player.getWidth(),
                    pos.y + (player.getRandom().nextDouble() - 0.5) * player.getHeight(),
                    pos.z + (player.getRandom().nextDouble() - 0.5) * player.getWidth(),
                    0, 0, 0);
        }

        // Game event
        world.emitGameEvent(GameEvent.TELEPORT, pos, GameEvent.Emitter.of(player));
    }

    private void addPhasingCompletionEffects() {
        World world = player.getWorld();
        Vec3d pos = player.getPos();

        // Completion sound
        world.playSound(null, BlockPos.ofFloored(pos),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                SoundCategory.PLAYERS, 0.75f, 1.2f);

        // End particles
        for (int i = 0; i < 32; i++) {
            world.addParticle(ParticleTypes.REVERSE_PORTAL,
                    pos.x + (player.getRandom().nextDouble() - 0.5) * player.getWidth(),
                    pos.y + (player.getRandom().nextDouble() - 0.5) * player.getHeight(),
                    pos.z + (player.getRandom().nextDouble() - 0.5) * player.getWidth(),
                    0, 0, 0);
        }
    }

    /**
     * Alternative: Add trail particles during movement
     */
    public static void addPhasingTrailParticles(Entity entity) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.gameRenderer.getCamera().isThirdPerson() || entity != client.cameraEntity) {
            // Create a ghostly trail effect
            Vec3d velocity = entity.getVelocity();

            for (int i = 0; i < 3; i++) {
                double offsetX = -velocity.x * i * 0.1;
                double offsetY = -velocity.y * i * 0.1;
                double offsetZ = -velocity.z * i * 0.1;

                entity.getWorld().addParticle(ParticleTypes.SOUL,
                        entity.getX() + offsetX + (entity.getRandom().nextDouble() - 0.5) * 0.3,
                        entity.getY() + offsetY + entity.getRandom().nextDouble() * 1.8,
                        entity.getZ() + offsetZ + (entity.getRandom().nextDouble() - 0.5) * 0.3,
                        0, 0, 0);
            }
        }
    }



    // =============================================================================
    // Getters
    // =============================================================================

    public boolean canPhase() {
        return PassiveAbilityManager.isActive(player, ModPassiveAbilities.ARCHMAGE_POWER);
    }

    public boolean isPhasing() {
        return isPhasing;
    }

    public boolean isOnCooldown() {
        return cooldownTimer > 0;
    }


    public Vec3d getPhasingTargetPos() {
        return phasingTargetPos;
    }

    private void sync() {
        ModEntityComponents.WALLPHASING.sync(player);
    }
}