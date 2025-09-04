package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ModDamageTypes {
    public static final RegistryKey<DamageType> ARROW_RAIN = createType("arrow_rain");
    public static final RegistryKey<DamageType> DOUBLE_ATTACK = createType("double_attack");
    public static final RegistryKey<DamageType> FIRE_DAMAGE = createType("fire_damage");
    public static final RegistryKey<DamageType> BASHING_BLOW = createType("bashing_blow");
    public static final RegistryKey<DamageType> HOLY = createType("holy");
    public static RegistryKey<DamageType> createType(String name) {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id(name));
    }

    public static DamageSource create(World world, RegistryKey<DamageType> key, @Nullable Entity source, @Nullable Entity attacker) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), source, attacker);
    }

    public static DamageSource create(World world, RegistryKey<DamageType> key, @Nullable Entity attacker) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), attacker);
    }

    public static DamageSource create(World world, RegistryKey<DamageType> key) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key));
    }
}
