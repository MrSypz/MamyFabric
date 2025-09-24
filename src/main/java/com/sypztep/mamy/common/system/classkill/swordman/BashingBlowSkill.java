package com.sypztep.mamy.common.system.classkill.swordman;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.*;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.damage.*;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillDamage;
import com.sypztep.mamy.common.util.SkillUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;
import net.minecraft.particle.ParticleTypes;

import java.util.List;

public class BashingBlowSkill extends Skill implements SkillDamage {

    public BashingBlowSkill(Identifier identifier) {
        super(identifier, "Bashing Blow", "Attack the single target deal massive Physical+Fire damage to the first enemy hit",
                8f, 0.5f,
                10,
                Mamy.id("skill/bashing_blow"));
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return skillLevel <= 5 ? super.getResourceCost(skillLevel) : 16; // base * 2
    }

    @Override
    public List<DamageComponent> getDamageComponents() {
        return List.of(
                DamageComponent.hybrid(ElementType.PHYSICAL, 0.6f, CombatType.MELEE, 1.0f),
                DamageComponent.hybrid(ElementType.FIRE, 0.4f, CombatType.MELEE, 0.3f));
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();
        float baseSkill = 5;

        data.baseDamage = baseSkill + (1 + (.2f * skillLevel));
        data.damageType = DamageTypeRef.ELEMENT; // Use ELEMENT to indicate hybrid damage
        data.maxHits = 1;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!caster.isAlive()) return false;

        ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
        return mainHand.isIn(ModTags.Items.ONE_HAND_SWORDS) || mainHand.isIn(ModTags.Items.TWO_HAND_SWORDS) || mainHand.isIn(ModTags.Items.SPEARS);
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        float baseSkill = 5;
        // Calculate damage
        float finalDamage = baseSkill + (1 + (.2f * skillLevel));

        // Get player's facing direction for targeting
        Vec3d direction = player.getRotationVector().normalize();
        Vec3d startPos = player.getPos();
        Vec3d endPos = startPos.add(direction.multiply(player.getAttributeValue(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE)));

        Box damageArea = SkillUtil.makePathBox(startPos, endPos, 2.0, 2.0);

        List<LivingEntity> targets = serverWorld.getEntitiesByClass(LivingEntity.class, damageArea, entity -> entity != player && entity.isAlive());

        if (!targets.isEmpty()) {
            LivingEntity target = targets.getFirst();
            DamageSource damageSource = ModDamageTypes.HybridSkillDamageSource.create(serverWorld, ModDamageTypes.BASHING_BLOW, player, this);
            target.damage(damageSource, finalDamage);
        }

        Vec3d centerPos = startPos.add(direction.multiply(2.5)); // Middle of dash path
        spawnSkillVisualEffects(serverWorld, centerPos);

        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, 1.2f);

        return true;
    }

    private void spawnSkillVisualEffects(ServerWorld world, Vec3d centerPos) {
        for (int i = 0; i < 15; i++) {
            double heightOffset = 3.0 - (i * 0.2); // Start 3 blocks above, work down
            double x = centerPos.x + (Math.random() - 0.5) * 2.0; // Spread horizontally
            double z = centerPos.z + (Math.random() - 0.5) * 2.0;

            world.spawnParticles(ParticleTypes.SWEEP_ATTACK, x, centerPos.y + heightOffset, z, 1, 0.1, 0.0, 0.1, 0.0);

            if (i % 3 == 0) // Every 3rd particle for fire
                world.spawnParticles(ParticleTypes.FLAME, x, centerPos.y + heightOffset, z, 1, 0.1, 0.0, 0.1, 0.02);

            world.spawnParticles(ParticleTypes.CRIT, x, centerPos.y + heightOffset, z, 2, 0.3, 0.1, 0.3, 0.05);
        }

        world.spawnParticles(ParticleTypes.EXPLOSION, centerPos.x, centerPos.y, centerPos.z, 3, 1.0, 0.1, 1.0, 0.1);
        world.spawnParticles(ParticleTypes.LARGE_SMOKE, centerPos.x, centerPos.y, centerPos.z, 5, 0.8, 0.1, 0.8, 0.08);
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
}