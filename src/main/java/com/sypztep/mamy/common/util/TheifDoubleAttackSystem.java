package com.sypztep.mamy.common.util;

import com.sypztep.mamy.client.util.ParticleHandler;
import com.sypztep.mamy.common.init.ModCustomParticles;
import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class TheifDoubleAttackSystem {
    private static final Map<UUID, MultiHitData> activeHits = new HashMap<>();

    private static class MultiHitData {
        final Entity target;
        final int totalHits;
        int currentHit;
        float nextHitTick;
        float baseAmount; // Store the base damage amount

        MultiHitData(Entity target, int totalHits, float startTick, float baseAmount) {
            this.target = target;
            this.totalHits = totalHits;
            this.currentHit = 1;
            this.nextHitTick = startTick;
            this.baseAmount = baseAmount;
        }
    }

    public static void scheduleMultiHit(PlayerEntity player, Entity target, int hitCount) {
        if (!player.getWorld().isClient) {
            hitCount = Math.min(hitCount, 2);
            long startTick = player.getWorld().getServer().getTicks() + 3; // delay first hit by 0.5 sec
            float baseAmount = (float) (player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) + player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT)) * 0.5f;
            activeHits.put(player.getUuid(), new MultiHitData(target, hitCount, startTick, baseAmount));
        }
    }

    public static void tick(MinecraftServer server) {
        long currentTick = server.getTicks();
        Iterator<Map.Entry<UUID, MultiHitData>> iterator = activeHits.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, MultiHitData> entry = iterator.next();
            MultiHitData data = entry.getValue();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());

            if (player == null || !data.target.isAlive()) {
                iterator.remove();
                continue;
            }

            if (currentTick >= data.nextHitTick) {
                applyMultiHitDamage(player, data);

                data.currentHit++;
                data.nextHitTick = currentTick + 3; // 0.5 sec delay for each hit

                if (data.currentHit >= data.totalHits) iterator.remove();
            }
        }
    }

    private static void applyMultiHitDamage(ServerPlayerEntity player, MultiHitData data) {
        if (data.target instanceof LivingEntity) {
            if (LivingEntityUtil.isCrit(player)) {
                if (data.target.damage(player.getDamageSources().create(ModDamageTypes.DOUBLE_ATTACK), (float) (data.baseAmount * (1 + player.getAttributeValue(ModEntityAttributes.CRIT_DAMAGE))))) {
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);

                    if (data.target instanceof LivingEntity livingTarget) {
                        ParticleHandler.sendToAll(livingTarget, player, ModCustomParticles.CRITICAL);
                        ParticleHandler.sendToAll(livingTarget, player, ParticleTypes.CRIT);
                    }
                }
            } else {
                if (data.target.damage(player.getDamageSources().create(ModDamageTypes.DOUBLE_ATTACK), data.baseAmount)) {
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
                }
            }
        } else {
            if (data.target.damage(player.getDamageSources().create(ModDamageTypes.DOUBLE_ATTACK), data.baseAmount)) {
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
            }
        }
    }
}