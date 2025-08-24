package com.sypztep.mamy.common.system.skill.swordman;


import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.CastableSkill;
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

public class EnergyBreakSkill extends Skill implements CastableSkill {

    public EnergyBreakSkill(Identifier identifier) {
        super(identifier, "Energy Break", "Unleash a devastating fire explosion in a 5x5 area",
                30f, 2f, ModClasses.SWORDMAN, 1, 1, 10, false, Mamy.id("skill/energy_break"));
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return 10; // 2 second cast delay (40 ticks)
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 0;
    }

    @Override
    public boolean shouldLockMovement() {
        return true;
    }

    @Override
    public Identifier getCastAnimation() {
        return Mamy.id("energy_charge");
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        // Base damage + 20% per level
        float attackFlat = (float) player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT);
        float attackMult = (float) player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_MULT);
        float baseMagicDamage = attackFlat * (1 + attackMult);

        float damageMultiplier = 1.0f + (0.2f * skillLevel);

        data.baseDamage = baseMagicDamage * damageMultiplier; // Minimum 10 damage
        data.damageType = DamageType.PHYSICAL; // Fire type
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
        float attackFlat = (float) player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT);
        float attackMult = (float) player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_MULT);
        float baseMagicDamage = attackFlat * (1 + attackMult);
        float damageMultiplier = 1.0f + (0.2f * skillLevel);
        float finalDamage = baseMagicDamage * damageMultiplier;

        // Create 5x5 AOE damage area centered on player
        Vec3d playerPos = player.getPos();
        Box aoeArea = new Box(
                playerPos.x - 2.5, playerPos.y, playerPos.z - 2.5,
                playerPos.x + 2.5, playerPos.y + 3, playerPos.z + 2.5
        );

        // Find and damage all enemies in area
        List<LivingEntity> targets = serverWorld.getEntitiesByClass(
                LivingEntity.class,
                aoeArea,
                entity -> entity != player && entity.isAlive()
        );

        DamageSource damageSource = serverWorld.getDamageSources().create(
                ModDamageTypes.FIRE_DAMAGE, player
        );

        for (LivingEntity target : targets) {
            target.damage(damageSource, finalDamage);
        }

        // Create massive fire explosion particles
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 0; y <= 2; y++) {
                    serverWorld.spawnParticles(ParticleTypes.FLAME,
                            playerPos.x + x + 0.5, playerPos.y + y + 0.5, playerPos.z + z + 0.5,
                            5, 0.3, 0.3, 0.3, 0.1);
                    serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE,
                            playerPos.x + x + 0.5, playerPos.y + y + 0.5, playerPos.z + z + 0.5,
                            3, 0.2, 0.2, 0.2, 0.05);
                }
            }
        }

        // Play explosion sound
        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS,
                1.5f, 0.8f);

        return true;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
}