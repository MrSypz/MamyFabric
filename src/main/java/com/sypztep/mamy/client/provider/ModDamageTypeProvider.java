package com.sypztep.mamy.client.provider;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModDamageTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.entity.damage.DamageEffects;
import net.minecraft.entity.damage.DamageScaling;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DeathMessageType;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModDamageTypeProvider extends FabricDynamicRegistryProvider {

    public ModDamageTypeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        // Fire skill damage type
        entries.add(ModDamageTypes.WHIRLWIND_SLASH,make("whirlwind_slash", 0.1f));
        entries.add(ModDamageTypes.PRECISE_STRIKE, new DamageType("precise_strike",0.1f));
        entries.add(ModDamageTypes.BLOODLUST, new DamageType("bloodlust", 0.1f));
        entries.add(ModDamageTypes.DOUBLE_ATTACK, new DamageType("double_attack",0.1f));
    }
    public DamageType make(String name, float exhaust) {
        return new DamageType(name, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, exhaust,DamageEffects.HURT, DeathMessageType.DEFAULT);
    }
    @Override
    public String getName() {
        return Mamy.MODID + " Damage Types";
    }
}