package com.sypztep.mamy.common.util;

import com.mojang.serialization.Codec;

public final class NumberUtil {
    public static String formatNumber(long number) {
        if (number >= 1_000_000_000_000L) {
            return String.format("%.1fT", number / 1_000_000_000_000.0); // 10.0T
        } else if (number >= 1_000_000_000L) {
            return String.format("%.1fB", number / 1_000_000_000.0); // 1.5B
        } else if (number >= 1_000_000L) {
            return String.format("%.1fM", number / 1_000_000.0); // 250.5M
        } else if (number >= 1_000L) {
            return String.format("%.1fK", number / 1_000.0); // 15.2K
        } else {
            return String.valueOf(number); // 999
        }
    }
    public static String formatDouble(double value, int maxDecimals) {
        String format = "%." + maxDecimals + "f";
        String formatted = String.format(format, value);

        if (formatted.indexOf('.') >= 0) {
            formatted = formatted.replaceAll("0+$", "");
            formatted = formatted.replaceAll("\\.$", "");
        }
        return formatted;
    }
    public enum WeightUnit {
        GRAM("g", 0.001f),
        KILOGRAM("kg", 1f),
        TON("t", 1000f);

        private final String symbol;
        private final float inKg;

        WeightUnit(String symbol, float inKg) {
            this.symbol = symbol;
            this.inKg = inKg;
        }
        public float toGrams(float value) {
            return value * 0.001f;
        }

        public String getSymbol() {
            return symbol;
        }

        public double convertFromKg(float kg) {
            return kg / inKg;
        }

        public static WeightUnit bestFit(float kg) {
            if (kg < 1f) return GRAM;
            if (kg >= 1000f) return TON;
            return KILOGRAM;
        }
        public static final Codec<WeightUnit> CODEC =
                Codec.STRING.xmap(WeightUnit::valueOf, WeightUnit::name);
    }
}
