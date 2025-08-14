package com.sypztep.mamy.common.reloadlistener;

import com.google.gson.JsonObject;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.data.MobExpEntry;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.ReloadHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class MamyMobExpReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID = Mamy.id("mobexp");

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        MobExpEntry.clearAll();

        ReloadHelper.processJsonResources(
                manager,
                "mobexp",
                object -> object.has("expReward") && object.has("classReward"), // validator
                filePath -> Identifier.of(filePath.substring(filePath.indexOf("/") + 1, filePath.length() - 5).replace("/", ":")),
                Registries.ENTITY_TYPE::get,
                Registries.ENTITY_TYPE.get(Registries.ENTITY_TYPE.getDefaultId()), // default
                (entityType, json) -> {
                    int expReward = json.get("expReward").getAsInt();
                    int classReward = json.get("classReward").getAsInt();
                    int baseLevel = json.has("baseLevel") ? json.get("baseLevel").getAsInt() : 1;
                    Map<StatTypes, Integer> stats = parseStats(json, entityType.toString(), baseLevel);

                    MobExpEntry entry = new MobExpEntry(expReward, classReward, baseLevel, stats);
                    MobExpEntry.addEntry(entityType, entry);
                },
                "mob"
        );
    }


    private Map<StatTypes, Integer> parseStats(JsonObject object, String entityIdStr, int baseLevel) {
        if (!object.has("stats")) {
            Mamy.LOGGER.debug("No stats found for '{}', using default level-based stats", entityIdStr);
            return createLevelBasedStats(Math.max(1, baseLevel));
        }

        JsonObject statsObject = object.getAsJsonObject("stats");

        try {
            Map<StatTypes, Integer> stats = new HashMap<>();

            stats.put(StatTypes.STRENGTH, getStatValue(statsObject, "strength", baseLevel));
            stats.put(StatTypes.AGILITY, getStatValue(statsObject, "agility", baseLevel));
            stats.put(StatTypes.VITALITY, getStatValue(statsObject, "vitality", baseLevel));
            stats.put(StatTypes.INTELLIGENCE, getStatValue(statsObject, "intelligence", baseLevel));
            stats.put(StatTypes.DEXTERITY, getStatValue(statsObject, "dexterity", baseLevel));
            stats.put(StatTypes.LUCK, getStatValue(statsObject, "luck", baseLevel));

            return stats;

        } catch (Exception e) {
            Mamy.LOGGER.warn("Error parsing stats for '{}', using default stats: {}", entityIdStr, e.getMessage());
            return createLevelBasedStats(Math.max(1, baseLevel));
        }
    }

    private Map<StatTypes, Integer> createLevelBasedStats(int level) {
        Map<StatTypes, Integer> stats = new HashMap<>();
        for (StatTypes statType : StatTypes.values()) {
            stats.put(statType, level);
        }
        return stats;
    }

    private int getStatValue(JsonObject statsObject, String statName, int defaultValue) {
        if (statsObject.has(statName)) {
            return Math.max(0, statsObject.get(statName).getAsInt()); // Minimum stat value of 0
        }
        return Math.max(1, defaultValue); // Default to base level or minimum 1
    }
}