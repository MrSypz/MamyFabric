package com.sypztep.mamy.common.reloadlistener;

import com.google.gson.JsonObject;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.data.ItemElementDataEntry;
import com.sypztep.mamy.common.system.damage.ElementType;
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

    private static final Identifier ID = Mamy.id("elementator");

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        ItemElementDataEntry.ITEM_DATA_MAP.clear();

        ReloadHelper.processJsonResources(
                manager,
                "elementator",
                object -> object.has("damageRatios") || object.has("powerBudget"),
                filePath -> Identifier.of(filePath.substring(filePath.indexOf("/") + 1, filePath.length() - 5).replace("/", ":")),
                Registries.ITEM::get,
                Registries.ITEM.get(Registries.ITEM.getDefaultId()),
                (item, json) -> {
                    double powerBudget = json.has("powerBudget") ? json.get("powerBudget").getAsDouble() : 1.0;
                    Map<RegistryEntry<EntityAttribute>, Double> damageRatios = parseDamageRatios(json, item);
                    ItemElementDataEntry entry = new ItemElementDataEntry(damageRatios, powerBudget);
                    ItemElementDataEntry.ITEM_DATA_MAP.put(item, entry);
                },
                "item"
        );
    }

    private Map<RegistryEntry<EntityAttribute>, Double> parseDamageRatios(JsonObject object, Item item) {
        if (!object.has("damageRatios")) {
            return createDefaultDamageRatios(item);
        }

        JsonObject ratiosObject = object.getAsJsonObject("damageRatios");
        Map<RegistryEntry<EntityAttribute>, Double> ratios = new HashMap<>();
        boolean isArmor = item instanceof net.minecraft.item.ArmorItem;

        Mamy.LOGGER.debug("Processing {} as {}", Registries.ITEM.getId(item), isArmor ? "ARMOR" : "WEAPON");

        // Use the consolidated ElementType for all mappings
        for (ElementType elementType : ElementType.values()) {
            String elementName = elementType.name;
            RegistryEntry<EntityAttribute> attribute = isArmor ? elementType.resistance : elementType.damageFlat;

            if (ratiosObject.has(elementName)) {
                double ratio = ratiosObject.get(elementName).getAsDouble();

                // For armor, allow negative values (vulnerabilities)
                // For weapons, filter out negative values
                if (isArmor || ratio > 0) {
                    ratios.put(attribute, ratio);
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
        if (ratios.isEmpty()) {
            Mamy.LOGGER.warn("No valid damage ratios found, using default");
            return createDefaultDamageRatios(item);
        }

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
            defaultRatios.put(ElementType.PHYSICAL.damageFlat, 1.0);
            Mamy.LOGGER.debug("Default physical damage for weapon: {}", Registries.ITEM.getId(item));
        }
        return defaultRatios;
    }
}