package com.sypztep.mamy.common.system.classkill.acolyte;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModStatusEffects;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.util.List;

public class AngelusSkill extends Skill implements CastableSkill {

    public AngelusSkill(Identifier identifier, List<SkillRequirement> skillRequirements) {
        super(identifier, "Angelus", "Places a temporary buff on user and all party members in area that increases Soft Defense and Max HP.",
                20f, 1f,
                ModClasses.ACOLYTE, 1,
                1, 10,
                false, Mamy.id("skill/angelus"),skillRequirements);
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return 20f + (skillLevel * 3f);
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return 7; // 0.35 seconds (7 ticks)
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 3; // 0.15 seconds (3 ticks)
    }

    @Override
    public float getCooldown(int skillLevel) {
        return 3.5f; // 3.5 seconds cast delay
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

        // This is an area buff skill
        data.baseDamage = 0;
        data.damageType = DamageType.HEAL; // Use heal type for beneficial effects
        data.maxHits = 0; // Area effect, not single target

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        return player.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        // Calculate duration: skill level × 30 seconds, converted to ticks
        int duration = (skillLevel * 30) * 20;

        // Create the angelus buff effect
        StatusEffectInstance angelusBuff = new StatusEffectInstance(
                ModStatusEffects.ANGELUS,
                duration,
                skillLevel - 1, // Amplifier starts at 0 for skill level 1
                false,
                false,
                true // Show particles
        );

        // Apply to self first
        boolean appliedToSelf = player.addStatusEffect(angelusBuff);
        int targetsAffected = appliedToSelf ? 1 : 0;

        // Find all party members/players in 14x14 area (7 block radius)
        Box areaBox = new Box(player.getBlockPos()).expand(7.0);

        for (PlayerEntity nearbyPlayer : serverWorld.getEntitiesByClass(
                PlayerEntity.class,
                areaBox,
                p -> p != player && p.isAlive() && isPartyMember(player, p))) {

            // Create fresh effect instance for each target
            StatusEffectInstance nearbyPlayerBuff = new StatusEffectInstance(
                    ModStatusEffects.ANGELUS,
                    duration,
                    skillLevel - 1,
                    false,
                    false,
                    true
            );

            if (nearbyPlayer.addStatusEffect(nearbyPlayerBuff)) {
                targetsAffected++;

                // Show particles on each affected player
                serverWorld.spawnParticles(ParticleTypes.END_ROD,
                        nearbyPlayer.getX(), nearbyPlayer.getY() + nearbyPlayer.getHeight() / 2, nearbyPlayer.getZ(),
                        8, 0.3, 0.3, 0.3, 0.1);
            }
        }

        if (targetsAffected > 0) {
            // Main area effect particles centered on caster
            serverWorld.spawnParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + player.getHeight() / 2, player.getZ(),
                    20, 7.0, 1.0, 7.0, 0.2);
            serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + player.getHeight() / 2, player.getZ(),
                    15, 7.0, 1.0, 7.0, 0.1);

            // Play sound effect
            serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0f, 1.2f);
        }

        return targetsAffected > 0;
    }

    /**
     * Check if target player is a party member
     * TODO: Implement actual party system check
     * For now, affects all nearby players
     */
    private boolean isPartyMember(PlayerEntity caster, PlayerEntity target) {
        // TODO: Replace with actual party system check when available
        // For now, return true to affect all nearby players
        return true;
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        List<Text> tooltip = super.generateTooltip(player, skillLevel, isLearned, context);

        if (skillLevel > 0) {
            int defIncrease = skillLevel * 5;
            int hpIncrease = skillLevel * 50;
            int duration = skillLevel * 30;

            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Area Buff Effects:").formatted(Formatting.GOLD));
            tooltip.add(Text.literal("• Soft Defense: ").formatted(Formatting.GRAY)
                    .append(Text.literal("+" + defIncrease + "%").formatted(Formatting.GREEN)));
            tooltip.add(Text.literal("• Max HP: ").formatted(Formatting.GRAY)
                    .append(Text.literal("+" + hpIncrease).formatted(Formatting.GREEN)));
            tooltip.add(Text.literal("• Duration: ").formatted(Formatting.GRAY)
                    .append(Text.literal(duration + "s").formatted(Formatting.YELLOW)));

            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Area: ").formatted(Formatting.AQUA)
                    .append(Text.literal("14×14 blocks").formatted(Formatting.WHITE)));
            tooltip.add(Text.literal("Targets: ").formatted(Formatting.AQUA)
                    .append(Text.literal("Self + Party Members").formatted(Formatting.WHITE)));
        }

        return tooltip;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ACOLYTE;
    }
}