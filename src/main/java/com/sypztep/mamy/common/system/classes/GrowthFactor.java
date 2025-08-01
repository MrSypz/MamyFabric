package com.sypztep.mamy.common.system.classes;

public record GrowthFactor(double flatPerLevel, double percentPerLevel) {
    public static GrowthFactor flat(double flatPerLevel) {
        return new GrowthFactor(flatPerLevel, 0.0);
    }

    public static GrowthFactor percent(double percentPerLevel) {
        return new GrowthFactor(0.0, percentPerLevel);
    }

    public static GrowthFactor both(double flatPerLevel, double percentPerLevel) {
        return new GrowthFactor(flatPerLevel, percentPerLevel);
    }

    public double calculateGrowth(double baseValue, int level) {
        return (flatPerLevel * level) + (baseValue * percentPerLevel * level);
    }
}