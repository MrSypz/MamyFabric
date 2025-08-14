package com.sypztep.mamy.client.util;

import com.sypztep.mamy.common.util.ElementalDamageSystem.ElementType;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public final class ElementalColors {
    private static final Map<ElementType, Color> ELEMENT_COLORS = new HashMap<>();

    static {
        ELEMENT_COLORS.put(ElementType.PHYSICAL, new Color(0x9C9393)); // Light gray
        ELEMENT_COLORS.put(ElementType.HEAT, new Color(0xFF4500));     // Orange red
        ELEMENT_COLORS.put(ElementType.ELECTRIC, new Color(0xFFD700)); // Gold
        ELEMENT_COLORS.put(ElementType.WATER, new Color(0x4169E1));    // Royal blue
        ELEMENT_COLORS.put(ElementType.WIND, new Color(0x98FB98));     // Pale green
        ELEMENT_COLORS.put(ElementType.HOLY, new Color(0xDDA0DD));     // Plum
    }

    public static Color getElementColor(ElementType element) {
        return ELEMENT_COLORS.getOrDefault(element, Color.WHITE);
    }
}