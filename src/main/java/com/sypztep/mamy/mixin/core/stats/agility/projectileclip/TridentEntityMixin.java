package com.sypztep.mamy.mixin.core.stats.agility.projectileclip;

import com.sypztep.mamy.common.api.MissingAccessor;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentEntity.class)
public class TridentEntityMixin {
    @Inject(method = "onEntityHit",at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/TridentEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V",shift = At.Shift.BEFORE), cancellable = true)
    private void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (entityHitResult.getEntity() instanceof MissingAccessor accessor && accessor.isMissing()) {
            ci.cancel();
        }
    }
}
