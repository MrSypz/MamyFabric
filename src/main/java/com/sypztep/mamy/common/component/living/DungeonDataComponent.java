package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DungeonDataComponent implements AutoSyncedComponent {
    private final PlayerEntity player;
    private UUID currentDungeonId = null;
    private BlockPos returnPosition = null;
    private String returnDimension = null;
    private final Map<String, Long> dungeonCooldowns = new HashMap<>();

    public DungeonDataComponent(PlayerEntity player) {
        this.player = player;
    }

    public boolean isInDungeon() {
        return currentDungeonId != null;
    }

    public UUID getCurrentDungeonId() {
        return currentDungeonId;
    }

    public void setCurrentDungeonId(UUID dungeonId) {
        this.currentDungeonId = dungeonId;
        ModEntityComponents.DUNGEON_DATA.sync(player);
    }

    public BlockPos getReturnPosition() {
        return returnPosition;
    }

    public void setReturnPosition(BlockPos pos, String dimension) {
        this.returnPosition = pos;
        this.returnDimension = dimension;
    }

    public String getReturnDimension() {
        return returnDimension;
    }

    public boolean isOnCooldown(String dungeonType, long currentTime) {
        return dungeonCooldowns.containsKey(dungeonType) &&
                currentTime < dungeonCooldowns.get(dungeonType);
    }

    public long getCooldownRemaining(String dungeonType, long currentTime) {
        if (!isOnCooldown(dungeonType, currentTime)) return 0;
        return dungeonCooldowns.get(dungeonType) - currentTime;
    }

    public void setCooldown(String dungeonType, long endTime) {
        dungeonCooldowns.put(dungeonType, endTime);
    }
    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (tag.containsUuid("currentDungeonId")) {
            currentDungeonId = tag.getUuid("currentDungeonId");
        }

        if (tag.contains("returnPos")) {
            NbtCompound posTag = tag.getCompound("returnPos");
            returnPosition = new BlockPos(
                    posTag.getInt("x"),
                    posTag.getInt("y"),
                    posTag.getInt("z")
            );
        }

        if (tag.contains("returnDimension")) {
            returnDimension = tag.getString("returnDimension");
        }

        if (tag.contains("cooldowns")) {
            NbtCompound cooldownTag = tag.getCompound("cooldowns");
            dungeonCooldowns.clear();
            for (String key : cooldownTag.getKeys()) {
                dungeonCooldowns.put(key, cooldownTag.getLong(key));
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (currentDungeonId != null) {
            tag.putUuid("currentDungeonId", currentDungeonId);
        }

        if (returnPosition != null) {
            NbtCompound posTag = new NbtCompound();
            posTag.putInt("x", returnPosition.getX());
            posTag.putInt("y", returnPosition.getY());
            posTag.putInt("z", returnPosition.getZ());
            tag.put("returnPos", posTag);
        }

        if (returnDimension != null) {
            tag.putString("returnDimension", returnDimension);
        }

        if (!dungeonCooldowns.isEmpty()) {
            NbtCompound cooldownTag = new NbtCompound();
            for (Map.Entry<String, Long> entry : dungeonCooldowns.entrySet()) {
                cooldownTag.putLong(entry.getKey(), entry.getValue());
            }
            tag.put("cooldowns", cooldownTag);
        }
    }
}
