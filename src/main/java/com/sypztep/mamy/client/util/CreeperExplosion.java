package com.sypztep.mamy.client.util;

import com.google.common.collect.Maps;
import com.sypztep.mamy.common.init.ModDamageTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class CreeperExplosion {
    private static final ExplosionBehavior DEFAULT_BEHAVIOR = new ExplosionBehavior();
    private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
    private final Random random;
    private final World world;
    private final double x;
    private final double y;
    private final double z;
    private final @Nullable Entity entity;
    private final float power;
    private final DamageSource damageSource;
    private final ExplosionBehavior behavior;
    private final ObjectArrayList<BlockPos> affectedBlocks;
    private final Map<PlayerEntity, Vec3d> affectedPlayers;

    public CreeperExplosion(World world, @Nullable Entity entity, double x, double y, double z, float power, List<BlockPos> affectedBlocks) {
        this(world, entity, x, y, z, power);
        this.affectedBlocks.addAll(affectedBlocks);
    }

    public CreeperExplosion(World world, @Nullable Entity entity, double x, double y, double z, float power) {
        this(world, entity, (ExplosionBehavior)null, x, y, z, power);
    }

    public CreeperExplosion(World world, @Nullable Entity entity, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power) {
        this.random = Random.create();
        this.affectedBlocks = new ObjectArrayList();
        this.affectedPlayers = Maps.newHashMap();
        this.world = world;
        this.entity = entity;
        this.power = power;
        this.x = x;
        this.y = y;
        this.z = z;
        this.damageSource = ModDamageTypes.create(world, ModDamageTypes.BASHING_BLOW);
        this.behavior = behavior == null ? this.chooseBehavior(entity) : behavior;
    }

    private ExplosionBehavior chooseBehavior(@Nullable Entity entity) {
        return (ExplosionBehavior)(entity == null ? DEFAULT_BEHAVIOR : new EntityExplosionBehavior(entity));
    }

    public void collectBlocksAndDamageEntities() {
        BlockPos expPos = new BlockPos((int) this.x, (int) this.y, (int) this.z);
        if (!this.world.isClient()) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for(float x = -this.power; x <= this.power; ++x) {
                for(float y = -this.power; y <= this.power; ++y) {
                    for(float z = -this.power; z <= this.power; ++z) {
                        if (mutable.set(this.x + (double)x, this.y + (double)y, this.z + (double)z).isWithinDistance(expPos, (double)this.power)) {
                            BlockState blockState = this.world.getBlockState(mutable);
                            if (!blockState.isAir()) {
                                FallingBlockEntity fallingBlockEntity = FallingBlockEntity.spawnFromBlock(this.world, mutable, blockState);
                                Vec3d vel = (new Vec3d((double)mutable.getX(), (double)mutable.getY(), (double)mutable.getZ())).subtract(new Vec3d(this.x, this.y, this.z)).normalize().multiply((double)this.power);
                                fallingBlockEntity.dropItem = false;
                                fallingBlockEntity.setVelocity(new Vec3d(vel.getX() * (double)this.random.nextFloat(), Math.abs(vel.getY()) * (double)(1.0F + this.random.nextFloat()), vel.getZ() * (double)this.random.nextFloat()));
                                fallingBlockEntity.velocityModified = true;
                                fallingBlockEntity.velocityDirty = true;
                                this.world.setBlockState(mutable, Blocks.AIR.getDefaultState(), 3);
                            }
                        }
                    }
                }
            }

            for(LivingEntity entitiesByClass : this.world.getEntitiesByClass(LivingEntity.class, new Box(this.x - (double)this.power, this.y - (double)this.power, this.z - (double)this.power, this.x + (double)this.power, this.y + (double)this.power, this.z + (double)this.power), LivingEntity::isAlive)) {
                if (entitiesByClass.getBlockPos().isWithinDistance(expPos, (double)(this.power * 1.5F))) {
//                    if (entitiesByClass instanceof EnderDragonPart) {
//                        EnderDragonPart enderDragonEntityPart = (EnderDragonPart)entitiesByClass;
//                        enderDragonEntityPart.getOwner().method_5643(DamageSource.field_5869, 9999.0F);
//                    }
//
//                    entitiesByClass.method_5643(DamageSource.field_5869, 9999.0F);
                }
            }
        }

    }

    public void affectWorld(boolean particles) {
//        if (this.world.isClient) {
//            this.world.playSoundClient(this.x, this.y, this.z, class_3417.field_15152, class_3419.field_15250, 4.0F, (1.0F + (this.world.field_9229.nextFloat() - this.world.field_9229.nextFloat()) * 0.2F) * 0.7F, false);
//        }
//
//        if (!(this.power < 2.0F)) {
//            this.world.addParticleClient(class_2398.field_11221, this.x, this.y, this.z, (double)1.0F, (double)0.0F, (double)0.0F);
//        } else {
//            this.world.addParticleClient(class_2398.field_11236, this.x, this.y, this.z, (double)1.0F, (double)0.0F, (double)0.0F);
//        }

        Util.shuffle(this.affectedBlocks, this.world.random);

        for(ObjectListIterator var2 = this.affectedBlocks.iterator(); var2.hasNext(); this.world.getProfiler().pop()) {
            BlockPos blockPos = (BlockPos)var2.next();
            BlockState blockState = this.world.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (!blockState.isAir()) {
            }
        }

    }
    private static LivingEntity getCausingEntity(@Nullable Entity from) {
        if (from == null) {
            return null;
        } else if (from instanceof TntEntity) {
            TntEntity tntEntity = (TntEntity)from;
            return tntEntity.getOwner();
        } else if (from instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)from;
            return livingEntity;
        } else {
            if (from instanceof ProjectileEntity) {
                ProjectileEntity projectileEntity = (ProjectileEntity)from;
                Entity entity = projectileEntity.getOwner();
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity2 = (LivingEntity)entity;
                    return livingEntity2;
                }
            }

            return null;
        }
    }
    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    public @Nullable Entity getEntity() {
        return this.entity;
    }
}
