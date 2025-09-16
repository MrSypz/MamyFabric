package com.sypztep.mamy.common.system.classkill.acolyte;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModStatusEffects;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.util.SkillUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class BlessingSkill extends Skill {

    public BlessingSkill(Identifier identifier, List<SkillRequirement> skillRequirements) {
        super(identifier, "Blessing",
                "Places a temporary buff on target that increases STR, DEX, INT, and HIT. Purges curse and stone effects. Against undead/demons, reduces their DEX and INT instead.",
                24f, 0f,
                10,
                Mamy.id("skill/blessing"),skillRequirements);
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return super.getResourceCost(skillLevel) + (skillLevel * 4f);
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

        // This is a buff skill
        data.baseDamage = 0;
        data.damageType = DamageTypeRef.HEAL; // Use heal type for beneficial effects
        data.maxHits = 1;

        // Target and range
        data.targetType = "Single Target or Self";
        data.targetRange = 9f;

        // Cooldown override
        data.overrideCooldown = true;
        data.customCooldownText = "None";

        if (skillLevel > 0) {
            int hitIncrease = skillLevel * 2;
            int duration = 40 + (skillLevel * 20);

            // Status effects applied
            data.statusEffectsApplied.add("STR: +" + skillLevel);
            data.statusEffectsApplied.add("DEX: +" + skillLevel);
            data.statusEffectsApplied.add("INT: +" + skillLevel);
            data.statusEffectsApplied.add("HIT: +" + hitIncrease);
            data.statusEffectsApplied.add("Duration: " + duration + "s");

            // Additional effects
            if (skillLevel >= 2) {
                data.additionalEffects.add("Purges Curse & Stone effects");
            }
            data.additionalEffects.add("vs Undead/Demons: Reduces DEX & INT instead");
        }

        // Context-sensitive tip for learning screen
        data.contextTip = "Powerful blessing that enhances stats or weakens evil creatures";

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

        // Find target using the new SkillUtil helper
        LivingEntity target = SkillUtil.findTargetEntity(player, 9.0);
        if (target == null) target = player; // Self-cast if no target found

        // Purge curse and stone effects (2nd stage only means skill level >= 2)
        if (skillLevel >= 2) {
            purgeCurseAndStone(target);
        }

        // Calculate duration: 40 + (skillLevel * 20) seconds, converted to ticks
        int duration = (40 + (skillLevel * 20)) * 20;

        // Amplifier = (skillLevel - 1) so level 1 = amplifier 0
        StatusEffectInstance blessing = new StatusEffectInstance(ModStatusEffects.BLESSING, duration, skillLevel - 1, false, false, false);

        boolean success = target.addStatusEffect(blessing);

        if (success) {
            boolean isUndeadOrDemon = target.getType().isIn(EntityTypeTags.UNDEAD) || isDemon(target);

            if (isUndeadOrDemon) {
                serverWorld.spawnParticles(ParticleTypes.SMOKE, target.getX(), target.getY() + target.getHeight() / 2, target.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
                serverWorld.spawnParticles(ParticleTypes.SOUL, target.getX(), target.getY() + target.getHeight() / 2, target.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
                serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_WITCH_HURT, SoundCategory.PLAYERS, 0.8f, 1.2f);
            } else {
                serverWorld.spawnParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + target.getHeight() / 2, target.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
                serverWorld.spawnParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + target.getHeight() / 2, target.getZ(), 12, 0.4, 0.4, 0.4, 0.1);
                serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.8f, 1.5f);
            }
        }

        return success;
    }

    private void purgeCurseAndStone(LivingEntity target) {
        target.removeStatusEffect(StatusEffects.SLOWNESS);
        target.removeStatusEffect(StatusEffects.WEAKNESS);
        target.removeStatusEffect(StatusEffects.MINING_FATIGUE);
    }

    private boolean isDemon(LivingEntity entity) {
        String entityName = entity.getType().toString().toLowerCase();
        return entityName.contains("wither") || entityName.contains("blaze") || entityName.contains("ghast") || entityName.contains("demon") || entityName.contains("devil"); // Add more demon types as needed
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ACOLYTE;
    }
}