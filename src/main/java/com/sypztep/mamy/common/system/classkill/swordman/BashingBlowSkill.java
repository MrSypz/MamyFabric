package com.sypztep.mamy.common.system.classkill.swordman;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.*;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.damage.HybridDamageSource;
import com.sypztep.mamy.common.system.damage.DamageComponent;
import com.sypztep.mamy.common.system.damage.ElementType;
import com.sypztep.mamy.common.system.damage.CombatType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;

import java.util.List;

public class BashingBlowSkill extends Skill {

    public BashingBlowSkill(Identifier identifier) {
        super(identifier, "Bashing Blow", "Dash 5 blocks forward and deal massive Physical+Fire damage to the first enemy hit",
                8f, 0.5f,
                10,
                Mamy.id("skill/bashing_blow"));
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return skillLevel <= 5 ? super.getResourceCost(skillLevel) : 16; // base * 2
    }

    /**
     * Define this skill as Physical+Fire hybrid damage
     * 60% Physical scaling + 40% Fire scaling, with Melee combat bonuses
     */
    @Override
    public List<DamageComponent> getDamageComponents() {
        return List.of(
                DamageComponent.hybrid(ElementType.PHYSICAL, 0.6f, CombatType.MELEE, 1.0f),
                DamageComponent.hybrid(ElementType.FIRE, 0.4f, CombatType.MELEE, 0.3f)
        );
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
        return mainHand.isIn(ModTags.Items.ONE_HAND_SWORDS) ||
                mainHand.isIn(ModTags.Items.TWO_HAND_SWORDS) ||
                mainHand.isIn(ModTags.Items.SPEARS);
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
        Vec3d endPos = startPos.add(direction.multiply(5.0)); // 5 block range

        // Create damage area along the dash path
        Box damageArea = new Box(
                Math.min(startPos.x, endPos.x) - 1, Math.min(startPos.y, endPos.y), Math.min(startPos.z, endPos.z) - 1,
                Math.max(startPos.x, endPos.x) + 1, Math.max(startPos.y, endPos.y) + 2, Math.max(startPos.z, endPos.z) + 1
        );

        // Find and damage entities in the path
        List<LivingEntity> targets = serverWorld.getEntitiesByClass(LivingEntity.class, damageArea,
                entity -> entity != player && entity.isAlive());

        // Damage the first target found
        if (!targets.isEmpty()) {
            LivingEntity target = targets.getFirst();

            // Create custom damage source that carries hybrid damage information
            DamageSource damageSource = new HybridSkillDamageSource(
                    serverWorld.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(ModDamageTypes.BASHING_BLOW),
                    player, this);

            target.damage(damageSource, finalDamage);
        }

        // Create vertical slash effect from above with fire elements
        Vec3d centerPos = startPos.add(direction.multiply(2.5)); // Middle of dash path
        spawnSkillVisualEffects(serverWorld, centerPos);

        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, 1.2f);

        return true;
    }

    /**
     * Enhanced visual effects for hybrid skill
     */
    private void spawnSkillVisualEffects(ServerWorld world, Vec3d centerPos) {
        // Create vertical slash particles falling from above
        for (int i = 0; i < 15; i++) {
            double heightOffset = 3.0 - (i * 0.2); // Start 3 blocks above, work down
            double x = centerPos.x + (Math.random() - 0.5) * 2.0; // Spread horizontally
            double z = centerPos.z + (Math.random() - 0.5) * 2.0;

            // Physical slash effects
            world.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                    x, centerPos.y + heightOffset, z,
                    1, 0.1, 0.0, 0.1, 0.0);

            // Fire effects mixed in
            if (i % 3 == 0) { // Every 3rd particle for fire
                world.spawnParticles(ParticleTypes.FLAME,
                        x, centerPos.y + heightOffset, z,
                        1, 0.1, 0.0, 0.1, 0.02);
            }

            world.spawnParticles(ParticleTypes.CRIT,
                    x, centerPos.y + heightOffset, z,
                    2, 0.3, 0.1, 0.3, 0.05);
        }

        // Enhanced explosion with fire
        world.spawnParticles(ParticleTypes.EXPLOSION,
                centerPos.x, centerPos.y, centerPos.z,
                3, 1.0, 0.1, 1.0, 0.1);
        world.spawnParticles(ParticleTypes.LARGE_SMOKE,
                centerPos.x, centerPos.y, centerPos.z,
                5, 0.8, 0.1, 0.8, 0.08);
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
    private static class HybridSkillDamageSource extends DamageSource implements HybridDamageSource {
        private final Skill skill;

        public HybridSkillDamageSource(RegistryEntry.Reference<DamageType> type,
                                       LivingEntity attacker, Skill skill) {
            super(type, attacker);
            this.skill = skill;
        }

        @Override
        public List<DamageComponent> getDamageComponents() {
            return skill.getDamageComponents();
        }
    }
}