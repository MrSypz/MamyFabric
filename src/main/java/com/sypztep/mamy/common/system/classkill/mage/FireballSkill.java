package com.sypztep.mamy.common.system.classkill.mage;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.skill.FireballEntity;
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
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class FireballSkill extends Skill implements CastableSkill {

    public FireballSkill(Identifier id, List<SkillRequirement> skillRequirements) {
        super(id, "Fireball", "Launches an explosive fireball that deals AoE fire damage in a 5x5 area",
                25, 0.7f, 10, Mamy.id("skill/fireball"), skillRequirements);
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return (int) (0.8f * 20); // 16 ticks
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return (int) (0.2f * 20); // 4 ticks
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
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT) + 1 + (2 * skillLevel);
        data.damageType = DamageTypeRef.ELEMENT;
        data.maxHits = 1; // 5x5 area (2.5 block radius)

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

        float damage = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT) + 1 + (2 * skillLevel);

        // Create fireball
        FireballEntity fireball = new FireballEntity(world, player, damage, skillLevel);

        // Set starting position (slightly in front of player)
        Vec3d startPos = player.getEyePos().add(player.getRotationVec(1.0f).multiply(0.5));
        fireball.setPosition(startPos.x, startPos.y, startPos.z);

        world.spawnEntity(fireball);

        // Play casting sound - deeper than firebolt
        world.playSound(null, player.getBlockPos(),
                SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS,
                1.0f, 0.8f);

        return true;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.MAGE;
    }
}