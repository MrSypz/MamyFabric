package com.sypztep.mamy.common.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.sypztep.mamy.client.screen.CameraShakeManager;
import com.sypztep.mamy.common.init.ModSoundEvents;
import com.sypztep.mamy.common.network.client.CameraShakePayloadS2C;
import com.sypztep.mamy.common.network.client.ShockwavePayloadS2C;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SphereExplosion {
    private static final ExplosionBehavior DEFAULT_BEHAVIOR = new ExplosionBehavior();
    private static final int MAX_DROPS_PER_COMBINED_STACK = 16;

    private final boolean createFire;
    private final Explosion.DestructionType destructionType;
    private final Random random = Random.create();
    private final World world;
    private final double x;
    private final double y;
    private final double z;
    @Nullable
    private final Entity entity;
    private final float power;
    private final DamageSource damageSource;
    private final ExplosionBehavior behavior;
    private final ParticleEffect particle;
    private final ParticleEffect emitterParticle;
    private final RegistryEntry<SoundEvent> soundEvent;
    private final ObjectArrayList<BlockPos> affectedBlocks = new ObjectArrayList<>();
    private final Map<PlayerEntity, Vec3d> affectedPlayers = Maps.newHashMap();

    // Proxy explosion for behavior calls
    private final Explosion proxyExplosion;

    public static DamageSource createDamageSource(World world, @Nullable Entity source) {
        return world.getDamageSources().explosion(source, getCausingEntity(source));
    }

    public SphereExplosion(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType) {
        this.world = world;
        this.entity = entity;
        this.power = power;
        this.x = x;
        this.y = y;
        this.z = z;
        this.createFire = createFire;

        // Handle ExplosionSourceType like vanilla
        this.destructionType = switch (explosionSourceType) {
            case NONE -> Explosion.DestructionType.KEEP;
            case BLOCK -> world.getDestructionType(GameRules.BLOCK_EXPLOSION_DROP_DECAY);
            case MOB ->
                    world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) ? world.getDestructionType(GameRules.MOB_EXPLOSION_DROP_DECAY) : Explosion.DestructionType.KEEP;
            case TNT -> world.getDestructionType(GameRules.TNT_EXPLOSION_DROP_DECAY);
            case TRIGGER -> Explosion.DestructionType.TRIGGER_BLOCK;
        };

        this.damageSource = createDamageSource(world, entity);
        this.behavior = entity == null ? DEFAULT_BEHAVIOR : new EntityExplosionBehavior(entity);
        this.particle = ParticleTypes.EXPLOSION;
        this.emitterParticle = ParticleTypes.EXPLOSION_EMITTER;
        this.soundEvent = SoundEvents.ENTITY_GENERIC_EXPLODE;

        this.proxyExplosion = new Explosion(world, entity, this.damageSource, this.behavior, x, y, z, power, createFire, this.destructionType, this.particle, this.emitterParticle, this.soundEvent);
    }


    private void applyCameraShake(double x, double y, double z, float power) {
        if (!this.world.isClient) {
            double maxShockwaveDistance = calculateShockwaveDistance(power);
            double amplitude = 2.0 + (0.2 * power * 50);
            double time = 5.0 + (0.05 * power * 10);

            for (ServerPlayerEntity player : PlayerLookup.around((ServerWorld) this.world, new Vec3d(x, y, z), maxShockwaveDistance)) {
                ShockwavePayloadS2C.send(player, x, y, z, time, maxShockwaveDistance, amplitude);
            }
        }
    }

    private double calculateShockwaveDistance(float power) {
        return 20.0 + (power * 4) + Math.log(power + 1) * 5.0;
    }

    public void collectBlocksAndDamageEntities() {
        this.world.emitGameEvent(this.entity, GameEvent.EXPLODE, new Vec3d(this.x, this.y, this.z));

        Set<BlockPos> set = Sets.newHashSet();
        BlockPos centerPos = BlockPos.ofFloored(this.x, this.y, this.z);

        // Sphere-based block collection (replacing vanilla raycast)
        int radius = (int) Math.ceil(this.power);
        double powerSquared = this.power * this.power;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int x = -radius; x <= radius; x++) {
            int xSquared = x * x;
            for (int y = -radius; y <= radius; y++) {
                int xySquared = xSquared + y * y;
                if (xySquared <= powerSquared) {
                    for (int z = -radius; z <= radius; z++) {
                        double distanceSquared = xySquared + z * z;
                        if (distanceSquared <= powerSquared) {
                            mutable.set(centerPos.getX() + x, centerPos.getY() + y, centerPos.getZ() + z);

                            if (this.world.isInBuildLimit(mutable)) {
                                BlockState blockState = this.world.getBlockState(mutable);
                                FluidState fluidState = this.world.getFluidState(mutable);

                                if (!blockState.isAir()) {
                                    // Use vanilla behavior system
                                    Optional<Float> optional = this.behavior.getBlastResistance(this.proxyExplosion, this.world, mutable, blockState, fluidState);
                                    if (optional.isPresent()) {
                                        double distance = Math.sqrt(distanceSquared);
                                        float effectivePower = this.power * (0.7F + this.world.random.nextFloat() * 0.6F) - (float) (distance * 0.3);
                                        effectivePower -= (optional.get() + 0.3F) * 0.3F;

                                        if (effectivePower > 0.0F && this.behavior.canDestroyBlock(this.proxyExplosion, this.world, mutable, blockState, effectivePower)) {
                                            set.add(mutable.toImmutable());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        this.affectedBlocks.addAll(set);

        // Entity damage (exact vanilla copy)
        float q = this.power * 2.0F;
        int k = MathHelper.floor(this.x - q - 1.0);
        int lx = MathHelper.floor(this.x + q + 1.0);
        int r = MathHelper.floor(this.y - q - 1.0);
        int s = MathHelper.floor(this.y + q + 1.0);
        int t = MathHelper.floor(this.z - q - 1.0);
        int u = MathHelper.floor(this.z + q + 1.0);
        List<Entity> list = this.world.getOtherEntities(this.entity, new Box(k, r, t, lx, s, u));
        Vec3d vec3d = new Vec3d(this.x, this.y, this.z);

        for (Entity entity : list) {
            if (!entity.isImmuneToExplosion(this.proxyExplosion)) {
                double v = Math.sqrt(entity.squaredDistanceTo(vec3d)) / q;
                if (v <= 1.0) {
                    double w = entity.getX() - this.x;
                    double x = (entity instanceof TntEntity ? entity.getY() : entity.getEyeY()) - this.y;
                    double y = entity.getZ() - this.z;
                    double z = Math.sqrt(w * w + x * x + y * y);
                    if (z != 0.0) {
                        w /= z;
                        x /= z;
                        y /= z;

                        if (this.behavior.shouldDamage(this.proxyExplosion, entity)) {
                            entity.damage(this.damageSource, this.behavior.calculateDamage(this.proxyExplosion, entity));
                        }

                        double aa = (1.0 - v) * Explosion.getExposure(vec3d, entity) * this.behavior.getKnockbackModifier(entity);
                        double ab;
                        if (entity instanceof LivingEntity livingEntity) {
                            ab = aa * (1.0 - livingEntity.getAttributeValue(EntityAttributes.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE));
                        } else {
                            ab = aa;
                        }

                        w *= ab;
                        x *= ab;
                        y *= ab;
                        Vec3d vec3d2 = new Vec3d(w, x, y);
                        entity.setVelocity(entity.getVelocity().add(vec3d2));
                        if (entity instanceof PlayerEntity playerEntity && !playerEntity.isSpectator() && (!playerEntity.isCreative() || !playerEntity.getAbilities().flying)) {
                            this.affectedPlayers.put(playerEntity, vec3d2);
                        }

                        entity.onExplodedBy(this.entity);
                    }
                }
            }
        }
    }

    public void affectWorld(boolean particles) {
        if (this.world.isClient) {
            this.world.playSound(this.x, this.y, this.z, this.soundEvent.value(), SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        boolean bl = this.shouldDestroy();
        if (particles && this.world.isClient) {
            ParticleEffect particleEffect;
            if (!(this.power < 2.0F) && bl) {
                particleEffect = this.emitterParticle;
            } else {
                particleEffect = this.particle;
            }
            this.world.addParticle(particleEffect, this.x, this.y, this.z, 1.0, 0.0, 0.0);
        }

        if (bl) {
            this.world.getProfiler().push("explosion_blocks");
            List<Pair<ItemStack, BlockPos>> list = new ArrayList<>();
            Util.shuffle(this.affectedBlocks, this.world.random);

            // Use vanilla block destruction
            for (BlockPos blockPos : this.affectedBlocks) {
                this.world.getBlockState(blockPos).onExploded(this.world, blockPos, this.proxyExplosion, (stack, pos) -> tryMergeStack(list, stack, pos));
            }

            for (Pair<ItemStack, BlockPos> pair : list) {
                Block.dropStack(this.world, pair.getSecond(), pair.getFirst());
            }

            this.world.getProfiler().pop();
        }

        if (this.createFire) {
            for (BlockPos blockPos2 : this.affectedBlocks) {
                if (this.random.nextInt(3) == 0 && this.world.getBlockState(blockPos2).isAir() && this.world.getBlockState(blockPos2.down()).isOpaqueFullCube(this.world, blockPos2.down())) {
                    this.world.setBlockState(blockPos2, AbstractFireBlock.getState(this.world, blockPos2));
                }
            }
        }
        applyCameraShake(x, y, z, power);
    }

    private static void tryMergeStack(List<Pair<ItemStack, BlockPos>> stacks, ItemStack stack, BlockPos pos) {
        for (int i = 0; i < stacks.size(); i++) {
            Pair<ItemStack, BlockPos> pair = stacks.get(i);
            ItemStack itemStack = pair.getFirst();
            if (ItemEntity.canMerge(itemStack, stack)) {
                stacks.set(i, Pair.of(ItemEntity.merge(itemStack, stack, MAX_DROPS_PER_COMBINED_STACK), pair.getSecond()));
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
        stacks.add(Pair.of(stack, pos));
    }

    public boolean shouldDestroy() {
        return this.destructionType != Explosion.DestructionType.KEEP;
    }

    @Nullable
    private static LivingEntity getCausingEntity(@Nullable Entity from) {
        return switch (from) {
            case null -> null;
            case TntEntity tntEntity -> tntEntity.getOwner();
            case LivingEntity livingEntity -> livingEntity;
            default ->
                    from instanceof ProjectileEntity projectileEntity && projectileEntity.getOwner() instanceof LivingEntity ownerProjectile ? ownerProjectile : null;
        };
    }

    // Getters
    public float getPower() {
        return this.power;
    }

    public Vec3d getPosition() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public Map<PlayerEntity, Vec3d> getAffectedPlayers() {
        return this.affectedPlayers;
    }

    public List<BlockPos> getAffectedBlocks() {
        return this.affectedBlocks;
    }

    @Nullable
    public Entity getEntity() {
        return this.entity;
    }
}