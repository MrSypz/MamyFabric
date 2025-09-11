package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.statuseffect.*;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class ModStatusEffects {
    public static RegistryEntry<StatusEffect> ENDURE;
    public static RegistryEntry<StatusEffect> INCREASE_AGILITY;
    public static RegistryEntry<StatusEffect> DECREASE_AGILITY;
    public static RegistryEntry<StatusEffect> BLESSING;
    public static RegistryEntry<StatusEffect> ANGELUS;
    public static RegistryEntry<StatusEffect> PROVOKE;
    public static RegistryEntry<StatusEffect> IMPROVE_CONCENTRATION;
    public static RegistryEntry<StatusEffect> ENVENOM_WEAPON;
    public static RegistryEntry<StatusEffect> HIDING;

    public static void init() {
        ENDURE = init("endure", new EndureEffect(StatusEffectCategory.BENEFICIAL));
        INCREASE_AGILITY = init("increase_agility", new IncreaseAgilityEffect(StatusEffectCategory.BENEFICIAL));
        DECREASE_AGILITY = init("decrease_agility", new DecreaseAgilityEffect(StatusEffectCategory.HARMFUL));
        BLESSING = init("blessing", new BlessingEffect(StatusEffectCategory.BENEFICIAL));
        ANGELUS = init("angelus", new AngelusEffect(StatusEffectCategory.BENEFICIAL));
        PROVOKE = init("provoke", new ProvokeEffect(StatusEffectCategory.HARMFUL));
        IMPROVE_CONCENTRATION = init("improve_concentration", new ImproveConcentrationEffect(StatusEffectCategory.BENEFICIAL));
        ENVENOM_WEAPON = init("envenom_weapon", new EnvenomWeaponStatusEffect(StatusEffectCategory.BENEFICIAL));
        HIDING = init("hiding", new HidingStatusEffect(StatusEffectCategory.BENEFICIAL));
    }
    public static RegistryEntry<StatusEffect> init(String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Mamy.id(name), effect);
    }
}