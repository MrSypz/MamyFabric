package com.sypztep.mamy.common.system.skill;

import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillHitTracker {
    private final Map<UUID, HitData> hitRecords = new HashMap<>();
    private final int maxHitCount;
    private final boolean bypassIframe;
    private final long iframeTime; // in ticks

    public SkillHitTracker(int maxHitCount, boolean bypassIframe) {
        this(maxHitCount, bypassIframe, 10); // Default 0.5 seconds iframe
    }

    public SkillHitTracker(int maxHitCount, boolean bypassIframe, long iframeTime) {
        this.maxHitCount = maxHitCount;
        this.bypassIframe = bypassIframe;
        this.iframeTime = iframeTime;
    }

    public boolean canHit(LivingEntity entity, long currentTime) {
        UUID entityId = entity.getUuid();
        HitData hitData = hitRecords.get(entityId);

        // Never hit this entity before
        if (hitData == null) {
            return true;
        }

        // Check hit count limit
        if (hitData.hitCount >= maxHitCount) {
            return false;
        }

        // Check iframe if not bypassed
        if (!bypassIframe) {
            long timeSinceLastHit = currentTime - hitData.lastHitTime;
            return timeSinceLastHit >= iframeTime;
        }

        return true;
    }

    public void recordHit(LivingEntity entity, long currentTime) {
        UUID entityId = entity.getUuid();
        HitData hitData = hitRecords.get(entityId);

        if (hitData == null) {
            hitRecords.put(entityId, new HitData(1, currentTime));
        } else {
            hitData.hitCount++;
            hitData.lastHitTime = currentTime;
        }
    }

    public boolean hasReachedMaxHits() {
        return hitRecords.values().stream().anyMatch(data -> data.hitCount >= maxHitCount);
    }

    public int getHitCount(LivingEntity entity) {
        HitData hitData = hitRecords.get(entity.getUuid());
        return hitData != null ? hitData.hitCount : 0;
    }

    public long getTimeSinceLastHit(LivingEntity entity, long currentTime) {
        HitData hitData = hitRecords.get(entity.getUuid());
        return hitData != null ? currentTime - hitData.lastHitTime : Long.MAX_VALUE;
    }

    public void reset() {
        hitRecords.clear();
    }

    // Inner class to track hit data
    private static class HitData {
        int hitCount;
        long lastHitTime;

        HitData(int hitCount, long lastHitTime) {
            this.hitCount = hitCount;
            this.lastHitTime = lastHitTime;
        }
    }
}