package com.sypztep.mamy.mixin.core.stats.intelligence.mob;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.SphereExplosion;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends LivingEntity {

    @Shadow
    private int explosionRadius;

    @Shadow
    protected abstract void spawnEffectsCloud();

    protected CreeperEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void onExplodeAdvanced(CallbackInfo ci) {
        if (ModConfig.newCreeperExplode) {
            CreeperEntity self = (CreeperEntity) (Object) this;

            float f = self.shouldRenderOverlay() ? 2.0F : 1.0F;
            float explosionPower = this.explosionRadius * f;
            explosionPower += ModEntityComponents.LIVINGLEVEL.get(self).getStatValue(StatTypes.INTELLIGENCE) / 3.0f;
            explosionPower = Math.min(explosionPower, 30.0f);

            SphereExplosion explosion = new SphereExplosion(self.getWorld(), self, self.getX(), self.getY(), self.getZ(), explosionPower, false, World.ExplosionSourceType.MOB);

            explosion.collectBlocksAndDamageEntities();
            explosion.affectWorld(true);
            this.spawnEffectsCloud();

            self.discard();
            ci.cancel();
        }
    }
}