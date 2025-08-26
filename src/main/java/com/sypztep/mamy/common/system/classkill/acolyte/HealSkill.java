package com.sypztep.mamy.common.system.classkill.acolyte;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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

        float healingAmount = calculateHealingAmount(player, skillLevel);

        if (target.getType().isIn(EntityTypeTags.UNDEAD)) {
            // Damage undead with holy damage (half the healing amount)
            float holyDamage = healingAmount * 0.5f;

            DamageSource holyDamageSource = ModDamageTypes.create(serverWorld, ModDamageTypes.HOLY, player);
            target.damage(holyDamageSource, holyDamage);

//            // Mushroom Cloud Effect
//            double baseX = target.getX();
//            double baseY = target.getY();
//            double baseZ = target.getZ();
//
//// Initial explosion flash
//            serverWorld.spawnParticles(ParticleTypes.FLASH, baseX, baseY + 1, baseZ, 20, 0.5, 0.5, 0.5, 0.1);
//            serverWorld.spawnParticles(ParticleTypes.EXPLOSION, baseX, baseY + 1, baseZ, 15, 1, 1, 1, 0.1);
//
//// Mushroom cloud stem - vertical smoke column
//            for (int i = 0; i < 8; i++) {
//                double stemY = baseY + (i * 1.5);
//                // Gray dust for stem (lighter gray)
//                DustParticleEffect grayDust = new DustParticleEffect(Vec3d.unpackRgb(0xC0C0C0).toVector3f(), 3.0f);
//                serverWorld.spawnParticles(grayDust, baseX, stemY, baseZ, 25, 0.3, 0.1, 0.3, 0.02);
//                // Some smoke mixed in
//                serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, baseX, stemY, baseZ, 15, 0.2, 0.05, 0.2, 0.02);
//                serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, baseX, stemY, baseZ, 10, 0.15, 0.05, 0.15, 0.01);
//            }
//
//            // Mushroom head - expanding rings at different heights
//            double headHeight = baseY + 12;
//
//            // Outer ring - expanding outward (gray to red dust)
//            for (int ring = 1; ring <= 4; ring++) {
//                double radius = ring * 4.0;
//                int particleCount = ring * 20;
//
//                for (int i = 0; i < particleCount; i++) {
//                    double angle = (2 * Math.PI * i) / particleCount;
//                    double offsetX = Math.cos(angle) * radius;
//                    double offsetZ = Math.sin(angle) * radius;
//
//                    // Gray dust for outer particles
//                    if (ring <= 3) {
//                        // Inner rings - darker gray transitioning to red
//                        int color = 0x666666 + (ring * 0x220000); // Gray to reddish
//                        DustParticleEffect dustEffect = new DustParticleEffect(Vec3d.unpackRgb(color).toVector3f(), 2.0f);
//                        serverWorld.spawnParticles(dustEffect,
//                                baseX + offsetX, headHeight - 4, baseZ + offsetZ,
//                                15, 0.4, 0.2, 0.4, 0.02);
//                    } else {
//                        // Outer rings - more red
//                        DustParticleEffect redDust = new DustParticleEffect(Vec3d.unpackRgb(0xCC4D33).toVector3f(), 3.0f);
//                        serverWorld.spawnParticles(redDust,
//                                baseX + offsetX, headHeight - 8, baseZ + offsetZ,
//                                20, 0.5, 0.3, 0.5, 0.02);
//                    }
//                }
//            }
//
//            for (int layer = 0; layer < 12; layer++) {
//                double layerY = headHeight + (layer * 0.55) - 1;
//                double layerRadius = 8 - (layer * 1.15);
//
//                for (int i = 0; i < 60; i++) {
//                    double angle = (2 * Math.PI * i) / 60;
//                    double offsetX = Math.cos(angle) * layerRadius;
//                    double offsetZ = Math.sin(angle) * layerRadius;
//                    serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, baseX + offsetX, layerY, baseZ + offsetZ,
//                            4, 0.3, 0.6, 0.3, 0.02);
//                }
//            }
//
//            // Rising smoke trails from the head
//            for (int i = 0; i < 5; i++) {
//                double smokeY = headHeight + (i * 2);
//                // Dispersing smoke as it rises
//                serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, baseX, smokeY, baseZ, 30, 1 + i, 0.5, 1 + i, 0.02);
//                serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, baseX, smokeY, baseZ, 20, 0.8 + i, 0.3, 0.8 + i, 0.01);
//            }

        } else {
            // Heal living target
            target.heal(healingAmount);

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