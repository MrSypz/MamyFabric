package com.sypztep.mamy.common.system.classkill.mage;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.skill.MagicArrowEntity;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class MagicArrowSkill extends Skill implements CastableSkill {

    public MagicArrowSkill(Identifier id) {
        super(id, "Magic Arrow", "Fires magic arrows that can pierce through multiple enemies",
                5, 0f, ModClasses.MAGE, 1, 1, 10, false,
                Mamy.id("skill/magic_arrow"));
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return 0; // Instant cast
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 0; // No fixed cast time
    }

    @Override
    public boolean canBeInterupt() {
        return true;
    }

    @Override
    public boolean shouldLockMovement() {
        return true;
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return 5 + skillLevel * 5; // Fixed cost
    }

    @Override
    public float getCooldown(int skillLevel) {
        return 2;
    }

    private int getMaxTargets(int skillLevel) {
        if (skillLevel >= 6) return 10;
        if (skillLevel == 5) return 5;
        if (skillLevel == 2) return 3;
        return 2;
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT) + (0.7f * skillLevel);
        data.damageType = DamageType.MAGIC;
        data.maxHits = getMaxTargets(skillLevel);

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(caster.getWorld() instanceof ServerWorld world)) return false;

        float matk = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT) + (0.7f * skillLevel);

        MagicArrowEntity arrow = new MagicArrowEntity(world, player, matk, skillLevel, 0);
        arrow.setPosition(player.getX(), player.getEyeY(), player.getZ());
        world.spawnEntity(arrow);

        world.playSound(null, player.getBlockPos(),
                SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS,
                1.0f, 1.0f);

        return true;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.MAGE;
    }
}