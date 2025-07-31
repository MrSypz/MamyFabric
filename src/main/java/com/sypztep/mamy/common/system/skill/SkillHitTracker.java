package com.sypztep.mamy.common.system.skill;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.entity.LivingEntity;

public class SkillHitTracker {
    private final int maxHitCount;
    private final int iframeTime;
    private final Object2IntMap<LivingEntity> hitCounts = new Object2IntOpenHashMap<>();
    private final Object2LongMap<LivingEntity> lastHitTimes = new Object2LongOpenHashMap<>();

    public SkillHitTracker(int maxHitCount, int iframeTime) {
        this.maxHitCount = maxHitCount;
        this.iframeTime = iframeTime;
    }

    public boolean canHit(LivingEntity entity, long currentTime) {
        // Check max hit count
        int currentHitCount = hitCounts.getInt(entity);
        if (currentHitCount >= maxHitCount) {
            return false;
        }

        // Check iframe time
        long lastHitTime = lastHitTimes.getLong(entity);
        return currentTime - lastHitTime >= iframeTime;
    }

    public void recordHit(LivingEntity entity, long currentTime) {
        hitCounts.put(entity, hitCounts.getInt(entity) + 1);
        lastHitTimes.put(entity, currentTime);
    }

    public int getHitCount(LivingEntity entity) {
        return hitCounts.getInt(entity);
    }

    public void reset() {
        hitCounts.clear();
        lastHitTimes.clear();
    }

    public void resetEntity(LivingEntity entity) {
        hitCounts.removeInt(entity);
        lastHitTimes.removeLong(entity);
    }
}