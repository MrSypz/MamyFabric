package com.sypztep.mamy.common.system.classkill.acolyte;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.entity.skill.BloodLustEntity;
import com.sypztep.mamy.common.entity.entity.skill.HealingLightEntity;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.config.SkillConfig;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

public class HealSkill extends Skill implements CastableSkill {

    public HealSkill(Identifier identifier) {
        super(identifier, "Heal", "Restore HP to a single target. Damages undead with holy power.",
                13f, 0f, ModClasses.ACOLYTE, 1, 1, 10, false, Mamy.id("skill/heal"));
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return 13f + (skillLevel - 1) * 3f;
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return 20;
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 5;
    }

    @Override
    public boolean shouldLockMovement() {
        return true;
    }

    @Override
    public Identifier getCastAnimation() {
        return Mamy.id("pray");
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = calculateHealingAmount(player, skillLevel);
        data.damageType = DamageType.HEAL;
        data.maxHits = 1;

        return data;
    }

    private float calculateHealingAmount(PlayerEntity player, int skillLevel) {
        // HP Restored = floor((BLv + INT)/5) × SkillLv × 3 × (1 + RecoveryEffect% + H.PLUS%) + MATK
        int baseLevel = ModEntityComponents.LIVINGLEVEL.get(player).getLevel();
        int intelligence = ModEntityComponents.LIVINGLEVEL.get(player).getStatValue(StatTypes.INTELLIGENCE);
        float magicAttack = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT);
        float healingEffectiveness = (float) player.getAttributeValue(ModEntityAttributes.HEAL_EFFECTIVE);

        float baseHealing = (float) Math.floor((baseLevel + intelligence) / 5.0) * skillLevel * 3;
        float modifiedHealing = baseHealing * (1 + healingEffectiveness);
        return modifiedHealing + magicAttack;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        // Find target using raycast
        LivingEntity target = findTargetEntity(player);
        if (target == null) {
            target = player; // Self-cast if no target found
        }
        HealingLightEntity healingLightEntity = new HealingLightEntity(target, target.getWorld());
        healingLightEntity.setPos(target.getX(), target.getY(), target.getZ());

        float healingAmount = calculateHealingAmount(player, skillLevel);

        if (target.getType().isIn(EntityTypeTags.UNDEAD)) {
            // Damage undead with holy damage (half the healing amount)
            float holyDamage = healingAmount * 0.5f;

            DamageSource holyDamageSource = ModDamageTypes.create(serverWorld, ModDamageTypes.HOLY, player);
            target.damage(holyDamageSource, holyDamage);

            target.getWorld().spawnEntity(healingLightEntity);
        } else {
            target.heal(healingAmount);
            target.getWorld().spawnEntity(healingLightEntity);
            // Healing particles
            serverWorld.spawnParticles(ParticleTypes.HEART,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    8, 0.3, 0.3, 0.3, 0.1);

            // Play healing sound
            serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS,
                    0.5f, 1.8f);
        }

        return true;
    }

    private LivingEntity findTargetEntity(PlayerEntity player) {
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d direction = player.getRotationVec(1.0f);
        int range = 16;
        EntityHitResult entityHit = null;
        double closestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : player.getWorld().getEntitiesByClass(LivingEntity.class,
                player.getBoundingBox().expand(range),
                e -> e != player && e.isAlive())) {

            // Check if entity is in line of sight
            Vec3d entityCenter = entity.getBoundingBox().getCenter();
            double distance = start.distanceTo(entityCenter);

            if (distance < closestDistance) {
                // Simple line of sight check
                Vec3d toEntity = entityCenter.subtract(start).normalize();
                if (direction.dotProduct(toEntity) > 0.8) { // Within ~36 degree cone
                    closestDistance = distance;
                    entityHit = new EntityHitResult(entity);
                }
            }
        }

        return entityHit != null ? (LivingEntity) entityHit.getEntity() : null;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ACOLYTE;
    }
}