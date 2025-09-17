package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.damage.DamageComponent;
import com.sypztep.mamy.common.system.damage.HybridDamageSource;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModDamageTypes {
    public static final RegistryKey<DamageType> ARROW_RAIN = createType("arrow_rain");
    public static final RegistryKey<DamageType> DOUBLE_ATTACK = createType("double_attack");
    public static final RegistryKey<DamageType> ENERGY_BREAK = createType("energy_break");
    public static final RegistryKey<DamageType> BASHING_BLOW = createType("bashing_blow");
    public static final RegistryKey<DamageType> MAGIC_ARROW = createType("magic_arrow");
    public static final RegistryKey<DamageType> HOLY = createType("holy");
    public static final RegistryKey<DamageType> FIREBALL = createType("fireball");
    public static final RegistryKey<DamageType> SHOCKWAVE_FLAME = createType("shockwave_flame");
    public static final RegistryKey<DamageType> LIGHTING = createType("lighting");

    private static RegistryKey<DamageType> createType(String name) {
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

    public static class HybridSkillDamageSource extends DamageSource implements HybridDamageSource {
        private final Skill skill;

        public HybridSkillDamageSource(RegistryEntry.Reference<DamageType> type, LivingEntity attacker, Skill skill) {
            super(type, attacker);
            this.skill = skill;
        }

        @Override
        public List<DamageComponent> getDamageComponents() {
            return skill.getDamageComponents();
        }
        public static HybridSkillDamageSource create(World world, RegistryKey<DamageType> key, LivingEntity attacker, Skill skill) {
            return new HybridSkillDamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), attacker, skill);
        }
    }
}
