package com.sypztep.mamy.client.screen.widget;

import com.sypztep.mamy.client.util.AnimationUtils;
import com.sypztep.mamy.client.util.DrawContextUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ScrollableTextList {
    private static final int ICON_SIZE = 16;

    private final List<ListElement> items;
    private Map<String, Object> values;
    private final int textHeight;
    private int x, y, width, height;

    // Scroll behavior
    private final ScrollBehavior scrollBehavior;

    public ScrollableTextList(List<ListElement> items, Map<String, Object> values) {
        this.items = items;
        this.values = values;
        this.textHeight = 25;

        // Initialize scroll behavior
        this.scrollBehavior = new ScrollBehavior()
                .setScrollbarWidth(6)
                .setScrollbarPadding(2)
                .setMinHandleSize(20);
    }

    public void updateValues(Map<String, Object> newValues) {
        this.values = newValues;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height,
                       float scale, float iconscale, int alpha, float deltaTick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // Early return if invalid parameters
        if (width <= 0 || height <= 0 || items.isEmpty()) {
            return;
        }

        // Calculate content height and update scroll behavior
        calculateContentHeight();
        scrollBehavior.setBounds(x, y, width, height);
        scrollBehavior.setContentHeight(getCalculatedContentHeight());

        // Update scroll behavior
        scrollBehavior.update(context, 0, 0, deltaTick); // We'll handle mouse in mouse methods

        // Draw the container background
        DrawContextUtils.drawRect(context, x, y, width, height + 10, 0xFF1E1E1E);

        // Add subtle border enhancements
        renderEnhancements(context, x, y, width, height);

        // Enable scissor for content clipping
        scrollBehavior.enableScissor(context);

        // Render content with proper positioning
        renderContent(context, textRenderer, x, y, width, height, scale, iconscale, alpha);

        // Disable scissor
        scrollBehavior.disableScissor(context);
    }

    private void renderEnhancements(DrawContext context, int x, int y, int width, int height) {
        // Just add subtle corner highlights for premium feel
        context.fill(x, y, x + 2, y + 2, 0xFF444444);
        context.fill(x + width - 2, y, x + width, y + 2, 0xFF444444);
        context.fill(x, y + height - 2, x + 2, y + height, 0xFF444444);
        context.fill(x + width - 2, y + height - 2, x + width, y + height, 0xFF444444);
    }

    private void renderContent(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height,
                               float scale, float iconscale, int alpha) {
        int scrollOffset = scrollBehavior.getScrollOffset();
        int currentY = y - scrollOffset;

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.scale(scale, scale, 1.0F);

        // Render each item
        for (ListElement listElement : items) {
            Text itemText = listElement.text();
            Identifier icon = listElement.icon();
            boolean isMainContext = itemText.getString().equals(itemText.getString().toUpperCase());
            int offsetX = isMainContext ? 50 : 0;

            // Skip items that are completely out of view
            if (currentY + textHeight < y) {
                currentY += textHeight;
                continue;
            }
            if (currentY >= y + height) break;

            String displayText = isMainContext ? itemText.getString() : "● " + itemText.getString();
            String formattedText = StringFormatter.format(displayText, values);
            List<String> wrappedLines = wrapText(textRenderer, formattedText, (int)((width - scrollBehavior.getContentWidth()) / scale) - 60);

            int textBlockHeight = wrappedLines.size() * textRenderer.fontHeight;

            for (int lineIndex = 0; lineIndex < wrappedLines.size(); lineIndex++) {
                String line = wrappedLines.get(lineIndex);

                if (currentY >= y && currentY + textRenderer.fontHeight <= y + height) {
                    int textY = currentY + (textHeight - textBlockHeight) / 2;
                    int textX = (int)(x / scale) + offsetX + 30;

                    // Render icon (only on first line of each item)
                    if (icon != null && lineIndex == 0) {
                        matrixStack.push();
                        float iconX = (x + offsetX - ICON_SIZE) / scale - 10;
                        float iconY = textY + (textRenderer.fontHeight) / 2.0f - (float)ICON_SIZE / 2;
                        matrixStack.translate(iconX, iconY, 0);
                        matrixStack.scale(iconscale, iconscale, 1.0F);

                        if (isMainContext) {
                            // Subtle glow for main context icons
                            context.drawGuiTexture(icon, -1, -1, ICON_SIZE + 2, ICON_SIZE + 2);
                        }
                        context.drawGuiTexture(icon, 0, 0, ICON_SIZE, ICON_SIZE);
                        matrixStack.pop();
                    }

                    // Render text
                    if (alpha > 0) {
                        AnimationUtils.drawFadeText(context, textRenderer, line, textX, textY, alpha);
                    }

                    // Enhanced gradient line for main context (only on first line)
                    if (isMainContext && lineIndex == 0) {
                        int gradientY = currentY + textRenderer.fontHeight + 14;
                        DrawContextUtils.renderHorizontalLineWithCenterGradient(context, (int)(x / scale), gradientY, (int)(width / scale), 1, 400, 0xFFFFFFFF, 0);
                        DrawContextUtils.renderHorizontalLineWithCenterGradient(context, (int)(x / scale) + (int)(width / scale)/4, gradientY + 1, (int)(width / scale)/2, 1, 401, 0x80FFFFFF, 0);
                    }
                }
                currentY += textRenderer.fontHeight;
                if (currentY >= y + height) break;
            }

            currentY += Math.max(0, textHeight - wrappedLines.size() * textRenderer.fontHeight);
        }

        matrixStack.pop();
    }

    private void calculateContentHeight() {
        // This would be calculated based on your content
        // For now, using a simple calculation
        int totalHeight = items.size() * textHeight;
        // Add extra height for wrapped text if needed
        // This is a simplified version - you might want to be more precise
    }

    private int getCalculatedContentHeight() {
        // Calculate the actual height needed for all content
        int totalHeight = 0;
        for (ListElement listElement : items) {
            // For each item, calculate its height including wrapped text
            // This is simplified - you might want to cache this calculation
            totalHeight += textHeight;
        }
        return totalHeight;
    }

    // Mouse interaction methods that delegate to ScrollBehavior
    public void scroll(int amount, double mouseX, double mouseY) {
        // Convert old scroll API to new ScrollBehavior API
        double verticalAmount = -amount / 20.0; // Convert to scroll wheel units
        scrollBehavior.handleScroll(mouseX, mouseY, 0, verticalAmount);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return scrollBehavior.handleMouseClick(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return scrollBehavior.handleMouseDrag(mouseX, mouseY, button, dragX, dragY);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        scrollBehavior.handleMouseRelease(mouseX, mouseY, button);
    }

    public boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private List<String> wrapText(TextRenderer textRenderer, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.toString().isEmpty() ? word : currentLine + " " + word;

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

        return lines;
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // Access to scroll behavior for advanced usage
    public ScrollBehavior getScrollBehavior() { return scrollBehavior; }

    public static class StringFormatter {
        public static String format(String template, Map<String, Object> values) {
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String placeholder = "$" + key;
                String replacement = formatValue(value);
                template = template.replace(placeholder, replacement);
            }
            return template;
        }

        private static String formatValue(Object value) {
            String colorCode = "§6";
            return switch (value) {
                case Integer i -> colorCode + String.format("%d", i);
                case Float v -> colorCode + String.format("%.2f", v);
                case Double v -> colorCode + String.format("%.2f", v);
                case String s -> colorCode + s;
                case null, default -> colorCode + "N/A";
            };
        }
    }
}