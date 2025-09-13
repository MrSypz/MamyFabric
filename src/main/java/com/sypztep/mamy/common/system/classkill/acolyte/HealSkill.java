package com.sypztep.mamy.common.system.classkill.acolyte;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.skill.HealingLightEntity;
import com.sypztep.mamy.common.init.*;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.SkillUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class HealSkill extends Skill implements CastableSkill {

    public HealSkill(Identifier identifier) {
        super(identifier, "Heal", "Restore HP to a single target. Damages undead with holy power.",
                13f, 1f,
                10,
                Mamy.id("skill/heal"));
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return super.getResourceCost(skillLevel) + (skillLevel - 1) * 3f;
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
    public Identifier getCastedAnimation() {
        return Mamy.id("prise");
    }

    @Override
    public boolean canBeInterupt() {
        return true;
    }

    private float calculateHealingAmount(PlayerEntity player, int skillLevel) {
        int baseLevel = ModEntityComponents.LIVINGLEVEL.get(player).getLevel();
        int intelligence = ModEntityComponents.LIVINGLEVEL.get(player).getStatValue(StatTypes.INTELLIGENCE);
        float magicAttack = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT);
        float healingEffectiveness = (float) player.getAttributeValue(ModEntityAttributes.HEAL_EFFECTIVE);

        float statBasedHealing = (float) Math.floor((baseLevel + intelligence) / 5.0) * skillLevel * 3;
        float minimumHealing = skillLevel * 5f;

        float baseHealing = Math.max(statBasedHealing, minimumHealing);
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
        LivingEntity target = SkillUtil.findTargetEntity(player, 9);
        if (target == null) target = player;

        HealingLightEntity healingLightEntity = new HealingLightEntity(target, target.getWorld());
        healingLightEntity.setPos(target.getX(), target.getY(), target.getZ());

        float healingAmount = calculateHealingAmount(player, skillLevel);

        if (target.getType().isIn(EntityTypeTags.UNDEAD)) {
            float holyDamage = healingAmount * 0.5f;

            DamageSource holyDamageSource = ModDamageTypes.create(serverWorld, ModDamageTypes.HOLY, player);
            target.damage(holyDamageSource, holyDamage);

            target.getWorld().spawnEntity(healingLightEntity);
        } else {
            target.heal(healingAmount);
            target.getWorld().spawnEntity(healingLightEntity);
            // Healing particles
            serverWorld.spawnParticles(ParticleTypes.HEART, target.getX(), target.getY() + target.getHeight() / 2, target.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
        }
        serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(), ModSoundEvents.ENTITY_GENERIC_AHHH, SoundCategory.PLAYERS, 0.8f, 1f + target.getRandom().nextFloat() * 0.2f);

        return true;
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        List<Text> tooltip = super.generateTooltip(player, skillLevel, isLearned, context);

        // Add healing formula explanation
        if (skillLevel > 0) {
            int baseLevel = ModEntityComponents.LIVINGLEVEL.get(player).getLevel();
            int intelligence = ModEntityComponents.LIVINGLEVEL.get(player).getStatValue(StatTypes.INTELLIGENCE);
            float magicAttack = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT);
            float healingEffectiveness = (float) player.getAttributeValue(ModEntityAttributes.HEAL_EFFECTIVE);

            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Healing Details:").formatted(Formatting.GOLD));

            // Show current stats contribution
            float statHealing = (float) Math.floor((baseLevel + intelligence) / 5.0) * skillLevel * 3;
            float minimumHealing = skillLevel * 5f;
            float baseHealing = Math.max(statHealing, minimumHealing);
            float totalHealing = calculateHealingAmount(player, skillLevel);

            tooltip.add(Text.literal("• Base Healing: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.0f", baseHealing)).formatted(Formatting.GREEN)));

            if (magicAttack > 0) {
                tooltip.add(Text.literal("• Magic Attack Bonus: ").formatted(Formatting.GRAY)
                        .append(Text.literal("+" + String.format("%.0f", magicAttack)).formatted(Formatting.YELLOW)));
            }

            if (healingEffectiveness > 0) {
                tooltip.add(Text.literal("• Healing Effectiveness: ").formatted(Formatting.GRAY)
                        .append(Text.literal("+" + String.format("%.0f%%", healingEffectiveness * 100)).formatted(Formatting.AQUA)));
            }

            tooltip.add(Text.literal("• Total Healing: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.0f", totalHealing)).formatted(Formatting.WHITE)));

            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("vs Undead: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.0f", totalHealing * 0.5f) + " Holy Damage").formatted(Formatting.GOLD)));

        }

        return tooltip;
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = 0;
        data.damageType = DamageTypeRef.HEAL;
        data.maxHits = 1;

        return data;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ACOLYTE;
    }
}