package com.sypztep.mamy.client.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Consumer;

public class SimpleDropdown {
    private final TextRenderer textRenderer;
    private final List<String> options;
    private final Consumer<String> onSelectionChanged;

    public int x, y, width, height;
    private String selectedValue;
    private boolean isOpen = false;

    private static final int ITEM_HEIGHT = 18;
    private static final int MAX_VISIBLE_ITEMS = 8;
    private static final int DROPDOWN_PADDING = 4;

    public SimpleDropdown(List<String> options, String defaultSelection, Consumer<String> onSelectionChanged) {
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.options = options;
        this.selectedValue = defaultSelection;
        this.onSelectionChanged = onSelectionChanged;
        this.height = ITEM_HEIGHT;
    }

    public void setBounds(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Main dropdown button
        int bgColor = isHovered(mouseX, mouseY) && !isOpen ? 0xFF2A2A2A : 0xFF1A1A1A;
        context.fill(x, y, x + width, y + height, bgColor);
        context.drawBorder(x, y, width, height, 0xFF444444);

        // Selected value text
        String displayText = selectedValue.isEmpty() ? "All" : selectedValue;
        int textX = x + DROPDOWN_PADDING;
        int textY = y + (height - textRenderer.fontHeight) / 2;
        context.drawText(textRenderer, Text.literal(displayText), textX, textY, 0xFFFFFF, false);

        // Dropdown arrow
        String arrow = isOpen ? "▲" : "▼";
        int arrowX = x + width - textRenderer.getWidth(arrow) - DROPDOWN_PADDING;
        context.drawText(textRenderer, Text.literal(arrow).formatted(Formatting.GRAY), arrowX, textY, 0xAAAAAA, false);

        // Dropdown list when open
        if (isOpen) {
            renderDropdownList(context, mouseX, mouseY);
        }
    }

    private void renderDropdownList(DrawContext context, int mouseX, int mouseY) {
        int listHeight = Math.min(options.size(), MAX_VISIBLE_ITEMS) * ITEM_HEIGHT;
        int listY = y + height;

        // Background
        context.fill(x, listY, x + width, listY + listHeight, 0xFF1A1A1A);
        context.drawBorder(x, listY, width, listHeight, 0xFF444444);

        // Options
        for (int i = 0; i < Math.min(options.size(), MAX_VISIBLE_ITEMS); i++) {
            String option = options.get(i);
            int itemY = listY + i * ITEM_HEIGHT;

            boolean isHovered = mouseX >= x && mouseX <= x + width &&
                    mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT;
            boolean isSelected = option.equals(selectedValue);

            // Item background
            if (isHovered) {
                context.fill(x, itemY, x + width, itemY + ITEM_HEIGHT, 0xFF2A4A2A);
            } else if (isSelected) {
                context.fill(x, itemY, x + width, itemY + ITEM_HEIGHT, 0xFF1A3A1A);
            }

            // Item text
            String displayText = option.isEmpty() ? "All" : option;
            Formatting color = isSelected ? Formatting.GREEN : Formatting.WHITE;
            int itemTextX = x + DROPDOWN_PADDING;
            int itemTextY = itemY + (ITEM_HEIGHT - textRenderer.fontHeight) / 2;

            context.drawText(textRenderer, Text.literal(displayText).formatted(color),
                    itemTextX, itemTextY, color.getColorValue() != null ? color.getColorValue() : 0xFFFFFF, false);

        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false; // Only left click

        // Check if clicking on main button
        if (isHovered((int)mouseX, (int)mouseY)) {
            isOpen = !isOpen;
            return true;
        }

        // Check if clicking on dropdown items
        if (isOpen) {
            int listY = y + height;
            int listHeight = Math.min(options.size(), MAX_VISIBLE_ITEMS) * ITEM_HEIGHT;

            if (mouseX >= x && mouseX <= x + width &&
                    mouseY >= listY && mouseY <= listY + listHeight) {

                int clickedIndex = (int)((mouseY - listY) / ITEM_HEIGHT);
                if (clickedIndex >= 0 && clickedIndex < options.size()) {
                    selectedValue = options.get(clickedIndex);
                    onSelectionChanged.accept(selectedValue);
                    isOpen = false;
                    return true;
                }
            } else {
                // Click outside - close dropdown
                isOpen = false;
            }
        }

        return false;
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void setSelected(String value) {
        if (options.contains(value)) {
            this.selectedValue = value;
        }
    }

    public String getSelected() {
        return selectedValue;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void close() {
        this.isOpen = false;
    }

    public int getTotalHeight() {
        if (isOpen) {
            return height + Math.min(options.size(), MAX_VISIBLE_ITEMS) * ITEM_HEIGHT;
        }
        return height;
    }
}