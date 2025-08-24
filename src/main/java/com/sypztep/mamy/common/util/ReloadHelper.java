package com.sypztep.mamy.common.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sypztep.mamy.Mamy;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ReloadHelper {

    /**
     * Generic JSON reload helper.
     *
     * @param manager        Resource manager from reload
     * @param folder         The folder under "data/namespace" (e.g. "itemweight")
     * @param validator      Predicate to check JSON has required fields
     * @param idExtractor    Function to turn file path into Identifier
     * @param registryGetter Function to resolve Identifier -> Registry object
     * @param defaultValue   Default fallback (for "unknown" entries)
     * @param processor      Consumer to handle (registryObject, json)
     * @param dataType       Name for logging (e.g. "item", "mob")
     */
    public static <T> void processJsonResources(
            ResourceManager manager,
            String folder,
            Predicate<JsonObject> validator,
            Function<String, Identifier> idExtractor,
            Function<Identifier, T> registryGetter,
            T defaultValue,
            BiConsumer<T, JsonObject> processor,
            String dataType
    ) {
        AtomicInteger loadedCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        manager.findAllResources(folder, path -> path.getPath().endsWith(".json"))
                .forEach((identifier, resources) -> {
                    for (Resource resource : resources) {
                        try (InputStream stream = resource.getInputStream()) {
                            JsonObject object = JsonParser.parseReader(
                                    new JsonReader(new InputStreamReader(stream))
                            ).getAsJsonObject();

                            // Build identifier from path
                            String filePath = identifier.getPath();
                            Identifier entryId = idExtractor.apply(filePath);

                            // Lookup in registry
                            T target = registryGetter.apply(entryId);

                            // Skip if missing
                            if (target == defaultValue && !entryId.equals(Registries.ITEM.getDefaultId())) {
                                Mamy.LOGGER.warn("Unknown {} '{}' in file '{}'", dataType, entryId, identifier);
                                errorCount.incrementAndGet();
                                continue;
                            }

                            // Validate JSON
                            if (!validator.test(object)) {
                                Mamy.LOGGER.error("Missing required fields in {} '{}'", dataType, identifier);
                                errorCount.incrementAndGet();
                                continue;
                            }

                            // Let caller handle JSON
                            processor.accept(target, object);
                            loadedCount.incrementAndGet();

                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            Mamy.LOGGER.error("Failed to load {} from '{}': {}", dataType, identifier, e.getMessage());
                            Mamy.LOGGER.error("Exception details: ", e);
                        }
                    }
                });

        Mamy.LOGGER.info("Successfully loaded {} {} entries", loadedCount.get(), dataType);
        if (errorCount.get() > 0) {
            Mamy.LOGGER.warn("Failed to load {} {} entries due to errors", errorCount.get(), dataType);
        }
    }
}
