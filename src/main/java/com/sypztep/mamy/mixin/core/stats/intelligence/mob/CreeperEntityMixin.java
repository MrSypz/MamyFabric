package com.sypztep.mamy.mixin.core.stats.intelligence.mob;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.sypztep.mamy.client.util.CreeperExplosion;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin {

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void onExplode(CallbackInfo ci) {
        CreeperEntity self = (CreeperEntity)(Object)this;

        if (!self.getWorld().isClient) {
            float f = self.shouldRenderOverlay() ? 2.0F : 1.0F;
//            self.dead = true;

            // Your custom explosion
            CreeperExplosion customExplosion = new CreeperExplosion(
                    self.getWorld(),
                    self,
                    self.getX(), self.getY(), self.getZ(),
                    (float)3 * f + ModEntityComponents.LIVINGLEVEL.get(this).getStatValue(StatTypes.INTELLIGENCE) / 3
            );

            customExplosion.collectBlocksAndDamageEntities();
            customExplosion.affectWorld(true);

//            self.spawnEffectsCloud();
//            self.onRemoval(Entity.RemovalReason.KILLED);
            self.discard();
        }

        // Cancel the original explode() method to avoid vanilla explosion
        ci.cancel();
    }
}
