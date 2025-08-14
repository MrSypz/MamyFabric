package com.sypztep.mamy.common.reloadlistener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.data.ItemDataEntry;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.util.ElementalAttributeRegistry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MamyElementalReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID = Mamy.id("elementator");

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        ItemDataEntry.clearAll();

        AtomicInteger loadedCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        manager.findAllResources("elementator", path -> path.getPath().endsWith(".json"))
                .forEach((identifier, resources) -> {
                    for (Resource resource : resources) {
                        try (InputStream stream = resource.getInputStream()) {
                            JsonObject object = JsonParser.parseReader(
                                    new JsonReader(new InputStreamReader(stream))
                            ).getAsJsonObject();

                            // Extract item ID from file path
                            String filePath = identifier.getPath();
                            String itemIdStr = filePath.substring(
                                    filePath.indexOf("/") + 1,
                                    filePath.length() - 5
                            ).replace("/", ":");

                            Identifier itemId = Identifier.of(itemIdStr);
                            Item item = Registries.ITEM.get(itemId);

                            // Skip if item doesn't exist
                            if (item == Registries.ITEM.get(Registries.ITEM.getDefaultId()) &&
                                    !itemId.equals(Registries.ITEM.getDefaultId())) {
                                Mamy.LOGGER.warn("Unknown item '{}' in file '{}'", itemIdStr, identifier);
                                errorCount.getAndIncrement();
                                continue;
                            }

                            if (!object.has("damageRatios") && !object.has("powerBudget")) {
                                Mamy.LOGGER.error("Missing 'damageRatios' and 'powerBudget' field in file '{}'", identifier);
                                errorCount.getAndIncrement();
                                continue;
                            }

                            double powerBudget = object.has("powerBudget") ? object.get("powerBudget").getAsDouble() : 1.0;

                            // Parse damage ratios
                            Map<RegistryEntry<EntityAttribute>, Double> damageRatios = parseDamageRatios(object);

                            ItemDataEntry entry = new ItemDataEntry(damageRatios, powerBudget);
                            ItemDataEntry.addEntry(item, entry);

                            loadedCount.getAndIncrement();

                            Mamy.LOGGER.debug("Loaded item data for '{}': powerBudget={}, damageRatios={}",
                                    itemIdStr, powerBudget, damageRatios);

                        } catch (NumberFormatException e) {
                            errorCount.getAndIncrement();
                            Mamy.LOGGER.error("Invalid number format in file '{}': {}", identifier, e.getMessage());
                        } catch (Exception e) {
                            errorCount.getAndIncrement();
                            Mamy.LOGGER.error("Failed to load item data from '{}': {}", identifier, e.getMessage());
                            Mamy.LOGGER.error("Exception details: ", e);
                        }
                    }
                });

        Mamy.LOGGER.info("Successfully loaded {} item entries with damage properties", loadedCount.get());
        if (errorCount.get() > 0) {
            Mamy.LOGGER.warn("Failed to load {} item entries due to errors", errorCount.get());
        }

        if (Mamy.LOGGER.isDebugEnabled()) {
            ItemDataEntry.ITEM_DATA_MAP.entrySet().stream()
                    .limit(5)
                    .forEach(entry -> {
                        Identifier id = Registries.ITEM.getId(entry.getKey());
                        ItemDataEntry itemEntry = entry.getValue();
                        Mamy.LOGGER.debug("Loaded: {} -> {}", id, itemEntry);
                    });
        }
    }

    private Map<RegistryEntry<EntityAttribute>, Double> parseDamageRatios(JsonObject object) {
        if (!object.has("damageRatios")) {
            return createDefaultDamageRatios();
        }

        JsonObject ratiosObject = object.getAsJsonObject("damageRatios");
        Map<RegistryEntry<EntityAttribute>, Double> ratios = new HashMap<>();

        // Use the registry to automatically handle all elements!
        Map<String, RegistryEntry<EntityAttribute>> elementMap = ElementalAttributeRegistry.getJsonToAttributeMap();

        for (Map.Entry<String, RegistryEntry<EntityAttribute>> entry : elementMap.entrySet()) {
            String elementName = entry.getKey();
            RegistryEntry<EntityAttribute> attribute = entry.getValue();

            if (ratiosObject.has(elementName)) {
                double ratio = ratiosObject.get(elementName).getAsDouble();
                if (ratio > 0) {
                    ratios.put(attribute, Math.max(0.0, ratio));
                }
            }
        }

        return normalizeRatios(ratios);
    }
    private Map<RegistryEntry<EntityAttribute>, Double> normalizeRatios(Map<RegistryEntry<EntityAttribute>, Double> ratios) {
        // If no ratios were found, use default
        if (ratios.isEmpty()) {
            Mamy.LOGGER.warn("No valid damage ratios found, using default");
            return createDefaultDamageRatios();
        }

        // Normalize ratios to sum to 1.0
        double totalRatio = ratios.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalRatio > 0) {
            Map<RegistryEntry<EntityAttribute>, Double> normalizedRatios = new HashMap<>();
            ratios.forEach((attribute, ratio) -> normalizedRatios.put(attribute, ratio / totalRatio));

            Mamy.LOGGER.debug("Normalized ratios - total was: {}, normalized: {}", totalRatio, normalizedRatios);
            return normalizedRatios;
        }

        return createDefaultDamageRatios();
    }
    private Map<RegistryEntry<EntityAttribute>, Double> createDefaultDamageRatios() {
        Map<RegistryEntry<EntityAttribute>, Double> defaultRatios = new HashMap<>();
        defaultRatios.put(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 1.0);
        return defaultRatios;
    }
}