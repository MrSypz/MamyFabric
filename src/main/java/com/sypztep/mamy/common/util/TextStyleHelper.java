package com.sypztep.mamy.common.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple utility class for auto-styling text based on common patterns
 */
public class TextStyleHelper {

    // Pattern to match: +15% Movement Speed, -10 Health, ×5x damage, etc.
    // More precise pattern that handles word boundaries better
    private static final Pattern BONUS_PATTERN = Pattern.compile("([+\\-×x])(\\d+(?:\\.\\d+)?)([%x]?)(?=\\s|$|[^\\w%])");

    // Pattern for standalone percentages not preceded by +/-
    private static final Pattern STANDALONE_PERCENTAGE_PATTERN = Pattern.compile("(?<![-+×x])(\\d+(?:\\.\\d+)?%)(?=\\s|$|\\W)");

    // Pattern for numbers with 'x' multiplier
    private static final Pattern MULTIPLIER_X_PATTERN = Pattern.compile("(?<!\\w)(\\d+(?:\\.\\d+)?x)(?=\\s|$|\\W)");

    /**
     * Auto-style text based on common patterns:
     * - +value = green
     * - -value = red
     * - ×value or xvalue = yellow
     * - percentages = colored based on prefix
     * - standalone numbers = yellow
     */
    public static MutableText autoStyle(String input) {
        MutableText result = Text.empty();
        String remaining = input;

        while (!remaining.isEmpty()) {
            boolean foundPattern = false;

            // Check for bonus/penalty patterns first (+15%, -10%, ×5, etc.)
            Matcher bonusMatcher = BONUS_PATTERN.matcher(remaining);
            if (bonusMatcher.find() && bonusMatcher.start() == 0) {
                String operator = bonusMatcher.group(1);  // +, -, ×, x
                String value = bonusMatcher.group(2);     // 15, 30, etc.
                String suffix = bonusMatcher.group(3);    // %, x, or empty

                // Determine color based on operator
                Formatting color;
                if (operator.equals("+")) {
                    color = Formatting.GREEN;
                } else if (operator.equals("-")) {
                    color = Formatting.RED;
                } else { // × or x
                    color = Formatting.YELLOW;
                }

                // Build the styled text as one unit
                String fullMatch = operator + value + (suffix != null ? suffix : "");
                result.append(Text.literal(fullMatch).formatted(color));

                remaining = remaining.substring(bonusMatcher.end());
                foundPattern = true;
            }

            // Check for standalone percentages (not preceded by +/-)
            else {
                Matcher percentMatcher = STANDALONE_PERCENTAGE_PATTERN.matcher(remaining);
                if (percentMatcher.find() && percentMatcher.start() == 0) {
                    String percent = percentMatcher.group(1);
                    result.append(Text.literal(percent).formatted(Formatting.AQUA));
                    remaining = remaining.substring(percentMatcher.end());
                    foundPattern = true;
                }
            }

            // Check for standalone multipliers (5x, 2.5x, etc.)
            if (!foundPattern) {
                Matcher multiplierMatcher = MULTIPLIER_X_PATTERN.matcher(remaining);
                if (multiplierMatcher.find() && multiplierMatcher.start() == 0) {
                    String multiplier = multiplierMatcher.group(1);
                    result.append(Text.literal(multiplier).formatted(Formatting.YELLOW));
                    remaining = remaining.substring(multiplierMatcher.end());
                    foundPattern = true;
                }
            }

            // If no pattern found, add the next character as normal text
            if (!foundPattern) {
                result.append(Text.literal(remaining.substring(0, 1)).formatted(Formatting.GRAY));
                remaining = remaining.substring(1);
            }
        }

        return result;
    }

    // Simple builder for manual styling (optional, for complex cases)
    public static class TextBuilder {
        private final MutableText text = Text.empty();

        public TextBuilder text(String str) {
            text.append(Text.literal(str).formatted(Formatting.GRAY));
            return this;
        }

        public TextBuilder positive(String str) {
            text.append(Text.literal(str).formatted(Formatting.GREEN));
            return this;
        }

        public TextBuilder negative(String str) {
            text.append(Text.literal(str).formatted(Formatting.RED));
            return this;
        }

        public TextBuilder multiplier(String str) {
            text.append(Text.literal(str).formatted(Formatting.YELLOW));
            return this;
        }

        public TextBuilder percentage(String str) {
            text.append(Text.literal(str).formatted(Formatting.AQUA));
            return this;
        }

        public MutableText build() {
            return text;
        }
    }

    public static TextBuilder create() {
        return new TextBuilder();
    }
}