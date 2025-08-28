package com.sypztep.mamy.client.screen.widget;

import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.util.TextUtil;
import net.minecraft.client.MinecraftClient;
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

    // Search UI constants
    private static final int SEARCH_BAR_HEIGHT = 24;
    private static final int SEARCH_BAR_PADDING = 4;
    private static final int SEARCH_ICON_SIZE = 12;
    private static final Identifier SEARCH_ICON = Identifier.ofVanilla("icon/search");

    private int hoveredItemIndex = -1;

    // Core data
    private final List<ListElement> allItems;
    private final List<ListElement> filteredItems;
    private Map<String, Object> values;

    // Search functionality
    private String searchQuery = "";
    private boolean searchFocused = false;

    // UI state
    private int x, y, width, height;
    private final List<Integer> itemHeights;
    private final List<List<String>> wrappedTextCache;
    private boolean needsHeightRecalculation = true;

    // Enhanced scroll behavior
    private final ScrollBehavior scrollBehavior;

    public ScrollableTextList(List<ListElement> items, Map<String, Object> values) {
        this.allItems = items;
        this.filteredItems = new ArrayList<>(items);
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

        // Reapply search filter if there's an active search
        if (!searchQuery.isEmpty()) {
            applySearchFilter();
        }
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height,
                       float scale, float deltaTime, double mouseX, double mouseY) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // Early return if invalid parameters
        if (width <= 0 || height <= 0 || allItems.isEmpty()) {
            return;
        }

        // Calculate search bar area
        int searchBarY = y;
        int contentY = y + SEARCH_BAR_HEIGHT + SEARCH_BAR_PADDING;
        int contentHeight = height - SEARCH_BAR_HEIGHT - SEARCH_BAR_PADDING;

        // Render search bar
        renderSearchBar(context, textRenderer, x, searchBarY, width, SEARCH_BAR_HEIGHT, mouseX, mouseY);

        // Recalculate heights if needed
        if (needsHeightRecalculation || itemHeights.size() != filteredItems.size()) {
            calculateItemHeights(textRenderer, scale);
            needsHeightRecalculation = false;
        }

        updateHoverState(mouseX, mouseY, contentY, contentHeight);

        // Update scroll behavior bounds and content
        updateScrollBounds(contentY, contentHeight);

        // Draw the container background with enhanced styling
        DrawContextUtils.drawRect(context, x, contentY, width, contentHeight, 0xFF1E1E1E);

        // Update scroll behavior
        scrollBehavior.update(context, (int) mouseX, (int) mouseY, deltaTime);

        // Enable scissor for content clipping
        scrollBehavior.enableScissor(context);

        // Render content with proper positioning
        renderContent(context, textRenderer, scale, contentY);

        // Disable scissor
        scrollBehavior.disableScissor(context);
    }

    private void renderSearchBar(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height, double mouseX, double mouseY) {
        // Background
        int bgColor = searchFocused ? 0xFF2A2A2A : 0xFF1A1A1A;
        DrawContextUtils.drawRect(context, x, y, width, height, bgColor);

        // Border
        int borderColor = searchFocused ? 0xFF4A9EFF : 0xFF404040;
        context.drawBorder(x, y, width, height, borderColor);

        // Search icon
        int iconX = x + SEARCH_BAR_PADDING;
        int iconY = y + (height - SEARCH_ICON_SIZE) / 2;
        context.drawGuiTexture(SEARCH_ICON, iconX, iconY, SEARCH_ICON_SIZE, SEARCH_ICON_SIZE);

        // Search text
        String displayText = searchQuery;
        int textX = iconX + SEARCH_ICON_SIZE + SEARCH_BAR_PADDING;
        int textY = y + (height - textRenderer.fontHeight) / 2;

        if (searchQuery.isEmpty() && !searchFocused) {
            // Placeholder text
            context.drawText(textRenderer, Text.literal("Search...").formatted(Formatting.GRAY),
                    textX, textY, 0xFF888888, false);
        } else {
            // Actual search text
            context.drawText(textRenderer, Text.literal(displayText),
                    textX, textY, 0xFFFFFFFF, false);
        }

        // Results count
        if (!searchQuery.isEmpty()) {
            String resultText = filteredItems.size() + " results";
            int resultWidth = textRenderer.getWidth(resultText);
            int resultX = x + width - resultWidth - SEARCH_BAR_PADDING;
            context.drawText(textRenderer, Text.literal(resultText).formatted(Formatting.GRAY),
                    resultX, textY, 0xFF888888, false);
        }
    }

    private void calculateItemHeights(TextRenderer textRenderer, float scale) {
        itemHeights.clear();
        wrappedTextCache.clear();

        int textWidth = (int) ((width - CONTENT_PADDING * 2) / scale);

        for (int i = 0; i < filteredItems.size(); i++) {
            ListElement listElement = filteredItems.get(i);
            Text itemText = listElement.text();
            boolean isMainContext = itemText.getString().equals(itemText.getString().toUpperCase());

            String displayText = isMainContext ? itemText.getString() : "● " + itemText.getString();
            String formattedText = StringFormatter.format(displayText, values);

            List<String> wrappedLines = TextUtil.wrapText(textRenderer, formattedText, textWidth);
            wrappedTextCache.add(wrappedLines);

            int textBlockHeight = wrappedLines.size() * textRenderer.fontHeight;
            int itemHeight = Math.max(MIN_ITEM_HEIGHT, textBlockHeight + ITEM_PADDING);

            if (isMainContext) itemHeight += 3;
            if (i == filteredItems.size() - 1) itemHeight += 5; // Add space on last

            itemHeights.add(itemHeight);
        }
    }

    private void updateScrollBounds(int contentY, int contentHeight) {
        scrollBehavior.setBounds(x, contentY, width, contentHeight);

        int totalContentHeight = itemHeights.stream().mapToInt(Integer::intValue).sum();
        scrollBehavior.setContentHeight(totalContentHeight);
    }

    private void renderContent(DrawContext context, TextRenderer textRenderer, float scale, int contentY) {
        int scrollOffset = scrollBehavior.getScrollOffset();
        int currentY = contentY + CONTENT_PADDING - scrollOffset;

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.scale(scale, scale, 1.0F);

        // Render each filtered item with enhanced styling
        for (int i = 0; i < filteredItems.size(); i++) {
            ListElement listElement = filteredItems.get(i);
            int itemHeight = itemHeights.get(i);
            List<String> wrappedLines = wrappedTextCache.get(i);

            // Skip items that are completely out of view
            if (currentY + itemHeight < contentY || currentY >= contentY + height - SEARCH_BAR_HEIGHT - SEARCH_BAR_PADDING) {
                currentY += itemHeight;
                continue;
            }

            boolean isHovered = (i == hoveredItemIndex);
            renderListItem(context, textRenderer, listElement, wrappedLines, currentY, itemHeight,
                    scale, isHovered);

            currentY += itemHeight;
        }

        matrixStack.pop();
    }

    private void renderListItem(DrawContext context, TextRenderer textRenderer, ListElement listElement,
                                List<String> wrappedLines, int currentY, int itemHeight,
                                float scale, boolean isHovered) {

        Text itemText = listElement.text();
        Identifier icon = listElement.icon();
        boolean isMainContext = itemText.getString().equals(itemText.getString().toUpperCase());

        int offsetX = isMainContext ? 50 : 20;
        int textX = (int) ((x + offsetX) / scale);
        int textY = (int) ((currentY + ITEM_PADDING) / scale);

        if (icon != null) {
            renderIcon(context, icon, textX - 30, textY);
        }

        // Render text lines with proper spacing (original styling)
        for (String line : wrappedLines) {
            int textColor;
            if (isMainContext) {
                context.drawText(textRenderer,
                        Text.literal(line).formatted(Formatting.BOLD),
                        textX, textY, 0xFFD700, false);
            } else {
                textColor = isHovered ? 0xFFFFFF : 0xCCCCCC;
                context.drawText(textRenderer, Text.literal(line), textX, textY, textColor, false);
            }

            textY += textRenderer.fontHeight + 2;
        }

        // Enhanced separator line for main context items
        if (isMainContext) {
            int separatorY = (int) ((currentY + itemHeight - 8) / scale);
            int separatorX = (int) ((x + 10) / scale);
            int separatorWidth = (int) ((width - 15) / scale);

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

    private void updateHoverState(double mouseX, double mouseY, int contentY, int contentHeight) {
        hoveredItemIndex = -1;
        if (!isMouseOver(mouseX, mouseY, x, contentY, width, contentHeight)) {
            return;
        }

        int scrollOffset = scrollBehavior.getScrollOffset();
        int currentY = contentY + CONTENT_PADDING - scrollOffset;

        for (int i = 0; i < filteredItems.size(); i++) {
            int itemHeight = itemHeights.get(i);

            if (mouseY >= currentY && mouseY < currentY + itemHeight) {
                hoveredItemIndex = i;
                break;
            }

            currentY += itemHeight;
        }
    }

    // Fuzzy search implementation
    private void applySearchFilter() {
        filteredItems.clear();

        if (searchQuery.isEmpty()) {
            filteredItems.addAll(allItems);
        } else {
            for (ListElement item : allItems) {
                String itemText = item.text().getString();
                String translationKey = extractTranslationKey(itemText);

                // Check if the item matches the search query
                if (fuzzyMatch(itemText, searchQuery) ||
                        fuzzyMatch(translationKey, searchQuery) ||
                        containsSearchTerms(itemText, searchQuery) ||
                        containsSearchTerms(translationKey, searchQuery)) {
                    filteredItems.add(item);
                }
            }
        }

        needsHeightRecalculation = true;
        scrollBehavior.resetScroll();
    }

    private String extractTranslationKey(String text) {
        // Extract keywords from translation keys like "mamy.info.strength" -> "strength"
        if (text.contains(".")) {
            String[] parts = text.split("\\.");
            return parts[parts.length - 1].replace("_", " ");
        }
        return text;
    }

    private boolean fuzzyMatch(String text, String query) {
        if (text == null || query == null) return false;

        text = text.toLowerCase();
        query = query.toLowerCase();

        // Direct substring match
        if (text.contains(query)) return true;

        // Fuzzy character sequence matching
        int queryIndex = 0;
        for (int i = 0; i < text.length() && queryIndex < query.length(); i++) {
            if (text.charAt(i) == query.charAt(queryIndex)) {
                queryIndex++;
            }
        }

        return queryIndex == query.length();
    }

    private boolean containsSearchTerms(String text, String query) {
        if (text == null || query == null) return false;

        text = text.toLowerCase();
        query = query.toLowerCase();

        // Split query into individual terms
        String[] terms = query.split("\\s+");

        for (String term : terms) {
            if (!text.contains(term)) {
                return false;
            }
        }

        return true;
    }

    // Enhanced mouse interaction methods
    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        // Check if click is in search bar
        if (mouseY >= y && mouseY < y + SEARCH_BAR_HEIGHT) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        int contentY = y + SEARCH_BAR_HEIGHT + SEARCH_BAR_PADDING;
        int contentHeight = height - SEARCH_BAR_HEIGHT - SEARCH_BAR_PADDING;

        if (isMouseOver(mouseX, mouseY, x, contentY, width, contentHeight)) {
            return scrollBehavior.handleMouseClick(mouseX, mouseY, button);
        }

        return false;
    }

    public boolean handleMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return scrollBehavior.handleMouseDrag(mouseX, mouseY, button, dragX, dragY);
    }

    public void handleMouseRelease(double mouseX, double mouseY, int button) {
        scrollBehavior.handleMouseRelease(mouseX, mouseY, button);
    }

    public boolean handleMouseScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int contentY = y + SEARCH_BAR_HEIGHT + SEARCH_BAR_PADDING;
        int contentHeight = height - SEARCH_BAR_HEIGHT - SEARCH_BAR_PADDING;

        if (isMouseOver(mouseX, mouseY, x, contentY, width, contentHeight)) {
            return scrollBehavior.handleScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        return false;
    }

    // Keyboard input handling
    public boolean handleKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (!searchFocused) return false;

        MinecraftClient client = MinecraftClient.getInstance();

        // Handle special keys
        switch (keyCode) {
            case 257: // ENTER
                searchFocused = false;
                return true;
            case 256: // ESCAPE
                searchQuery = "";
                searchFocused = false;
                applySearchFilter();
                return true;
            case 259: // BACKSPACE
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    applySearchFilter();
                }
                return true;
        }

        return false;
    }

    public boolean handleCharTyped(char chr, int modifiers) {
        if (!searchFocused) return false;

        if (Character.isLetterOrDigit(chr) || Character.isWhitespace(chr) || ".-_".indexOf(chr) >= 0) {
            searchQuery += chr;
            applySearchFilter();
            return true;
        }

        return false;
    }

    public boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    // Getters for external access
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isSearchFocused() { return searchFocused; }
    public String getSearchQuery() { return searchQuery; }

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
                case Short s -> colorCode + s;
                case Integer i -> colorCode + String.format("%d", i);
                case Float v -> colorCode + String.format("%.2f", v);
                case Double v -> colorCode + String.format("%.2f", v);
                case String s -> colorCode + s;
                case null, default -> colorCode + "N/A";
            };
        }
    }
}