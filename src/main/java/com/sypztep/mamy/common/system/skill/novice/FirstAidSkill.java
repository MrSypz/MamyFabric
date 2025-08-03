package com.sypztep.mamy.common.system.skill.novice;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class FirstAidSkill extends Skill {

    public FirstAidSkill(Identifier identifier) {
        super(identifier, "First Aid", "Heal yourself and nearby allies",
                10f, 3f, ModClasses.NOVICE, 0, 2, 5, true, Mamy.id("skill/first_aid"));
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        // Percentage-based healing scaling with player's magic attack damage
        data.damagePercentage = 0.3f + (skillLevel * 0.1f); // 30% + 10% per level
        data.baseDamage = 2.0f + (skillLevel * 1.5f); // Base healing
        data.damageType = DamageType.HEAL;
        data.maxHits = 1;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public void use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return;

        // Calculate healing based on player's magic attack damage attribute
        double magicAttackDamage = player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE);
        double healingPercentage = (30 + (skillLevel * 10)) / 100.0;
        float baseHealing = 2.0f + (skillLevel * 1.5f);
        float totalHealing = (float)(magicAttackDamage * healingPercentage) + baseHealing;

        double range = 2.0 + (skillLevel * 0.5);

        // Heal the caster first
        player.heal(totalHealing);

        // Find and heal nearby allies
        var nearbyAllies = player.getWorld().getEntitiesByClass(PlayerEntity.class,
                player.getBoundingBox().expand(range),
                entity -> entity != player && entity.isAlive() && entity.isTeammate(player));

        for (PlayerEntity ally : nearbyAllies) {
            ally.heal(totalHealing);
        }

        // Healing effects
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            int particleCount = 5 + (skillLevel * 2);

            serverWorld.spawnParticles(ParticleTypes.HEART,
                    player.getX(), player.getY() + 1, player.getZ(),
                    particleCount, 0.5, 0.5, 0.5, 0.1);
        }

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS,
                0.3f + (skillLevel * 0.1f), 1.2f + (skillLevel * 0.1f));
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return true;
    }
}