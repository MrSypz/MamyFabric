package com.sypztep.mamy.common.reloadlistener;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.item.WeightComponent;
import com.sypztep.mamy.common.data.ItemWeightEntry;
import com.sypztep.mamy.common.util.ReloadHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class MamyItemWeightReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID = Mamy.id(WeightComponent.RESOURCE_LOCATION);

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        ItemWeightEntry.ITEM_DATA_MAP.clear();

        ReloadHelper.processJsonResources(
                manager,
                WeightComponent.RESOURCE_LOCATION,
                object -> object.has("weight"),
                filePath -> Identifier.of(filePath.substring(filePath.indexOf("/") + 1, filePath.length() - 5).replace("/", ":")),
                Registries.ITEM::get,
                Registries.ITEM.get(Registries.ITEM.getDefaultId()),
                (item, json) -> {
                    float weight = json.get("weight").getAsFloat();
                    ItemWeightEntry entry = new ItemWeightEntry(weight);
                    ItemWeightEntry.ITEM_DATA_MAP.put(item, entry);

                    Mamy.LOGGER.debug("Loaded custom weight for '{}': {}",
                            Registries.ITEM.getId(item), weight);
                },
                "item"
        );

        Mamy.LOGGER.info("Loaded {} custom item weights", ItemWeightEntry.ITEM_DATA_MAP.size());
    }
}
