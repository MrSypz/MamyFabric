package com.sypztep.mamy.common.reloadlistener;


import com.google.gson.JsonObject;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.item.ElementalComponent;
import com.sypztep.mamy.common.data.ItemElementDataEntry;
import com.sypztep.mamy.common.system.damage.ElementType;
import com.sypztep.mamy.common.system.damage.CombatType;
import com.sypztep.mamy.common.util.ReloadHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class MamyElementalReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID = Mamy.id(ElementalComponent.RESOURCE_LOCATION);

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        ItemElementDataEntry.ITEM_DATA_MAP.clear();

        ReloadHelper.processJsonResources(
                manager,
                ElementalComponent.RESOURCE_LOCATION,
                object -> object.has("elementalRatios") || object.has("combatRatios") ||
                        object.has("damageRatios") || object.has("powerBudget"), // Backward compatibility
                filePath -> Identifier.of(filePath.substring(filePath.indexOf("/") + 1, filePath.length() - 5).replace("/", ":")),
                Registries.ITEM::get,
                Registries.ITEM.get(Registries.ITEM.getDefaultId()),
                (item, json) -> {
                    double powerBudget = json.has("powerBudget") ? json.get("powerBudget").getAsDouble() : 1.0;
                    double combatWeight = json.has("combatWeight") ? json.get("combatWeight").getAsDouble() : 1.0;

                    Map<RegistryEntry<EntityAttribute>, Double> elementalRatios = parseElementalRatios(json, item);
                    Map<RegistryEntry<EntityAttribute>, Double> combatRatios = parseCombatRatios(json, item);

                    ItemElementDataEntry entry = new ItemElementDataEntry(elementalRatios, combatRatios, powerBudget, combatWeight);
                    ItemElementDataEntry.ITEM_DATA_MAP.put(item, entry);

                    Mamy.LOGGER.info("Loaded element data for {}: {}", Registries.ITEM.getId(item), entry);
                },
                "item"
        );
    }

    private Map<RegistryEntry<EntityAttribute>, Double> parseElementalRatios(JsonObject object, Item item) {
        JsonObject ratiosObject = null;

        // Check new format first, then fallback to legacy
        if (object.has("elementalRatios")) {
            ratiosObject = object.getAsJsonObject("elementalRatios");
        } else if (object.has("damageRatios")) {
            ratiosObject = object.getAsJsonObject("damageRatios"); // Legacy support
        }

        if (ratiosObject == null) {
            return createDefaultElementalRatios(item);
        }

        Map<RegistryEntry<EntityAttribute>, Double> ratios = new HashMap<>();
        boolean isArmor = item instanceof net.minecraft.item.ArmorItem;

        Mamy.LOGGER.debug("Processing {} elemental ratios as {}", Registries.ITEM.getId(item), isArmor ? "ARMOR" : "WEAPON");

        // Parse elemental ratios using ElementType
        for (ElementType elementType : ElementType.values()) {
            String elementName = elementType.name;
            RegistryEntry<EntityAttribute> attribute = isArmor ? elementType.resistance : elementType.damageFlat;

            if (ratiosObject.has(elementName)) {
                double ratio = ratiosObject.get(elementName).getAsDouble();

                // For armor, allow negative values (vulnerabilities)
                // For weapons, filter out negative values
                if (isArmor || ratio > 0) {
                    ratios.put(attribute, ratio);
                    logger(elementName,attribute.value().getTranslationKey(), ratio);
                } else {
                    Mamy.LOGGER.debug("  Skipping negative weapon ratio: {} = {}", elementName, ratio);
                }
            }
        }

        // Only normalize weapon ratios, not armor resistances
        return isArmor ? ratios : normalizeRatios(ratios, item, "elemental");
    }

    private Map<RegistryEntry<EntityAttribute>, Double> parseCombatRatios(JsonObject object, Item item) {
        if (!object.has("combatRatios")) {
            return Map.of(); // No combat ratios defined
        }

        JsonObject ratiosObject = object.getAsJsonObject("combatRatios");
        Map<RegistryEntry<EntityAttribute>, Double> ratios = new HashMap<>();
        boolean isArmor = item instanceof net.minecraft.item.ArmorItem;

        Mamy.LOGGER.debug("Processing {} combat ratios as {}", Registries.ITEM.getId(item), isArmor ? "ARMOR" : "WEAPON");

        // Parse combat ratios using CombatType
        for (CombatType combatType : CombatType.values()) {
            if (!combatType.hasAttributes()) continue; // Skip HYBRID and PURE

            String combatName = combatType.name.toLowerCase();
            RegistryEntry<EntityAttribute> attribute = isArmor ? combatType.resistance : combatType.damageFlat;

            if (ratiosObject.has(combatName)) {
                double ratio = ratiosObject.get(combatName).getAsDouble();

                if (isArmor || ratio > 0) {
                    ratios.put(attribute, ratio);
                    logger(combatName,attribute.value().getTranslationKey(), ratio);
                }
            }
        }

        return isArmor ? ratios : normalizeRatios(ratios, item, "combat");
    }

    private Map<RegistryEntry<EntityAttribute>, Double> normalizeRatios(Map<RegistryEntry<EntityAttribute>, Double> ratios, Item item, String type) {
        if (ratios.isEmpty()) {
            Mamy.LOGGER.debug("No {} ratios found for {}", type, Registries.ITEM.getId(item));
            return type.equals("elemental") ? createDefaultElementalRatios(item) : Map.of();
        }

        double totalRatio = ratios.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalRatio > 0) {
            Map<RegistryEntry<EntityAttribute>, Double> normalizedRatios = new HashMap<>();
            ratios.forEach((attribute, ratio) -> normalizedRatios.put(attribute, ratio / totalRatio));

            Mamy.LOGGER.debug("Normalized {} ratios - total was: {}, normalized: {}", type, totalRatio, normalizedRatios);
            return normalizedRatios;
        }

        return type.equals("elemental") ? createDefaultElementalRatios(item) : Map.of();
    }

    private Map<RegistryEntry<EntityAttribute>, Double> createDefaultElementalRatios(Item item) {
        Map<RegistryEntry<EntityAttribute>, Double> defaultRatios = new HashMap<>();
        boolean isArmor = item instanceof net.minecraft.item.ArmorItem;

        if (isArmor) {
            Mamy.LOGGER.debug("No default elemental resistances for armor item: {}", Registries.ITEM.getId(item));
        } else {
            defaultRatios.put(ElementType.PHYSICAL.damageFlat, 1.0);
            Mamy.LOGGER.debug("Default physical damage for weapon: {}", Registries.ITEM.getId(item));
        }
        return defaultRatios;
    }
    private void logger(String s, String ss, double d) {
        Mamy.LOGGER.debug("  {} -> {} = {}", s,ss, d);
    }
}