package com.sypztep.mamy.client.screen.widget;

import com.sypztep.mamy.client.util.AnimationUtils;
import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.util.ColorUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ScrollableTextList {
    private static final float SCROLL_SPEED = 1.2F;
    private static final int ICON_SIZE = 16;

    // Enhanced scrollbar properties
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_PADDING = 2;
    private static final float SCROLLBAR_ANIMATION_SPEED = 0.15f;
    private static final float SCROLLBAR_FADE_SPEED = 0.1f;

    private final List<ListElement> items;
    private float scrollOffset;
    private float targetScrollOffset;
    private Map<String, Object> values;
    private final int textHeight;
    private int x, y, width, height;

    // Animation and interaction state
    private float scrollbarHoverAnimation = 0.0f;
    private boolean scrollbarHovered = false;
    private boolean isDragging = false;
    private int contentTotalHeight = 0;
    private int maxScroll = 0;

    public ScrollableTextList(List<ListElement> items, Map<String, Object> values) {
        this.items = items;
        this.values = values;
        this.scrollOffset = 0;
        this.targetScrollOffset = 0;
        this.textHeight = 25;
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

        calculateContentHeight();
        updateScrolling(deltaTick);
        updateScrollbarAnimation();

        // Draw the container background that was in the original
        DrawContextUtils.drawRect(context, x, y, width, height + 10, 0xFF1E1E1E);

        // Add subtle border enhancements
        renderEnhancements(context, x, y, width, height);

        // Render content with proper positioning
        renderContent(context, textRenderer, x, y, width, height, scale, iconscale, alpha);

        // Render enhanced scrollbar
        if (maxScroll > 0) {
            renderEnhancedScrollbar(context);
        }
    }

    private void renderEnhancements(DrawContext context, int x, int y, int width, int height) {
        // Just add subtle corner highlights for premium feel without disrupting main rendering
        context.fill(x, y, x + 2, y + 2, 0xFF444444);
        context.fill(x + width - 2, y, x + width, y + 2, 0xFF444444);
        context.fill(x, y + height - 2, x + 2, y + height, 0xFF444444);
        context.fill(x + width - 2, y + height - 2, x + width, y + height, 0xFF444444);
    }

    private void renderContent(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height,
                               float scale, float iconscale, int alpha) {
        int currentY = y - (int)scrollOffset;

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
            List<String> wrappedLines = wrapText(textRenderer, formattedText, (int)(width / scale) - 60);

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

                    // Render text - use direct method if AnimationUtils is causing issues
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

    private void renderEnhancedScrollbar(DrawContext context) {
        if (maxScroll <= 0) return;

        int visibleHeight = height;
        int scrollbarX = x + width - SCROLLBAR_WIDTH - SCROLLBAR_PADDING;
        int scrollbarY = y;
        int scrollbarHeight = height;

        // Background with animation
        int bgAlpha = 25 + (int)(55 * scrollbarHoverAnimation);
        context.fill(scrollbarX, scrollbarY, scrollbarX + SCROLLBAR_WIDTH, scrollbarY + scrollbarHeight,
                (bgAlpha << 24));

        // Handle
        int handleHeight = Math.max(20, scrollbarHeight * visibleHeight / contentTotalHeight);
        int handleY = scrollbarY + (int)((scrollbarHeight - handleHeight) * scrollOffset / maxScroll);

        // Animate handle size when hovered
        int handleExpansion = (int)scrollbarHoverAnimation;
        int animatedScrollbarX = scrollbarX - handleExpansion;
        int animatedHandleHeight = handleHeight + handleExpansion * 2;

        // Colors with smooth transitions
        int baseAlpha = 120 + (int)(135 * scrollbarHoverAnimation);
        int handleBgColor = (baseAlpha << 24) | 0x666666;
        int handleFgColor = ColorUtils.interpolateColor(0xFFAAAAAA, 0xFFFFFFFF, scrollbarHoverAnimation);

        // Glow effect when hovered
        if (scrollbarHoverAnimation > 0.1f) {
            int glowSize = (int)(4 * scrollbarHoverAnimation);
            int glowAlpha = (int)(40 * scrollbarHoverAnimation);
            int glowColor = (glowAlpha << 24) | 0xFFFFFF;

            context.fill(animatedScrollbarX - glowSize, handleY - glowSize,
                    animatedScrollbarX + SCROLLBAR_WIDTH + glowSize,
                    handleY + animatedHandleHeight + glowSize, glowColor);
        }

        // Handle background
        context.fill(animatedScrollbarX, handleY,
                animatedScrollbarX + SCROLLBAR_WIDTH, handleY + animatedHandleHeight,
                handleBgColor);

        // Handle foreground with rounded effect
        context.fill(animatedScrollbarX + 1, handleY + 1,
                animatedScrollbarX + SCROLLBAR_WIDTH - 1, handleY + animatedHandleHeight - 1,
                handleFgColor);

        // Grip lines when highly hovered
        if (scrollbarHoverAnimation > 0.5f) {
            int lineY = handleY + animatedHandleHeight / 2 - 3;
            int lineAlpha = (int)(255 * ((scrollbarHoverAnimation - 0.5f) * 2));
            int lineColor = (lineAlpha << 24) | 0x999999;

            for (int i = 0; i < 3; i++) {
                context.fill(animatedScrollbarX + 2, lineY + (i * 3),
                        animatedScrollbarX + SCROLLBAR_WIDTH - 2, lineY + (i * 3) + 1,
                        lineColor);
            }
        }
    }

    private void calculateContentHeight() {
        int totalItems = items.size();
        contentTotalHeight = totalItems * textHeight;
        maxScroll = Math.max(0, contentTotalHeight - height);
        // Clamp current scroll
        scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);
        targetScrollOffset = MathHelper.clamp(targetScrollOffset, 0, maxScroll);
    }

    private void updateScrolling(float deltaTick) {
        // Smooth scrolling with lerp
        if (Math.abs(scrollOffset - targetScrollOffset) > 0.1f) {
            float delta = (targetScrollOffset - scrollOffset) * SCROLL_SPEED * deltaTick;
            scrollOffset += delta;
        } else {
            scrollOffset = targetScrollOffset;
        }

        scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);
    }

    private void updateScrollbarAnimation() {
        // Update scrollbar hover animation
        if (scrollbarHovered || isDragging) {
            scrollbarHoverAnimation = Math.min(1.0f, scrollbarHoverAnimation + SCROLLBAR_ANIMATION_SPEED);
        } else {
            scrollbarHoverAnimation = Math.max(0.0f, scrollbarHoverAnimation - SCROLLBAR_FADE_SPEED);
        }
    }

    private boolean isScrollbarClicked(double mouseX, double mouseY) {
        if (maxScroll <= 0) return false;

        int scrollbarX = x + width - SCROLLBAR_WIDTH - SCROLLBAR_PADDING;
        int scrollbarY = y;
        int scrollbarHeight = height;

        return mouseX >= scrollbarX && mouseX <= scrollbarX + SCROLLBAR_WIDTH &&
                mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight;
    }

    public void scroll(int amount, double mouseX, double mouseY) {
        if (isMouseOver(mouseX, mouseY, this.x, this.y, this.width, this.height)) {
            targetScrollOffset -= amount;
            targetScrollOffset = MathHelper.clamp(targetScrollOffset, 0, maxScroll);

            // Update scrollbar hover state
            scrollbarHovered = isScrollbarClicked(mouseX, mouseY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isScrollbarClicked(mouseX, mouseY)) {
            isDragging = true;
            // Calculate new scroll position
            int scrollbarY = y;
            int scrollbarHeight = height;
            double scrollRatio = (mouseY - scrollbarY) / (double)scrollbarHeight;
            scrollOffset = (float)(scrollRatio * maxScroll);
            targetScrollOffset = scrollOffset;
            scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);
            targetScrollOffset = MathHelper.clamp(targetScrollOffset, 0, maxScroll);
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            int scrollbarY = y;
            int scrollbarHeight = height;

            double scrollRatio = (mouseY - scrollbarY) / (double)scrollbarHeight;
            scrollOffset = (float)(scrollRatio * maxScroll);
            targetScrollOffset = scrollOffset;
            scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);
            targetScrollOffset = MathHelper.clamp(targetScrollOffset, 0, maxScroll);
            return true;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
    }

    public boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        boolean isOver = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        if (isOver && maxScroll > 0) {
            scrollbarHovered = isScrollbarClicked(mouseX, mouseY);
        } else {
            scrollbarHovered = false;
        }
        return isOver;
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