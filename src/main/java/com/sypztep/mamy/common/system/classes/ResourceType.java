package com.sypztep.mamy.common.system.classes;

public enum ResourceType {
    MANA(0x3366FF, "Mana"),
    RAGE(0xFF3333, "Rage");

    private final int color;
    private final String displayName;

    ResourceType(int color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public int getColor() { return color; }
    public String getDisplayName() { return displayName; }
}