package com.sypztep.mamy.common.system.classes;

public enum ResourceType {
    MANA(0xFF3366FF, 0xFF66AAFF,"Mana"),
    RAGE(0xFFFF3333, 0xFFFF6666,"Rage");

    private final int color;
    private final int colorglow;
    private final String displayName;

    ResourceType(int color,int colorglow, String displayName) {
        this.color = color;
        this.colorglow =  colorglow;
        this.displayName = displayName;
    }

    public int getColor() { return color; }
    public int getColorglow() { return colorglow; }
    public String getDisplayName() { return displayName; }
}