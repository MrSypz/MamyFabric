package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.common.init.ModClasses;

import java.util.*;

public class ClassRegistry {

    public static PlayerClass getClass(String id) {
        return ModClasses.CLASSES.get(id);
    }

    public static Collection<PlayerClass> getAllClasses() {
        return ModClasses.CLASSES.values();
    }

    public static List<PlayerClass> getClassesByTier(int tier) {
        return ModClasses.CLASSES.values().stream()
                .filter(clazz -> clazz.getTier() == tier)
                .sorted(Comparator.comparingInt(PlayerClass::getBranch))
                .toList();
    }

    public static List<PlayerClass> getTranscendentClasses() {
        return ModClasses.CLASSES.values().stream()
                .filter(PlayerClass::isTranscendent)
                .toList();
    }

    public static List<PlayerClass> getAvailableEvolutions(PlayerClass currentClass) {
        return ModClasses.CLASSES.values().stream()
                .filter(clazz -> clazz != currentClass)
                .filter(clazz -> clazz.getTier() > currentClass.getTier()) // ONLY HIGHER TIERS
                .filter(clazz -> !clazz.isTranscendent()) // Exclude transcendent from normal evolutions
                .toList();
    }

    public static List<PlayerClass> getAvailableTranscendence(PlayerClass currentClass, int classLevel) {
        return ModClasses.CLASSES.values().stream()
                .filter(clazz -> clazz.canTranscendFrom(currentClass, classLevel))
                .filter(clazz -> clazz.getTier() > currentClass.getTier()) // ONLY HIGHER TIERS
                .filter(PlayerClass::isTranscendent) // Only transcendent classes
                .toList();
    }
    public static PlayerClass getDefaultClass() {
        return ModClasses.NOVICE;
    }
}