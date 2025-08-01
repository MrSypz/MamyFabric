package com.sypztep.mamy.mixin.core.critevasion;

import com.sypztep.mamy.client.util.ParticleHandler;
import com.sypztep.mamy.common.api.MissingAccessor;
import com.sypztep.mamy.common.api.entity.DominatusLivingEntityEvents;
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
import sypztep.tyrannus.client.util.TextParticleProvider;

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

    @ModifyVariable(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V",at = @At("HEAD"),argsOnly = true)
    private float applyPreArmorDamageModification(float amount, DamageSource source) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return amount;
        isCrit = LivingEntityUtil.isCrit(attacker);
        return DominatusLivingEntityEvents.PRE_ARMOR_DAMAGE.invoker().preModifyDamage(target, source, amount, isCrit);
    }

    @Override
    public boolean isMissing() {
        return !isHit;
    }
}
