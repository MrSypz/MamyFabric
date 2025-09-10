package com.sypztep.mamy.common.util;

import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.LivingEntity;

import java.util.Map;

public class StatModifierHelper {

    public static void applyTemporaryModifier(LivingEntity entity, StatTypes statType, String source, short value, boolean refreshEffects) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.getNullable(entity);
        if (component == null) return;

        if (refreshEffects) {
            component.performBatchUpdate(() -> {
                component.getStatByType(statType).addTemporaryModifier(source, value);
                component.refreshStatEffectsInternal(statType);
            });
        } else {
            component.getStatByType(statType).addTemporaryModifier(source, value);
        }
    }

    public static void removeTemporaryModifier(LivingEntity entity, StatTypes statType, String source, boolean refreshEffects) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.getNullable(entity);
        if (component == null) return;

        if (refreshEffects) {
            component.performBatchUpdate(() -> {
                component.getStatByType(statType).removeTemporaryModifier(source);
                // Use existing refresh system
                component.refreshStatEffectsInternal(statType);
            });
        } else {
            component.getStatByType(statType).removeTemporaryModifier(source);
        }
    }

    public static void applyMultipleModifiers(LivingEntity entity, Map<StatTypes, Short> modifiers, String source) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.getNullable(entity);
        if (component == null) return;

        component.performBatchUpdate(() -> {
            for (Map.Entry<StatTypes, Short> entry : modifiers.entrySet()) {
                StatTypes statType = entry.getKey();
                Short value = entry.getValue();
                component.getStatByType(statType).addTemporaryModifier(source, value);
                component.refreshStatEffectsInternal(statType);
            }
        });
    }

    public static void removeAllModifiersFromSource(LivingEntity entity, String source) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.getNullable(entity);
        if (component == null) return;

        component.performBatchUpdate(() -> {
            for (StatTypes statType : StatTypes.values()) {
                if (component.getStatByType(statType).hasTemporaryModifier(source)) {
                    component.getStatByType(statType).removeTemporaryModifier(source);
                    component.refreshStatEffectsInternal(statType);
                }
            }
        });

        System.out.println("Finished removing source: " + source);
    }

    public static short getTemporaryModifier(LivingEntity entity, StatTypes statType, String source) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.getNullable(entity);
        if (component == null) return 0;

        return component.getStatByType(statType).getTemporaryModifier(source);
    }

    public static boolean hasTemporaryModifier(LivingEntity entity, StatTypes statType, String source) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.getNullable(entity);
        if (component == null) return false;

        return component.getStatByType(statType).hasTemporaryModifier(source);
    }
}