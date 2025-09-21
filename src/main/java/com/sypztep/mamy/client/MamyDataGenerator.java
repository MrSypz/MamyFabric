package com.sypztep.mamy.client;

import com.sypztep.mamy.client.provider.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class MamyDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(ModLanguageProvider::new);
        pack.addProvider(ModMobDataProvider::new);
        pack.addProvider(ModDamageTypeProvider::new);
        pack.addProvider(ModDamageTypeTagProvider::new);
        pack.addProvider(ModEntityTypeTagProvider::new);
        pack.addProvider(ModItemWeightProvider::new);
        pack.addProvider(ModItemTagProvider::new);
        pack.addProvider(ModElementalProvider::new);
        pack.addProvider(ModModelProvider::new);
    }
}
