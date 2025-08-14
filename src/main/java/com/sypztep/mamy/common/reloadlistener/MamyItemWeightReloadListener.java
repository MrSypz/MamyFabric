package com.sypztep.mamy.common.reloadlistener;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.ItemWeight;
import com.sypztep.mamy.common.data.ItemWeightEntry;
import com.sypztep.mamy.common.init.ModDataComponents;
import com.sypztep.mamy.common.util.NumberUtil;
import com.sypztep.mamy.common.util.ReloadHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class MamyItemWeightReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID = Mamy.id("itemweight");

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        ItemWeightEntry.ITEM_DATA_MAP.clear();

        ReloadHelper.processJsonResources(
                manager,
                "itemweight",
                object -> object.has("value"), // validator
                filePath -> Identifier.of(filePath.substring(filePath.indexOf("/") + 1, filePath.length() - 5).replace("/", ":")),
                Registries.ITEM::get,
                Registries.ITEM.get(Registries.ITEM.getDefaultId()),
                (item, json) -> {
                    float value = json.get("value").getAsFloat();
                    NumberUtil.WeightUnit unit = json.has("unit") ? NumberUtil.WeightUnit.valueOf(json.get("unit").getAsString().toUpperCase()) : NumberUtil.WeightUnit.GRAM;

                    ItemWeight weight = new ItemWeight(value, unit);
                    ItemWeightEntry entry = new ItemWeightEntry(weight.toGrams()); // store in grams internally
                    ItemWeightEntry.ITEM_DATA_MAP.put(item, entry);

                    ItemStack dummy = new ItemStack(item);
                    dummy.set(ModDataComponents.ITEM_WEIGHT_COMPONENT_TYPE, weight);

                    Mamy.LOGGER.debug("Loaded item weight for '{}': {} {}", Registries.ITEM.getId(item), value, unit);
                },
                "item"
        );
    }
}
