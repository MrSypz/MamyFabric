package com.sypztep.mamy.common.system.stat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.EnumMap;
import java.util.Map;

public final class LivingStats {
    private final Map<StatTypes, Stat> stats;
    private final LevelSystem levelSystem;

    public LivingStats(LivingEntity living) {
        this.stats = new EnumMap<>(StatTypes.class);
        this.levelSystem = new LevelSystem(living);
        for (StatTypes statType : StatTypes.values()) {
            this.stats.put(statType, statType.createStat((short) 0)); // Base value = 0
        }
    }

    public Stat getStat(StatTypes statType) {
        return stats.get(statType);
    }

    private void allocatePoints(StatTypes statType, short points) {
        Stat stat = getStat(statType);
        if (stat != null) stat.increase(points);
    }

    public void useStatPoint(StatTypes types, short points) {
        short perPoint = this.getStat(types).getIncreasePerPoint();
        short statPoints = this.getLevelSystem().getStatPoints();

        if (statPoints >= perPoint * points) {
            this.getLevelSystem().subtractStatPoints((short) (perPoint * points));
            allocatePoints(types, points);
        }
    }
    public void resetStatsWithPointReturn(PlayerEntity player) {
        stats.values().forEach(stat -> stat.reset(player, levelSystem));
    }
    public void resetStats(PlayerEntity player) {
        stats.values().forEach(stat -> stat.reset(player));
    }

    public LevelSystem getLevelSystem() {
        return levelSystem;
    }

    //------------------------utility---------------------
    public void writeToNbt(NbtCompound tag, LivingEntity living) {
        NbtCompound statsCompound = new NbtCompound();
        stats.forEach((type, stat) -> {
            NbtCompound statTag = new NbtCompound();
            stat.writeToNbt(statTag, living);
            statsCompound.put(type.name().toLowerCase(), statTag); // Use lowercase like Dominatus
        });
        tag.put("Stats", statsCompound);
        levelSystem.writeToNbt(tag);
    }

    public void readFromNbt(NbtCompound tag, LivingEntity living) {
        NbtCompound statsCompound = tag.getCompound("Stats");
        if (!statsCompound.isEmpty()) {
            stats.forEach((type, stat) -> {
                NbtCompound statTag = statsCompound.getCompound(type.name().toLowerCase());
                stat.readFromNbt(statTag, living);
            });
        }
        levelSystem.readFromNbt(tag);
    }

}


