package com.sypztep.mamy.common.util;

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
    public static String formatNumber(double number) {
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
}
