package com.sypztep.mamy.client.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.sypztep.mamy.Mamy;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public abstract class MamyCodecDataProvider<T> implements DataProvider {

    protected final FabricDataOutput dataOutput;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture;
    private final Codec<T> codec;
    private final String folderName;

    protected MamyCodecDataProvider(FabricDataOutput dataOutput,
                                    CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture,
                                    String folderName,
                                    Codec<T> codec) {
        this.dataOutput = dataOutput;
        this.registriesFuture = registriesFuture;
        this.folderName = folderName;
        this.codec = codec;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return this.registriesFuture.thenCompose(lookup -> {
            Map<Identifier, JsonElement> entries = new HashMap<>();
            DynamicOps<JsonElement> ops = lookup.getOps(JsonOps.INSTANCE);

            BiConsumer<Identifier, T> provider = (id, value) -> {
                JsonElement json = this.convert(id, value, ops);
                JsonElement existingJson = entries.put(id, json);

                if (existingJson != null) {
                    throw new IllegalArgumentException("Duplicate entry " + id);
                }
            };

            this.configure(provider, lookup);
            return this.write(writer, entries);
        });
    }

    protected abstract void configure(BiConsumer<Identifier, T> provider, RegistryWrapper.WrapperLookup lookup);

    private JsonElement convert(Identifier id, T value, DynamicOps<JsonElement> ops) {
        return this.codec.encodeStart(ops, value)
                .mapError(message -> "Invalid entry %s: %s".formatted(id, message))
                .getOrThrow();
    }

    private CompletableFuture<?> write(DataWriter writer, Map<Identifier, JsonElement> entries) {
        return CompletableFuture.allOf(entries.entrySet().stream().map(entry -> {
            Path path = this.getCustomPath(entry.getKey());
            return DataProvider.writeToPath(writer, entry.getValue(), path);
        }).toArray(CompletableFuture[]::new));
    }

    private Path getCustomPath(Identifier id) {
        return this.dataOutput.getPath()
                .resolve("data")
                .resolve(Mamy.MODID)
                .resolve(folderName)
                .resolve(id.getNamespace())
                .resolve(id.getPath() + ".json");
    }
}