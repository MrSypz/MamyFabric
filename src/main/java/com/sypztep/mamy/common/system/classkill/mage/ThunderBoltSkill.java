package com.sypztep.mamy.common.system.classkill.mage;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.skill.ThunderBoltEntity;
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

public class ThunderBoltSkill extends Skill implements CastableSkill {

    public ThunderBoltSkill(Identifier id) {
        super(id, "Thunder Bolt", "Launch a chain lightning that jumps between enemies",
                12f, 0.3f, 10, Mamy.id("skill/thunder_bolt"));
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return Math.max(10, 25 - (skillLevel * 2)); // 25 -> 7 ticks (1.25 -> 0.35 seconds)
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 3; // 0.15 seconds fixed cast
    }

    @Override
    public boolean canBeInterupt() {
        return true;
    }

    @Override
    public Identifier getCastedAnimation() {
        return Mamy.id("thunder_bolt_cast");
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        float baseDamage = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT);
        data.baseDamage = baseDamage + (8 + (skillLevel * 3)); // 8 + 3*level base damage
        data.damageType = DamageTypeRef.ELEMENT;
        data.maxHits = Math.min(5, 3 + (skillLevel / 3)); // 3-5 targets based on level

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

        float baseDamage = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT);
        float damage = baseDamage + (8 + (skillLevel * 3));
        int maxTargets = Math.min(5, 3 + (skillLevel / 3));

        ThunderBoltEntity thunderBolt = new ThunderBoltEntity(world, player, damage, maxTargets);

        Vec3d startPos = player.getEyePos().add(player.getRotationVec(1.0f).multiply(0.3));
        thunderBolt.setPosition(startPos.x, startPos.y, startPos.z);

        world.spawnEntity(thunderBolt);

        world.playSound(null, player.getBlockPos(),
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS,
                0.8f, 1.3f);

        return true;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.MAGE;
    }
}