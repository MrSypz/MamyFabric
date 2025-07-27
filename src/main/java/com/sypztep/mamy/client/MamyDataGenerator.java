package com.sypztep.mamy.client;

import com.sypztep.mamy.client.provider.ModDamageTypeTagProvider;
import com.sypztep.mamy.client.provider.ModLanguageProvider;
import com.sypztep.mamy.client.provider.ModMobDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class MamyDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(ModLanguageProvider::new);
        pack.addProvider(ModMobDataGenerator::new);
        pack.addProvider(ModDamageTypeTagProvider::new);
    }
}
