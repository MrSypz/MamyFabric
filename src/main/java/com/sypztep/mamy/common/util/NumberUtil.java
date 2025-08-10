package com.sypztep.mamy.common.util;

public final class NumberUtil {
    public static String formatNumber(long number) {
        if (number >= 1_000_000_000L) {
            return String.format("%.1fB", number / 1_000_000_000.0);
        } else if (number >= 1_000_000L) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000L) {
            return String.format("%.1fK", number / 1_000.0);
        } else {
            return String.valueOf(number);
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
}
