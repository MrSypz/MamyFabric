package com.sypztep.mamy.common.system.classkill.thief;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.util.SkillUtil;
import net.minecraft.entity.LivingEntity;
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

public class DetoxifySkill extends Skill {

    public DetoxifySkill(Identifier identifier, List<SkillRequirement> skillRequirements) {
        super(identifier, "Detoxify", "Cures any target within range of Poison and Wither status effects.",
                10f, 0f,
                1,
                Mamy.id("skill/detoxify"), skillRequirements);
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        // Use the universal tooltip renderer
        SkillTooltipData data = getSkillTooltipData(player, skillLevel);
        return SkillTooltipRenderer.render(this, data, player, skillLevel, isLearned, context);
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        // Basic skill properties
        data.baseDamage = 0;
        data.damageType = DamageTypeRef.HEAL;
        data.maxHits = 1;

        // Status effects removed
        data.statusEffectsRemoved.add("Poison");
        data.statusEffectsRemoved.add("Wither");

        // Target and range
        data.targetType = "Single Target or Self";
        data.targetRange = 9f;

        // Cooldown override
        data.overrideCooldown = true;
        data.customCooldownText = "None";

        // Context-sensitive tip for learning screen
        data.contextTip = "Quick poison and wither removal for yourself or allies";

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!player.getWorld().isClient()) {
            LivingEntity target = SkillUtil.findTargetEntity(player, 9f);

            if (target != null && target.isAlive()) {
                boolean cured = false;

                // Remove poison effect
                if (target.hasStatusEffect(StatusEffects.POISON)) {
                    target.removeStatusEffect(StatusEffects.POISON);
                    cured = true;
                }

                // Remove wither effect
                if (target.hasStatusEffect(StatusEffects.WITHER)) {
                    target.removeStatusEffect(StatusEffects.WITHER);
                    cured = true;
                }

                if (cured) {
                    ServerWorld serverWorld = (ServerWorld) player.getWorld();

                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                            target.getX(), target.getY() + 1, target.getZ(),
                            10, 0.5, 0.5, 0.5, 0.1);

                    // Sound effect
                    serverWorld.playSound(null, target.getBlockPos(),
                            SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.PLAYERS,
                            1.0f, 1.5f);

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.THIEF;
    }
}