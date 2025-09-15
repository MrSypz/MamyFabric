package com.sypztep.mamy.common.util;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.entity.Entity;

public record MultiHitRecord(Int2IntMap hitCounts, int maxHitsPerTarget) {
    public MultiHitRecord(int maxHitsPerTarget) {
        this(new Int2IntOpenHashMap(), maxHitsPerTarget);
        this.hitCounts.defaultReturnValue(0);
    }

    public boolean canHit(Entity entity) {
        return hitCounts.get(entity.getId()) < maxHitsPerTarget;
    }

    public int recordHitAndGet(Entity entity) {
        int newHits = hitCounts.get(entity.getId()) + 1;
        hitCounts.put(entity.getId(), newHits);
        return newHits;
    }

    public void recordHit(Entity entity) {
        recordHitAndGet(entity);
    }

    public int getHitCount(Entity entity) {
        return hitCounts.get(entity.getId());
    }
}