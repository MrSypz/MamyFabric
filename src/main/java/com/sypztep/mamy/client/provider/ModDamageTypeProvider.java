package com.sypztep.mamy.client.provider;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModDamageTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModDamageTypeProvider extends FabricDynamicRegistryProvider {

    public ModDamageTypeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        entries.add(ModDamageTypes.BLOODLUST, new DamageType("bloodlust", 0.1f));
        entries.add(ModDamageTypes.DOUBLE_ATTACK, new DamageType("double_attack",0.1f));
    }
    @Override
    public String getName() {
        return Mamy.MODID + " Damage Types";
    }
}