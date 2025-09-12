package com.sypztep.mamy.common.system.classkill.thief;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModStatusEffects;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class EnvenomSkill extends Skill {

    public EnvenomSkill(Identifier identifier) {
        super(identifier, "Envenom", "Coats the weapon in poison and strikes a single target to inflict Poison property physical damage. It has a chance of leaving the target poisoned, which is reduced by the target's VIT.",
                12f, 0f,
                10,
                 Mamy.id("skill/envenom"));
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        List<Text> tooltip = super.generateTooltip(player, skillLevel, isLearned, context);

        // Add description
        if (skillLevel > 0 || context == TooltipContext.LEARNING_SCREEN) {
            tooltip.add(Text.literal(""));

            // Damage info
            int damage = skillLevel * 9;
            tooltip.add(Text.literal("Poison Damage: ").formatted(Formatting.GRAY)
                    .append(Text.literal("+" + damage).formatted(Formatting.RED)));

            // Poison chance
            int poisonChance = 14 + ((skillLevel - 1) * 4);
            tooltip.add(Text.literal("Poison Chance: ").formatted(Formatting.GRAY)
                    .append(Text.literal(poisonChance + "%").formatted(Formatting.GREEN)));

            tooltip.add(Text.literal("• Only deals damage on successful poison").formatted(Formatting.DARK_GREEN));
            tooltip.add(Text.literal("• Chance reduced by target's VIT").formatted(Formatting.DARK_GREEN));

            // Usage tip
            if (context == TooltipContext.LEARNING_SCREEN) {
                tooltip.add(Text.literal(""));
                tooltip.add(Text.literal("💡 Tip: ").formatted(Formatting.YELLOW)
                        .append(Text.literal("Higher VIT targets are harder to poison").formatted(Formatting.GRAY)));
            }
        }

        return tooltip;
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = skillLevel * 9;
        data.damageType = DamageTypeRef.PHYSICAL;
        data.maxHits = 1;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!player.isAlive()) return false;
        return !player.hasStatusEffect(ModStatusEffects.ENVENOM_WEAPON);
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        // Apply the envenom weapon buff to the player
        int duration = 300 * 20; // 5 minutes in ticks (long duration so it doesn't expire quickly)
        StatusEffectInstance envenomEffect = new StatusEffectInstance(
                ModStatusEffects.ENVENOM_WEAPON,
                duration,
                skillLevel - 1, // Store skill level in amplifier
                false,
                true, // Show particles
                true  // Show in HUD
        );

        boolean success = player.addStatusEffect(envenomEffect);

        if (success) {
            // Visual effects on the player to show weapon is coated
            serverWorld.spawnParticles(ParticleTypes.ITEM_SLIME,
                    player.getX(), player.getY() + 1, player.getZ(),
                    8, 0.3, 0.3, 0.3, 0.1);

            serverWorld.spawnParticles(ParticleTypes.SNEEZE,
                    player.getX(), player.getY() + 1, player.getZ(),
                    5, 0.2, 0.2, 0.2, 0.05);

            // Sound effect for coating weapon
            serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_HONEY_BOTTLE_DRINK, SoundCategory.PLAYERS, 0.8f, 1.4f);
        }

        return success;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.THIEF;
    }

    public static void handlePlayerAttack(PlayerEntity attacker, LivingEntity target) {
        StatusEffectInstance envenomEffect = attacker.getStatusEffect(ModStatusEffects.ENVENOM_WEAPON);
        if (envenomEffect == null) return;

        attacker.removeStatusEffect(ModStatusEffects.ENVENOM_WEAPON);

        int skillLevel = envenomEffect.getAmplifier() + 1;

        int basePoisonChance = 14 + ((skillLevel - 1) * 4);

         int targetVit = ModEntityComponents.LIVINGLEVEL.get(target).getStatValue(StatTypes.VITALITY);
        int finalPoisonChance = Math.max(1, basePoisonChance - (targetVit / 4)); // VIT/4 reduction

        // Roll for poison success
        boolean poisonSuccess = attacker.getRandom().nextInt(100) < finalPoisonChance;

        if (poisonSuccess) {
            // Deal bonus poison damage
            float bonusDamage = skillLevel * 9;
            DamageSource poisonDamageSource = ModDamageTypes.create(
                    attacker.getWorld(), ModDamageTypes.DOUBLE_ATTACK, attacker);
            target.damage(poisonDamageSource, bonusDamage);

            int poisonDuration = 3 * 20; // 30 seconds in ticks
            StatusEffectInstance poisonStatusEffect = new StatusEffectInstance(
                    StatusEffects.POISON,
                    poisonDuration,
                    2, // Poison III
                    false,
                    true,
                    true
            );
            target.addStatusEffect(poisonStatusEffect);

            if (attacker.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.SNEEZE,
                        target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                        8, 0.3, 0.3, 0.3, 0.1);

                serverWorld.spawnParticles(ParticleTypes.ITEM_SLIME,
                        target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                        5, 0.2, 0.2, 0.2, 0.05);

                // Poison sound
                serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ENTITY_SPIDER_HURT, SoundCategory.PLAYERS, 0.8f, 1.2f);
            }
        } else {
            if (attacker.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.SMOKE,
                        target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                        3, 0.2, 0.2, 0.2, 0.05);

                serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.5f, 0.8f);
            }
        }

        if (attacker.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.POOF,
                    attacker.getX(), attacker.getY() + 1, attacker.getZ(),
                    5, 0.2, 0.2, 0.2, 0.05);
        }
    }
}