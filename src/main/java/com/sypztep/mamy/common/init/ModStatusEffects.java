package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.statuseffect.*;
import com.sypztep.mamy.common.system.crowdcontrol.CrowdControlManager.CrowdControlType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
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

    public static RegistryEntry<StatusEffect> KNOCKDOWN;
    public static RegistryEntry<StatusEffect> BOUND;
    public static RegistryEntry<StatusEffect> STUN;
    public static RegistryEntry<StatusEffect> STIFFNESS;
    public static RegistryEntry<StatusEffect> FREEZING;
    public static RegistryEntry<StatusEffect> KNOCKBACK;
    public static RegistryEntry<StatusEffect> FLOATING;
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

        KNOCKDOWN = init("knockdown", new CrowdControlStatusEffect(StatusEffectCategory.HARMFUL, CrowdControlType.KNOCKDOWN, CrowdControlType.KNOCKDOWN.getCcPoints()));
        BOUND = init("bound", new CrowdControlStatusEffect(StatusEffectCategory.HARMFUL, CrowdControlType.BOUND, CrowdControlType.BOUND.getCcPoints()));
        STUN = init("stun", new CrowdControlStatusEffect(StatusEffectCategory.HARMFUL, CrowdControlType.STUN, CrowdControlType.STUN.getCcPoints()));
        STIFFNESS = init("stiffness", new CrowdControlStatusEffect(StatusEffectCategory.HARMFUL, CrowdControlType.STIFFNESS, CrowdControlType.STIFFNESS.getCcPoints()));
        FREEZING = init("freezing", new CrowdControlStatusEffect(StatusEffectCategory.HARMFUL, CrowdControlType.FREEZING, CrowdControlType.FREEZING.getCcPoints()));
        KNOCKBACK = init("knockback", new CrowdControlStatusEffect(StatusEffectCategory.HARMFUL, CrowdControlType.KNOCKBACK, CrowdControlType.KNOCKBACK.getCcPoints()));
        FLOATING = init("floating", new CrowdControlStatusEffect(StatusEffectCategory.HARMFUL, CrowdControlType.FLOATING, CrowdControlType.FLOATING.getCcPoints()));
        ENVENOM_WEAPON = init("envenom_weapon", new EnvenomWeaponStatusEffect(StatusEffectCategory.BENEFICIAL));
        HIDING = init("hiding", new HidingStatusEffect(StatusEffectCategory.BENEFICIAL));
    }
    public static RegistryEntry<StatusEffect> init(String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Mamy.id(name), effect);
    }
}