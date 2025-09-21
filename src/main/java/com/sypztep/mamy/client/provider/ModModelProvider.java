package com.sypztep.mamy.client.provider;

import com.google.gson.JsonObject;
import com.sypztep.mamy.Mamy;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import org.jetbrains.annotations.Nullable;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        generateDualItemModel(itemModelGenerator, "novice_dagger");
        generateDualItemModel(itemModelGenerator, "darksteel_mace");
    }

    private void generateDualItemModel(ItemModelGenerator itemModelGenerator, String name) {
        // gen <name>.json
        itemModelGenerator.writer.accept(
                Mamy.id("item/" + name),
                () -> modelJson("builtin/entity", null)
        );

        // gen <name>_gui.json
        itemModelGenerator.writer.accept(
                Mamy.id("item/" + name + "_gui"),
                () -> modelJson("item/handheld", "mamy:item/" + name)
        );
    }

    private JsonObject modelJson(String parent, @Nullable String texture) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", parent);
        if ("builtin/entity".equals(parent))
            json.addProperty("gui_light", "front");

        if (texture != null) {
            JsonObject textures = new JsonObject();
            textures.addProperty("layer0", texture);
            json.add("textures", textures);
        }
        return json;
    }
}
