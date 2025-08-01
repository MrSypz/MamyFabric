package com.sypztep.mamy.common.system.classes;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum ResourceType {
    MANA(0x3366FF, "Mana", Formatting.BLUE),
    RAGE(0xFF3333, "Rage", Formatting.YELLOW);

    private final int color;
    private final String displayName;
    private final Formatting textColor;

    ResourceType(int color, String displayName, Formatting textColor) {
        this.color = color;
        this.displayName = displayName;
        this.textColor = textColor;
    }

    public int getColor() { return color; }
    public String getDisplayName() { return displayName; }
    public Formatting getTextColor() { return textColor; }

    public Text getFormattedName() {
        return Text.literal(displayName).formatted(textColor);
    }
}