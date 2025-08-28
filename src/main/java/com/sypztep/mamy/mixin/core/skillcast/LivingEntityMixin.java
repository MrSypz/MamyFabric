package com.sypztep.mamy.mixin.core.skillcast;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//@Environment(EnvType.CLIENT)
//@Mixin(LivingEntity.class)
//public class LivingEntityMixin {
//
//    @Inject(method = "damage", at = @At("HEAD"))
//    private void interruptCastOnDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
//        if ((Object) this == MinecraftClient.getInstance().player) {
//            SkillCastingManager.getInstance().interruptCast();
//        }
//    }
//}