package com.sypztep.mamy.mixin.vanilla.rebalancedamage.enviroment;

import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Entity.class)
public class EntityMixin {
    @Unique
    private Entity entity = (Entity) (Object) this;
    @ModifyArg(method = "setOnFireFromLava", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    private float modifyLavaDamage(float amount) {
        return LivingEntityUtil.modifyEnviromentDamage(entity,amount, 0.2f);
    }
    @ModifyArg(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    private float modifyFireDamage(float amount) {
        return LivingEntityUtil.modifyEnviromentDamage(entity,amount, 0.01f);
    }
    @ModifyArg(method = "onStruckByLightning", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    private float modifyLightningDamage(float amount) {
        return LivingEntityUtil.modifyEnviromentDamage(entity,amount, 0.25f);
    }
}