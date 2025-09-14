package com.sypztep.mamy.mixin.core.critevasion;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.sypztep.mamy.client.util.TextParticleProvider;
import com.sypztep.mamy.client.util.ParticleHandler;
import com.sypztep.mamy.common.api.MissingAccessor;
import com.sypztep.mamy.common.system.damage.DamageUtil;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import com.sypztep.mamy.common.init.ModCustomParticles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements MissingAccessor {
    @Unique
    protected LivingEntity target = (LivingEntity) (Object) this;

    protected boolean isHit;

    protected boolean isCrit;
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSleeping()Z"), cancellable = true)
    private void allowDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return;
        if (!LivingEntityUtil.isHitable(target, source)) return;
        isHit = LivingEntityUtil.hitCheck(attacker, target);
        if (!isHit) {
            TextParticleProvider missParticle = LivingEntityUtil.isPlayer(attacker) ? ModCustomParticles.MISSING : ModCustomParticles.MISSING_MONSTER;
            ParticleHandler.sendToAll(target, attacker, missParticle);
            cir.setReturnValue(false);
        }
    }

    @ModifyVariable(method = "applyDamage", at = @At("HEAD"), argsOnly = true)
    private float applyPreArmorDamageModification(float amount, DamageSource source) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return amount;
        isCrit = LivingEntityUtil.isCrit(attacker);
        return DamageUtil.calculateDamage(target, attacker, source, amount, isCrit);
    }

    @Inject(method = "applyArmorToDamage", at = @At("HEAD"), cancellable = true)
    private void replaceArmorCalculation(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(DamageUtil.getDamageAfterArmor((LivingEntity) (Object) this, source, amount));
    }

    // Inject code after get Enchantment Protection
    @ModifyReturnValue(method = "modifyAppliedDamage", at = @At("RETURN"))
    private float modifyDamageReturn(float original, DamageSource source) {
        return DamageUtil.damageResistanceModifier((LivingEntity) (Object) this, original, source);
    }

    @Override
    public boolean isMissing() {
        return !isHit;
    }
}
