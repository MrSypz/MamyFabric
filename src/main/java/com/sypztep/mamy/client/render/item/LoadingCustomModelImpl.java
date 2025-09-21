package com.sypztep.mamy.client.render.item;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModItems;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
public class LoadingCustomModelImpl implements ModelLoadingPlugin {
    @Override
    public void onInitializeModelLoader(Context context) {
        for (Item item : ModItems.CUSTOM_RENDER) {
            String weaponName = Registries.ITEM.getId(item).getPath();
            for (String getstyle : DynamicItemRenderer.style) {
                context.addModels(ModelIdentifier.ofInventoryVariant(Mamy.id("item/" + weaponName + getstyle)).id());
            }
        }
    }
}