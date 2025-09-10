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
        entries.add(ModDamageTypes.ARROW_RAIN, new DamageType("arrow_rain", 0.1f));
        entries.add(ModDamageTypes.DOUBLE_ATTACK, new DamageType("double_attack",0.1f));
        entries.add(ModDamageTypes.FIRE_DAMAGE, new DamageType("fire_attack",0.1f));
        entries.add(ModDamageTypes.BASHING_BLOW, new DamageType("bashing_blow",0.1f));
        entries.add(ModDamageTypes.HOLY, new DamageType("holy",0.1f));
        entries.add(ModDamageTypes.MAGIC_ARROW, new DamageType("magic_arrow",0.1f));
    }
    @Override
    public String getName() {
        return Mamy.MODID + " Damage Types";
    }
}