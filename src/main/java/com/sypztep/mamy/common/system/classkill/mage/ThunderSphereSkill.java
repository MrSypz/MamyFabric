package com.sypztep.mamy.common.system.classkill.mage;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.skill.ThunderSphereEntity;
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

import java.util.Collections;

public class ThunderSphereSkill extends Skill implements CastableSkill {

    public ThunderSphereSkill(Identifier id) {
        super(id, "Thunder Sphere", "Launch an electric sphere that creates a massive thunder explosion",
                18f, 0.4f, 10, Mamy.id("skill/thunder_sphere"));
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return Math.max(15, 45 - (skillLevel * 3)); // 45 -> 18 ticks (2.25 -> 0.9 seconds)
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 8 + (skillLevel / 2); // 0.4 -> 0.65 seconds fixed cast
    }

    @Override
    public boolean canBeInterupt() {
        return true;
    }

    @Override
    public Identifier getCastedAnimation() {
        return Mamy.id("thunder_sphere_cast");
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return 18f + (skillLevel * 2.5f); // 18 -> 43 resource cost
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        float baseDamage = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT);
        data.baseDamage = baseDamage + (15 + (skillLevel * 5)); // 15 + 5*level base damage
        data.damageType = DamageTypeRef.ELEMENT;
        data.maxHits = 2; // Two explosion hits

        // Secondary damage info for explosion radius
        data.secondaryDamages = Collections.singletonList(
                new SecondaryDamage(DamageTypeRef.ELEMENT, data.baseDamage, 1, 2) // 6 block radius, 2 hits
        );

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
        float damage = baseDamage + (15 + (skillLevel * 5));

        // Create thunder sphere projectile
        ThunderSphereEntity thunderSphere = new ThunderSphereEntity(world, player, damage, skillLevel);

        // Set starting position slightly in front of player
        Vec3d startPos = player.getEyePos().add(player.getRotationVec(1.0f).multiply(0.5));
        thunderSphere.setPosition(startPos.x, startPos.y, startPos.z);

        world.spawnEntity(thunderSphere);

        // Play charging electric sound
        world.playSound(null, player.getBlockPos(),
                SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS,
                1.0f, 1.8f);

        return true;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.MAGE;
    }
}