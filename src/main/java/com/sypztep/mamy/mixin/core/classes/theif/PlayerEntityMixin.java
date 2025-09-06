package com.sypztep.mamy.mixin.core.classes.theif;

import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.network.server.MultiHitPayloadC2S;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class) @Environment(EnvType.CLIENT)
public class PlayerEntityMixin {
    @Inject(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            shift = At.Shift.AFTER)) // After damage is dealt
    private void injectMultiHit(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        float double_attack = (float) player.getAttributeValue(ModEntityAttributes.DOUBLE_ATTACK_CHANCE);
        if (double_attack <= 0.0f) return;
        if (player.getWorld().isClient()
                && LivingEntityUtil.isHitable((LivingEntity) target,target.getDamageSources().playerAttack(player))
                && LivingEntityUtil.hitCheck(player, (LivingEntity) target)) {
            if (player.getRandom().nextFloat() < double_attack)
                MultiHitPayloadC2S.send(player, target, 2);
        }
    }
}
