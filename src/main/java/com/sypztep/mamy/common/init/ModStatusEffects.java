package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.statuseffect.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class ModStatusEffects {
    public static RegistryEntry<StatusEffect> STUN;
    public static RegistryEntry<StatusEffect> CARVE;
    public static RegistryEntry<StatusEffect> ENDURE;
    public static RegistryEntry<StatusEffect> INCREASE_AGILITY;
    public static RegistryEntry<StatusEffect> DECREASE_AGILITY;
    public static RegistryEntry<StatusEffect> BLESSING;
    public static RegistryEntry<StatusEffect> ANGELUS;
    public static RegistryEntry<StatusEffect> PROVOKE;
    public static RegistryEntry<StatusEffect> IMPROVE_CONCENTRATION;

    public static void initEffects() {
        STUN = init("stun", new StunEffect());
        CARVE = init("carve", new MarkEffect(StatusEffectCategory.HARMFUL).addAttributeModifier(EntityAttributes.GENERIC_ARMOR, Mamy.id("carve_status_effect"), -0.08D, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        ENDURE = init("endure", new EndureEffect(StatusEffectCategory.BENEFICIAL));
        INCREASE_AGILITY = init("increase_agility", new IncreaseAgilityEffect(StatusEffectCategory.BENEFICIAL));
        DECREASE_AGILITY = init("decrease_agility", new DecreaseAgilityEffect(StatusEffectCategory.HARMFUL));
        BLESSING = init("blessing", new BlessingEffect(StatusEffectCategory.BENEFICIAL));
        ANGELUS = init("angelus", new AngelusEffect(StatusEffectCategory.BENEFICIAL));
        PROVOKE = init("provoke", new ProvokeEffect(StatusEffectCategory.HARMFUL));
        IMPROVE_CONCENTRATION = init("improve_concentration", new ImproveConcentrationEffect(StatusEffectCategory.BENEFICIAL));
    }
    public static RegistryEntry<StatusEffect> init(String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Mamy.id(name), effect);
    }
}
