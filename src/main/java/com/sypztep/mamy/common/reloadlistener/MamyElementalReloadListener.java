package com.sypztep.mamy.common.reloadlistener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.data.ItemElementDataEntry;
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
        ItemElementDataEntry.ITEM_DATA_MAP.clear();

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
                            Map<RegistryEntry<EntityAttribute>, Double> damageRatios = parseDamageRatios(object, item);
                            ItemElementDataEntry entry = new ItemElementDataEntry(damageRatios, powerBudget);
                            ItemElementDataEntry.ITEM_DATA_MAP.put(item, entry);

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
            ItemElementDataEntry.ITEM_DATA_MAP.entrySet().stream()
                    .limit(5)
                    .forEach(entry -> {
                        Identifier id = Registries.ITEM.getId(entry.getKey());
                        ItemElementDataEntry itemEntry = entry.getValue();
                        Mamy.LOGGER.debug("Loaded: {} -> {}", id, itemEntry);
                    });
        }
    }

    private Map<RegistryEntry<EntityAttribute>, Double> parseDamageRatios(JsonObject object, Item item) {
        if (!object.has("damageRatios")) {
            return createDefaultDamageRatios(item);
        }

        JsonObject ratiosObject = object.getAsJsonObject("damageRatios");
        Map<RegistryEntry<EntityAttribute>, Double> ratios = new HashMap<>();

        // Determine if this is armor or weapon
        boolean isArmor = item instanceof net.minecraft.item.ArmorItem;

        // Get the appropriate attribute map based on item type
        Map<String, RegistryEntry<EntityAttribute>> elementMap = isArmor
                ? ElementalAttributeRegistry.getArmorAttributeMap()
                : ElementalAttributeRegistry.getWeaponAttributeMap();

        Mamy.LOGGER.debug("Processing {} as {}", Registries.ITEM.getId(item), isArmor ? "ARMOR" : "WEAPON");
        Mamy.LOGGER.debug("Available JSON keys: {}", elementMap.keySet());

        for (Map.Entry<String, RegistryEntry<EntityAttribute>> entry : elementMap.entrySet()) {
            String elementName = entry.getKey();
            RegistryEntry<EntityAttribute> attribute = entry.getValue();

            if (ratiosObject.has(elementName)) {
                double ratio = ratiosObject.get(elementName).getAsDouble();

                // ✅ For armor, allow negative values (vulnerabilities)
                // ✅ For weapons, filter out negative values
                if (isArmor || ratio > 0) {
                    ratios.put(attribute, ratio); // Don't clamp armor values!
                    Mamy.LOGGER.debug("  {} -> {} = {}", elementName,
                            attribute.value().getTranslationKey(), ratio);
                } else {
                    Mamy.LOGGER.debug("  Skipping negative weapon ratio: {} = {}", elementName, ratio);
                }
            }
        }

        // Only normalize weapon ratios, not armor resistances
        return isArmor ? ratios : normalizeRatios(ratios, item);
    }

    private Map<RegistryEntry<EntityAttribute>, Double> normalizeRatios(Map<RegistryEntry<EntityAttribute>, Double> ratios, Item item) {
        // If no ratios were found, use default
        if (ratios.isEmpty()) {
            Mamy.LOGGER.warn("No valid damage ratios found, using default");
            return createDefaultDamageRatios(item);
        }

        // Normalize ratios to sum to 1.0
        double totalRatio = ratios.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalRatio > 0) {
            Map<RegistryEntry<EntityAttribute>, Double> normalizedRatios = new HashMap<>();
            ratios.forEach((attribute, ratio) -> normalizedRatios.put(attribute, ratio / totalRatio));

            Mamy.LOGGER.debug("Normalized ratios - total was: {}, normalized: {}", totalRatio, normalizedRatios);
            return normalizedRatios;
        }

        return createDefaultDamageRatios(item);
    }

    private Map<RegistryEntry<EntityAttribute>, Double> createDefaultDamageRatios(Item item) {
        Map<RegistryEntry<EntityAttribute>, Double> defaultRatios = new HashMap<>();

        boolean isArmor = item instanceof net.minecraft.item.ArmorItem;

        if (isArmor) {
            Mamy.LOGGER.debug("No default resistances for armor item: {}", Registries.ITEM.getId(item));
        } else {
            defaultRatios.put(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 1.0);
            Mamy.LOGGER.debug("Default physical damage for weapon: {}", Registries.ITEM.getId(item));
        }
        return defaultRatios; // Empty map
    }
}