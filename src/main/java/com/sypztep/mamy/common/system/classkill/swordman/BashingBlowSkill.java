package com.sypztep.mamy.common.system.classkill.swordman;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModParticles;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;
import net.minecraft.particle.ParticleTypes;

import java.util.List;

public class BashingBlowSkill extends Skill {

    public BashingBlowSkill(Identifier identifier) {
        super(identifier, "Bashing Blow", "Dash 5 blocks forward and deal massive damage to the first enemy hit", 8f, 0.5f, ModClasses.SWORDMAN, 1, 1, 10, false, Mamy.id("skill/bashing_blow"));
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return skillLevel <= 5 ? 8f : 16f;
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        float meleeFlat = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float meleeMult = (float) player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_MULT);
        float baseMeleeDamage = meleeFlat * (1 + meleeMult);
        float damageMultiplier = 1.0f + (0.3f * skillLevel);

        data.baseDamage = baseMeleeDamage * damageMultiplier;
        data.damageType = DamageType.ELEMENT;
        data.maxHits = 1;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        // Calculate damage
        float meleeFlat = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float meleeMult = (float) player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_MULT);
        float baseMeleeDamage = meleeFlat * (1 + meleeMult);
        float damageMultiplier = 1.0f + (0.3f * skillLevel);
        float finalDamage = baseMeleeDamage * damageMultiplier;

        // Get player's facing direction for targeting
        Vec3d direction = player.getRotationVector().normalize();
        Vec3d startPos = player.getPos();
        Vec3d endPos = startPos.add(direction.multiply(5.0)); // 5 block range

        // Create damage area along the dash path
        Box damageArea = new Box(Math.min(startPos.x, endPos.x) - 1, Math.min(startPos.y, endPos.y), Math.min(startPos.z, endPos.z) - 1, Math.max(startPos.x, endPos.x) + 1, Math.max(startPos.y, endPos.y) + 2, Math.max(startPos.z, endPos.z) + 1);

        // Find and damage entities in the path
        List<LivingEntity> targets = serverWorld.getEntitiesByClass(LivingEntity.class, damageArea, entity -> entity != player && entity.isAlive());

        // Damage the first target found
        if (!targets.isEmpty()) {
            LivingEntity target = targets.getFirst();
            DamageSource damageSource = serverWorld.getDamageSources().create(ModDamageTypes.BASHING_BLOW, player);
            target.damage(damageSource, finalDamage);

            serverWorld.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
            serverWorld.spawnParticles(ModParticles.ARROW_IMPACT, target.getX(), target.getBlockY() + 0.1f, target.getZ(), 1, 0, 0, 0, 0.0);
        }

        // Create vertical slash effect from above
        Vec3d centerPos = startPos.add(direction.multiply(2.5)); // Middle of dash path

        // Create vertical slash particles falling from above
        for (int i = 0; i < 15; i++) {
            double heightOffset = 3.0 - (i * 0.2); // Start 3 blocks above, work down
            double x = centerPos.x + (Math.random() - 0.5) * 2.0; // Spread horizontally
            double z = centerPos.z + (Math.random() - 0.5) * 2.0;

            serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, x, centerPos.y + heightOffset, z, 1, 0.1, 0.0, 0.1, 0.0);
            serverWorld.spawnParticles(ParticleTypes.CRIT, x, centerPos.y + heightOffset, z, 2, 0.3, 0.1, 0.3, 0.05);
        }
        serverWorld.spawnParticles(ParticleTypes.EXPLOSION, centerPos.x, centerPos.y, centerPos.z, 5, 1.0, 0.1, 1.0, 0.1);
        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, 1.2f);

        return true;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
}