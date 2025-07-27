package com.sypztep.mamy.common.reloadlistener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.data.MobExpEntry;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MamyMobExpReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID = Mamy.id("mobexp");

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        MobExpEntry.clearAll();

        AtomicInteger loadedCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        manager.findAllResources("mobexp", path -> path.getPath().endsWith(".json"))
                .forEach((identifier, resources) -> {
                    for (Resource resource : resources) {
                        try (InputStream stream = resource.getInputStream()) {
                            JsonObject object = JsonParser.parseReader(
                                    new JsonReader(new InputStreamReader(stream))
                            ).getAsJsonObject();

                            // Extract entity ID from file path
                            String filePath = identifier.getPath();
                            String entityIdStr = filePath.substring(
                                    filePath.indexOf("/") + 1,
                                    filePath.length() - 5
                            ).replace("/", ":");

                            Identifier entityId = Identifier.of(entityIdStr);
                            EntityType<?> entityType = Registries.ENTITY_TYPE.get(entityId);

                            // Skip if entity type doesn't exist
                            if (entityType == Registries.ENTITY_TYPE.get(Registries.ENTITY_TYPE.getDefaultId()) &&
                                    !entityId.equals(Registries.ENTITY_TYPE.getDefaultId())) {
                                Mamy.LOGGER.warn("Unknown entity type '{}' in file '{}'", entityIdStr, identifier);
                                errorCount.getAndIncrement();
                                continue;
                            }

                            if (!object.has("expReward")) {
                                Mamy.LOGGER.error("Missing 'expReward' field in file '{}'", identifier);
                                errorCount.getAndIncrement();
                                continue;
                            }

                            int expReward = object.get("expReward").getAsInt();

                            int baseLevel = object.has("baseLevel") ? object.get("baseLevel").getAsInt() : 1;

                            Map<StatTypes, Integer> stats = parseStats(object, entityIdStr, baseLevel);

                            MobExpEntry entry = new MobExpEntry(expReward, baseLevel, stats);
                            MobExpEntry.addEntry(entityType, entry);

                            loadedCount.getAndIncrement();

                            Mamy.LOGGER.debug("Loaded mob data for entity '{}': expReward={}, baseLevel={}, stats={}",
                                    entityIdStr, expReward, baseLevel, stats);

                        } catch (NumberFormatException e) {
                            errorCount.getAndIncrement();
                            Mamy.LOGGER.error("Invalid number format in file '{}': {}", identifier, e.getMessage());
                        } catch (Exception e) {
                            errorCount.getAndIncrement();
                            Mamy.LOGGER.error("Failed to load mob data from '{}': {}", identifier, e.getMessage());
                            Mamy.LOGGER.error("Exception details: ", e);
                        }
                    }
                });

        Mamy.LOGGER.info("Successfully loaded {} mob entries with stats and levels", loadedCount.get());
        if (errorCount.get() > 0) {
            Mamy.LOGGER.warn("Failed to load {} mob entries due to errors", errorCount.get());
        }

        if (Mamy.LOGGER.isDebugEnabled()) {
            MobExpEntry.MOBEXP_MAP.entrySet().stream()
                    .limit(5)
                    .forEach(entry -> {
                        Identifier id = Registries.ENTITY_TYPE.getId(entry.getKey());
                        MobExpEntry mobEntry = entry.getValue();
                        Mamy.LOGGER.debug("Loaded: {} -> {}", id, mobEntry);
                    });
        }
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