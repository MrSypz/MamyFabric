package com.sypztep.mamy.client.screen.widget;

import com.sypztep.mamy.client.util.DrawContextUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ScrollableTextList {
    private static final int ICON_SIZE = 16;
    private static final int MIN_ITEM_HEIGHT = 15;
    private static final int ITEM_PADDING = 0;
    private static final int CONTENT_PADDING = 6;

    private int hoveredItemIndex = -1;

    // Core data
    private final List<ListElement> items;
    private Map<String, Object> values;

    // UI state
    private int x, y, width, height;
    private final List<Integer> itemHeights;
    private final List<List<String>> wrappedTextCache;
    private boolean needsHeightRecalculation = true;

    // Enhanced scroll behavior
    private final ScrollBehavior scrollBehavior;

    public ScrollableTextList(List<ListElement> items, Map<String, Object> values) {
        this.items = items;
        this.values = values;
        this.itemHeights = new ArrayList<>();
        this.wrappedTextCache = new ArrayList<>();

        this.scrollBehavior = new ScrollBehavior()
                .setScrollbarWidth(5)
                .setScrollbarPadding(0)
                .setMinHandleSize(24)
                .setScrollbarEnabled(true);
    }

    public void updateValues(Map<String, Object> newValues) {
        this.values = newValues;
        this.needsHeightRecalculation = true;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height,
                       float scale, float iconScale, int alpha, float deltaTime, double mouseX, double mouseY) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        // Early return if invalid parameters
        if (width <= 0 || height <= 0 || items.isEmpty()) {
            return;
        }

        // Recalculate heights if needed
        if (needsHeightRecalculation || itemHeights.size() != items.size()) {
            calculateItemHeights(textRenderer, scale);
            needsHeightRecalculation = false;
        }
        updateHoverState(mouseX, mouseY);

        // Update scroll behavior bounds and content
        updateScrollBounds();

        // Draw the container background with enhanced styling
        DrawContextUtils.drawRect(context, x, y, width, height, 0xFF1E1E1E);

        // Update scroll behavior
        scrollBehavior.update(context, (int) mouseX, (int) mouseY, deltaTime);

        // Enable scissor for content clipping
        scrollBehavior.enableScissor(context);

        // Render content with proper positioning
        renderContent(context, textRenderer, scale, iconScale, alpha);

        // Disable scissor
        scrollBehavior.disableScissor(context);
    }

    private void calculateItemHeights(TextRenderer textRenderer, float scale) {
        itemHeights.clear();
        wrappedTextCache.clear();

        // Calculate available width for text (accounting for padding and icon space)
        int textWidth = (int)((width - CONTENT_PADDING * 2) / scale);

        for (int i = 0; i < items.size(); i++) {
            ListElement listElement = items.get(i);
            Text itemText = listElement.text();
            boolean isMainContext = itemText.getString().equals(itemText.getString().toUpperCase());

            String displayText = isMainContext ? itemText.getString() : "● " + itemText.getString();
            String formattedText = StringFormatter.format(displayText, values);

            List<String> wrappedLines = wrapText(textRenderer, formattedText, textWidth);
            wrappedTextCache.add(wrappedLines);

            int textBlockHeight = wrappedLines.size() * textRenderer.fontHeight;
            int itemHeight = Math.max(MIN_ITEM_HEIGHT, textBlockHeight + ITEM_PADDING);

            if (isMainContext) itemHeight += 3;

            if (i == items.size() - 1) itemHeight += 5; // Add space on last

            itemHeights.add(itemHeight);
        }

    }

    private void updateScrollBounds() {
        scrollBehavior.setBounds(x, y, width, height);

        int totalContentHeight = itemHeights.stream().mapToInt(Integer::intValue).sum();
        scrollBehavior.setContentHeight(totalContentHeight);
    }

    private void renderContent(DrawContext context, TextRenderer textRenderer, float scale, float iconScale, int alpha) {
        int scrollOffset = scrollBehavior.getScrollOffset();
        int currentY = y + CONTENT_PADDING - scrollOffset;

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.scale(scale, scale, 1.0F);

        // Render each item with enhanced styling
        for (int i = 0; i < items.size(); i++) {
            ListElement listElement = items.get(i);
            int itemHeight = itemHeights.get(i);
            List<String> wrappedLines = wrappedTextCache.get(i);

            // Skip items that are completely out of view
            if (currentY + itemHeight < y || currentY >= y + height) {
                currentY += itemHeight;
                continue;
            }

            boolean isHovered = (i == hoveredItemIndex);
            renderListItem(context, textRenderer, listElement, wrappedLines, currentY, itemHeight,
                    scale, alpha, isHovered);

            currentY += itemHeight;
        }

        matrixStack.pop();
    }

    private void renderListItem(DrawContext context, TextRenderer textRenderer, ListElement listElement,
                                List<String> wrappedLines, int currentY, int itemHeight,
                                float scale, int alpha, boolean isHovered) {

        Text itemText = listElement.text();
        Identifier icon = listElement.icon();
        boolean isMainContext = itemText.getString().equals(itemText.getString().toUpperCase());

        int offsetX = isMainContext ? 50 : 20;
        int textX = (int)((x + offsetX) / scale);
        int textY = (int)((currentY + ITEM_PADDING) / scale);

        if (icon != null) {
            renderIcon(context, icon, textX - 30, textY);
        }

        // Render text lines with proper spacing
        for (String line : wrappedLines) {
            if (alpha > 0) {
                int textColor;
                if (isMainContext) {
                    context.drawText(textRenderer,
                            Text.literal(line).formatted(Formatting.BOLD),
                            textX, textY, 0xFFD700,false);
                } else {
                    textColor = isHovered ? 0xFFFFFF : 0xCCCCCC;
                    context.drawText(textRenderer, Text.of(line), textX, textY, textColor, false);
                }
            }

            textY += textRenderer.fontHeight + 2;
        }

        // Enhanced separator line for main context items
        if (isMainContext) {
            int separatorY = (int)((currentY + itemHeight - 8) / scale);
            int separatorX = (int)((x + 10) / scale);
            int separatorWidth = (int)((width - 15) / scale);

            // Gradient separator line
            DrawContextUtils.renderHorizontalLineWithCenterGradient(context,
                    separatorX, separatorY, separatorWidth, 1, 400,
                    0xFFFFD700, 0, 1);
        }
    }

    private void renderIcon(DrawContext context, Identifier icon, int x, int y) {
        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();

        float finalY = y - 4; // Slight vertical adjustment

        matrixStack.translate((float) x, finalY, 0);

        context.drawGuiTexture(icon, 0, 0, ICON_SIZE, ICON_SIZE);

        matrixStack.pop();
    }


    private List<String> wrapText(TextRenderer textRenderer, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();

        // Handle explicit line breaks first
        String[] explicitLines = text.split("\\n");

        for (String line : explicitLines) {
            String[] words = line.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;

                if (textRenderer.getWidth(testLine) <= maxWidth) {
                    if (!currentLine.isEmpty()) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                } else {
                    if (!currentLine.isEmpty()) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    } else {
                        lines.add(word);
                    }
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }
        }

        return lines;
    }

    private void updateHoverState(double mouseX, double mouseY) {
        hoveredItemIndex = -1;
        if (!isMouseOver(mouseX, mouseY, x, y, width, height)) {
            return;
        }

        int scrollOffset = scrollBehavior.getScrollOffset();
        int currentY = y + CONTENT_PADDING - scrollOffset;

        for (int i = 0; i < items.size(); i++) {
            int itemHeight = itemHeights.get(i);

            if (mouseY >= currentY && mouseY < currentY + itemHeight) {
                hoveredItemIndex = i;
                break;
            }

            currentY += itemHeight;
        }
    }

    // Enhanced mouse interaction methods
    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        return scrollBehavior.handleMouseClick(mouseX, mouseY, button);
    }

    public boolean handleMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return scrollBehavior.handleMouseDrag(mouseX, mouseY, button, dragX, dragY);
    }

    public void handleMouseRelease(double mouseX, double mouseY, int button) {
        scrollBehavior.handleMouseRelease(mouseX, mouseY, button);
    }

    public boolean handleMouseScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return scrollBehavior.handleScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    // Getters for external access
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public static class StringFormatter {
        public static String format(String template, Map<String, Object> values) {
            if (template == null || values == null) {
                return template != null ? template : "";
            }

            String result = template;
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String placeholder = "$" + key;

                if (result.contains(placeholder)) {
                    String replacement = formatValue(value);
                    result = result.replace(placeholder, replacement);
                }
            }
            return result;
        }

        private static String formatValue(Object value) {
            String colorCode = "§6";
            return switch (value) {
                case Short s -> colorCode + s; // Add explicit Short handling
                case Integer i -> colorCode + String.format("%d", i);
                case Float v -> colorCode + String.format("%.2f", v);
                case Double v -> colorCode + String.format("%.2f", v);
                case String s -> colorCode + s;
                case null, default -> colorCode + "N/A";
            };
        }
    }
}