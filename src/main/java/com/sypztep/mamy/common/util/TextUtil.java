package com.sypztep.mamy.common.util;

import net.minecraft.client.font.TextRenderer;

import java.util.ArrayList;
import java.util.List;

public class TextUtil {
//    public static List<String> wrapText(String text, int maxWidth, TextRenderer textRenderer) {
//        List<String> lines = new ArrayList<>();
//        String[] words = text.split(" ");
//        StringBuilder currentLine = new StringBuilder();
//
//        for (String word : words) {
//            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
//            if (textRenderer.getWidth(testLine) <= maxWidth) {
//                currentLine = new StringBuilder(testLine);
//            } else {
//                if (!currentLine.isEmpty()) {
//                    lines.add(currentLine.toString());
//                    currentLine = new StringBuilder(word);
//                } else {
//                    lines.add(word);
//                }
//            }
//        }
//
//        if (!currentLine.isEmpty()) {
//            lines.add(currentLine.toString());
//        }
//
//        return lines;
//    }
    public static List<String> wrapText(TextRenderer textRenderer, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();

        String[] explicitLines = text.split("\\n"); // for spacing

        for (String line : explicitLines) {
            String[] words = line.split(" ");

            if (words.length == 0) {
                lines.add("");
                continue;
            }

            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;

                if (textRenderer.getWidth(testLine) <= maxWidth) currentLine = new StringBuilder(testLine);
                else {
                    if (!currentLine.isEmpty()) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    } else lines.add(word);
                }
            }

            if (!currentLine.isEmpty()) lines.add(currentLine.toString());

        }

        return lines;
    }
    public static String warpLine(String text, int maxWidth, TextRenderer renderer) {
        if (renderer.getWidth(text) <= maxWidth) {
            return text;
        }

        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (renderer.getWidth(sb.toString() + c + "...") > maxWidth) {
                sb.append("...");
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
