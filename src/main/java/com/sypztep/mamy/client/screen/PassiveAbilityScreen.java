package com.sypztep.mamy.client.screen;

import com.sypztep.mamy.client.screen.widget.ScrollBehavior;
import com.sypztep.mamy.client.toast.ToastRenderer;
import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import com.sypztep.mamy.common.util.TextUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class PassiveAbilityScreen extends Screen {
    // UI constants
    private static final int CONTENT_PADDING = 50;
    private static final int SECTION_SPACING = 10;
    private static final int SUMMARY_HEIGHT = 25;
    private static final int MIN_ITEM_HEIGHT = 25;
    private static final int ITEM_PADDING = 8;

    // Component data
    private final LivingLevelComponent playerStats;
    private final PassiveAbilityManager abilityManager;

    // UI state
    private final List<PassiveAbility> displayedAbilities;
    private PassiveAbility selectedAbility;
    private final List<Integer> itemHeights; // Store calculated heights for each item

    // Scroll behaviors
    private final ScrollBehavior listScrollBehavior;
    private final ScrollBehavior detailsScrollBehavior;

    public PassiveAbilityScreen(MinecraftClient client) {
        super(Text.literal("Passive Abilities"));
        assert client.player != null;
        this.playerStats = ModEntityComponents.LIVINGLEVEL.get(client.player);
        this.abilityManager = playerStats.getPassiveAbilityManager();
        this.displayedAbilities = ModPassiveAbilities.getAbilitiesGroupedByStatThenLevel();
        this.itemHeights = new ArrayList<>();

        // Initialize scroll behaviors
        this.listScrollBehavior = new ScrollBehavior().setScrollbarWidth(8).setScrollbarPadding(3).setMinHandleSize(24);

        this.detailsScrollBehavior = new ScrollBehavior().setScrollbarWidth(8).setScrollbarPadding(3).setMinHandleSize(24);
    }

    @Override
    protected void init() {
        super.init();

        // Calculate item heights and update scroll bounds
        calculateItemHeights();
        updateScrollBounds();
    }

    private void calculateItemHeights() {
        itemHeights.clear();

        // Calculate available width for requirements text (accounting for 2:2 layout)
        int contentWidth = width - (CONTENT_PADDING * 2);
        int listWidth = (contentWidth - SECTION_SPACING) / 2;
        int textWidth = listWidth - 30 - ITEM_PADDING * 2; // Account for padding and margins

        for (PassiveAbility ability : displayedAbilities) {
            int height = MIN_ITEM_HEIGHT;

            // Calculate height needed for requirements text
            String reqSummary = getRequirementSummary(ability);
            if (!reqSummary.isEmpty()) {
                List<String> reqLines = TextUtil.wrapText(textRenderer, reqSummary, textWidth);
                // Add height for requirement lines (beyond the first line that fits in MIN_ITEM_HEIGHT)
                if (reqLines.size() > 1) {
                    height += (reqLines.size() - 1) * (textRenderer.fontHeight + 2);
                }
            }

            // Add some padding
            height += ITEM_PADDING;
            itemHeights.add(height);
        }
    }

    private void updateScrollBounds() {
        int contentY = CONTENT_PADDING + SUMMARY_HEIGHT + SECTION_SPACING;
        int contentWidth = width - (CONTENT_PADDING * 2);
        int contentHeight = height - contentY - 20;

        // 2:2 layout instead of 2:1
        int listWidth = (contentWidth - SECTION_SPACING) / 2;
        int detailsWidth = (contentWidth - SECTION_SPACING) / 2;
        int detailsX = CONTENT_PADDING + listWidth + SECTION_SPACING;

        // Set bounds for list scroll behavior
        listScrollBehavior.setBounds(CONTENT_PADDING + 5, contentY + 10, listWidth - 10, contentHeight - 20);

        // Calculate total content height for list (sum of all item heights)
        int totalContentHeight = itemHeights.stream().mapToInt(Integer::intValue).sum();
        listScrollBehavior.setContentHeight(totalContentHeight);

        // Set bounds for details scroll behavior
        detailsScrollBehavior.setBounds(detailsX + 5, contentY + 10, detailsWidth - 10, contentHeight - 20);

        // Calculate details content height
        int detailsContentHeight = calculateDetailsContentHeight(detailsWidth - 20);
        detailsScrollBehavior.setContentHeight(detailsContentHeight);
    }

    private int calculateDetailsContentHeight(int maxWidth) {
        if (selectedAbility == null) return 0;

        int totalHeight = 0;

        // Status height
        totalHeight += textRenderer.fontHeight;

        // Description height
        List<String> descLines = TextUtil.wrapText(textRenderer, selectedAbility.getDescription().getString(), maxWidth);
        totalHeight += descLines.size() * (textRenderer.fontHeight + 2) + 10;
        int magicNumber = 14; // it all screen pad
        // Requirements list
        totalHeight += selectedAbility.getRequirements().size() * (textRenderer.fontHeight + 2) + magicNumber;

        // Effects description
        String effectDesc = getEffectDescription(selectedAbility);
        List<String> effectLines = TextUtil.wrapText(textRenderer, effectDesc, maxWidth);
        totalHeight += effectLines.size() * (textRenderer.fontHeight + 2);

        // Add some extra padding
        totalHeight += 50;

        return totalHeight;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (itemHeights.size() != displayedAbilities.size()) {
            calculateItemHeights();
        }

        // Update scroll bounds (in case of window resize)
        updateScrollBounds();

        // Set dark background
        DrawContextUtils.fillScreen(context, 0xF0121212);

        // Main content area
        int contentX = CONTENT_PADDING;
        int contentY = CONTENT_PADDING;
        int contentWidth = width - (CONTENT_PADDING * 2);
        int contentHeight = height - contentY - 20;

        // Render title
        renderTitle(context, contentX, contentY - 25, contentWidth);

        // 2:2 layout - equal split
        int listWidth = (contentWidth - SECTION_SPACING) / 2;
        int detailsWidth = (contentWidth - SECTION_SPACING) / 2;
        int detailsX = contentX + listWidth + SECTION_SPACING;

        // Render summary info at top
        renderPassiveSummary(context, contentX, contentY, contentWidth);

        // Render ability list with scroll behavior
        renderPassiveAbilityList(context, contentX, contentY + SUMMARY_HEIGHT + SECTION_SPACING, listWidth, contentHeight - SUMMARY_HEIGHT - SECTION_SPACING, mouseX, mouseY, delta);

        // Render details panel with scroll behavior
        if (selectedAbility != null) {
            renderPassiveAbilityDetails(context, detailsX, contentY + SUMMARY_HEIGHT + SECTION_SPACING, detailsWidth, contentHeight - SUMMARY_HEIGHT - SECTION_SPACING, mouseX, mouseY, delta);
        }

        // Render toasts over screen
        renderToastsOverScreen(context, delta);
    }

    private void renderTitle(DrawContext context, int x, int y, int width) {
        Text titleText = Text.literal("Passive Abilities").formatted(Formatting.GOLD, Formatting.BOLD);
        int titleX = x + (width - textRenderer.getWidth(titleText)) / 2;

        context.drawText(textRenderer, titleText, titleX, y, 0xFFD700, false);

        // Decorative lines
        int lineY = y + textRenderer.fontHeight + 3;
        DrawContextUtils.renderHorizontalLineWithCenterGradient(context, x, lineY, width, 1, 400, 0xFFFFFFFF, 0, 1.0f);
    }

    private void renderPassiveSummary(DrawContext context, int x, int y, int width) {
        if (abilityManager == null) return;

        int unlockedCount = abilityManager.getUnlockedAbilities().size();
        int activeCount = abilityManager.getActiveAbilities().size();
        int totalCount = displayedAbilities.size();

        // Background with border
        context.fill(x, y, x + width, y + SUMMARY_HEIGHT, 0xFF1E1E1E);
        context.fill(x, y, x + width, y + 1, 0xFF4CAF50);
        context.fill(x, y + SUMMARY_HEIGHT - 1, x + width, y + SUMMARY_HEIGHT, 0xFF4CAF50);

        // Summary text
        String summaryText = String.format("Unlocked: %d/%d | Active: %d ", unlockedCount, totalCount, activeCount);

        context.drawText(textRenderer, Text.literal(summaryText).formatted(Formatting.WHITE), x + 10, y + 8, 0xFFFFFF, false);

        // Progress bar
        int barX = x + width - 150;
        int barY = y + 10;
        int barWidth = 120;
        int barHeight = 6;
        float progress = totalCount > 0 ? (float) unlockedCount / totalCount : 0;

        // Progress bar background
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);

        // Progress bar fill
        if (progress > 0) {
            int progressWidth = (int) (barWidth * progress);
            context.fill(barX, barY, barX + progressWidth, barY + barHeight, 0xFF4CAF50);
        }

        // Progress text
        String progressText = String.format("%.1f%%", progress * 100);
        int progressTextX = barX + barWidth + 5;
        context.drawText(textRenderer, Text.literal(progressText).formatted(Formatting.GRAY), progressTextX, y + 8, 0xAAAAAA, false);
    }

    private void renderPassiveAbilityList(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {

        // Background with border
        context.fill(x, y, x + width, y + height, 0xFF1E1E1E);
        context.drawBorder(x, y, width, height, 0xFF444444);

        if (abilityManager == null) return;

        // Update and render scroll behavior
        listScrollBehavior.update(context, mouseX, mouseY, delta);

        // Enable scissor for content clipping
        listScrollBehavior.enableScissor(context);

        // Calculate visible items with dynamic heights
        int scrollOffset = listScrollBehavior.getScrollOffset();
        int currentY = y + 10 - scrollOffset;

        // Render abilities with dynamic heights
        for (int i = 0; i < displayedAbilities.size(); i++) {
            PassiveAbility ability = displayedAbilities.get(i);
            int itemHeight = itemHeights.get(i);

            if (currentY + itemHeight < y || currentY > y + height) {
                currentY += itemHeight;
                continue;
            }

            boolean unlocked = abilityManager.isUnlocked(ability);
            boolean active = abilityManager.isActive(ability);
            boolean canUnlock = ability.meetsRequirements(client.player);
            boolean isHovered = mouseX >= x + 5 && mouseX <= x + width - 25 && mouseY >= currentY && mouseY <= currentY + itemHeight - 2;
            boolean isSelected = ability == selectedAbility;

            renderAbilityListItem(context, ability, x + 5, currentY, width - 30, itemHeight - 2, unlocked, active, canUnlock, isHovered, isSelected);

            currentY += itemHeight;
        }

        // Disable scissor
        listScrollBehavior.disableScissor(context);
    }

    private void renderAbilityListItem(DrawContext context, PassiveAbility ability, int x, int y, int width, int height, boolean unlocked, boolean active, boolean canUnlock, boolean isHovered, boolean isSelected) {

        // Background for item
        int itemBg = 0xFF1A1A1A;
        if (isSelected) itemBg = 0xFF2A4A2A;
        else if (isHovered) itemBg = 0xFF2A2A2A;

        context.fill(x, y, x + width, y + height, itemBg);

        // Status indicator
        String statusIcon;
        Formatting color;
        if (active) {
            statusIcon = "✓";
            color = Formatting.GREEN;
        } else if (unlocked) {
            statusIcon = "⚠";
            color = Formatting.YELLOW;
        } else if (canUnlock) {
            statusIcon = "!";
            color = Formatting.AQUA;
        } else {
            statusIcon = "✗";
            color = Formatting.GRAY;
        }

        // Render ability info
        int textX = x + ITEM_PADDING;
        int currentTextY = y + 5;

        // Name with status
        Text abilityName = Text.literal(statusIcon + " " + ability.getDisplayName().getString()).formatted(color);
        context.drawText(textRenderer, abilityName, textX, currentTextY, color.getColorValue() != null ? color.getColorValue() : 0xFFFFFF, false);

        currentTextY += textRenderer.fontHeight + 3;

        // Requirements summary (wrapped for long text)
        String reqSummary = getRequirementSummary(ability);
        if (!reqSummary.isEmpty()) {
            List<String> reqLines = TextUtil.wrapText(textRenderer, reqSummary, width - ITEM_PADDING * 2);
            for (String line : reqLines) {
                context.drawText(textRenderer, Text.literal(line).formatted(Formatting.DARK_GRAY), textX, currentTextY, 0x888888, false);
                currentTextY += textRenderer.fontHeight + 2;
            }
        }

        // Selection highlight
        if (isSelected) {
            context.fill(x + width - 7, y, x + width, y + height, 0xFF4CAF50);
        }
    }

    private void renderPassiveAbilityDetails(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
        // Background with border
        context.fill(x, y, x + width, y + height, 0xFF1E1E1E);
        context.drawBorder(x, y, width, height, 0xFF444444);

        if (selectedAbility == null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Select an ability").formatted(Formatting.GRAY), x + width / 2, y + height / 2, 0x888888);
            return;
        }

        // Update and render scroll behavior for details
        detailsScrollBehavior.update(context, mouseX, mouseY, delta);

        // Enable scissor for content clipping
        detailsScrollBehavior.enableScissor(context);

        int scrollOffset = detailsScrollBehavior.getScrollOffset();
        int currentY = y + 10 - scrollOffset;
        int textX = x + 10;
        int maxWidth = width - 20;

        boolean unlocked = abilityManager != null && abilityManager.isUnlocked(selectedAbility);
        boolean active = abilityManager != null && abilityManager.isActive(selectedAbility);
        boolean canUnlock = selectedAbility.meetsRequirements(client.player);

        // Title with status
        String statusSuffix = active ? " ✓" : (unlocked ? " ⚠" : (canUnlock ? " !" : " ✗"));
        Formatting titleColor = active ? Formatting.GREEN : (unlocked ? Formatting.YELLOW : (canUnlock ? Formatting.AQUA : Formatting.GRAY));

        Text titleText = selectedAbility.getDisplayName().copy().formatted(titleColor, Formatting.BOLD).append(Text.literal(statusSuffix).formatted(Formatting.WHITE));

        context.drawText(textRenderer, titleText, textX, currentY, titleColor.getColorValue() != null ? titleColor.getColorValue() : 0xFFFFFF, false);
        currentY += textRenderer.fontHeight + 8;

        // Status description
        String statusText = active ? "§a● ACTIVE" : (unlocked ? "§e● UNLOCKED" : (canUnlock ? "§b● CAN UNLOCK!" : "§7● LOCKED"));
        context.drawText(textRenderer, Text.literal(statusText), textX, currentY, 0xFFFFFF, false);
        currentY += textRenderer.fontHeight + 10;

        // Description
        List<String> descLines = TextUtil.wrapText(textRenderer, selectedAbility.getDescription().getString(), maxWidth);
        for (String line : descLines) {
            context.drawText(textRenderer, Text.literal(line).formatted(Formatting.GRAY), textX, currentY, 0xAAAAAA, false);
            currentY += textRenderer.fontHeight + 2;
        }
        currentY += 10;

        // Requirements header
        context.drawText(textRenderer, Text.literal("Requirements:").formatted(Formatting.GOLD), textX, currentY, 0xFFD700, false);
        currentY += textRenderer.fontHeight + 5;

        // Requirements list
        for (var req : selectedAbility.getRequirements().entrySet()) {
            var statType = req.getKey();
            int required = req.getValue();
            int current = playerStats.getStatValue(statType);

            boolean meets = current >= required;
            Formatting reqColor = meets ? Formatting.GREEN : Formatting.RED;
            String reqText = String.format("  %s: %d/%d", statType.getAka(), current, required);

            context.drawText(textRenderer, Text.literal(reqText).formatted(reqColor), textX, currentY, reqColor.getColorValue() != null ? reqColor.getColorValue() : 0xFFFFFF, false);
            currentY += textRenderer.fontHeight + 2;
        }

        // Effect preview
        currentY += 10;
        context.drawText(textRenderer, Text.literal("Effects:").formatted(Formatting.GOLD), textX, currentY, 0xFFD700, false);
        currentY += textRenderer.fontHeight + 5;

        // Show effect description
        String effectDesc = getEffectDescription(selectedAbility);
        List<String> effectLines = TextUtil.wrapText(textRenderer, effectDesc, maxWidth);
        for (String line : effectLines) {
            context.drawText(textRenderer, Text.literal(line).formatted(Formatting.AQUA), textX, currentY, 0x55FFFF, false);
            currentY += textRenderer.fontHeight + 2;
        }

        // Disable scissor
        detailsScrollBehavior.disableScissor(context);
    }

    private void renderToastsOverScreen(DrawContext context, float delta) {
        float deltaTime = delta / 20.0f;
        ToastRenderer.renderToasts(context, this.width, deltaTime);
    }

    private String getRequirementSummary(PassiveAbility ability) {
        StringBuilder summary = new StringBuilder();
        var requirements = ability.getRequirements();

        for (var entry : requirements.entrySet()) {
            if (!summary.isEmpty()) summary.append(", ");
            summary.append(entry.getKey().getAka()).append(" ").append(entry.getValue());
        }

        return summary.toString();
    }

    private String getEffectDescription(PassiveAbility ability) {
        List<AttributeModification> modifications = ability.getAttributeModifications();
        if (modifications.isEmpty()) return "No specific attribute bonuses.";

        StringBuilder desc = new StringBuilder();
        for (var mod : modifications) {
            if (!desc.isEmpty()) desc.append(", ");
            String attributeName = mod.attribute().getIdAsString();
            desc.append(attributeName.substring(attributeName.lastIndexOf('.') + 1));
        }

        return desc.toString();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle details scroll behavior click first
        if (detailsScrollBehavior.handleMouseClick(mouseX, mouseY, button)) {
            return true;
        }

        // Handle list scroll behavior click
        if (listScrollBehavior.handleMouseClick(mouseX, mouseY, button)) {
            return true;
        }

        if (button != 0) return super.mouseClicked(mouseX, mouseY, button); // Left click only

        int contentY = CONTENT_PADDING + SUMMARY_HEIGHT + SECTION_SPACING;
        int contentWidth = width - (CONTENT_PADDING * 2);
        int listWidth = (contentWidth - SECTION_SPACING) / 2; // Updated for 2:2 layout
        int contentHeight = height - contentY - 20;

        // Use the same bounds as hover detection
        int itemAreaX = CONTENT_PADDING + 5;
        int itemAreaWidth = listWidth - 30; // This makes the right bound listX + listWidth - 25

        // Check if clicking in the same area as hover detection
        if (mouseX >= itemAreaX && mouseX <= itemAreaX + itemAreaWidth && mouseY >= contentY && mouseY <= contentY + contentHeight) {

            int scrollOffset = listScrollBehavior.getScrollOffset();

            // Calculate the same way as in hover detection with dynamic heights
            int adjustedMouseY = (int) mouseY;
            int currentY = contentY + 10 - scrollOffset; // Same as hover calculation

            // Find which item was clicked by iterating through items like hover does
            for (int i = 0; i < displayedAbilities.size(); i++) {
                int itemHeight = itemHeights.get(i);
                int itemY = currentY;
                int actualItemHeight = itemHeight - 2; // Same as hover detection

                if (adjustedMouseY >= itemY && adjustedMouseY <= itemY + actualItemHeight) {
                    if (itemY + actualItemHeight >= contentY && itemY <= contentY + contentHeight) {
                        selectedAbility = displayedAbilities.get(i);
                        listScrollBehavior.scrollToItem(i, itemHeight);
                        return true;
                    }
                    break;
                }
                currentY += itemHeight;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (detailsScrollBehavior.handleMouseDrag(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }

        if (listScrollBehavior.handleMouseDrag(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        detailsScrollBehavior.handleMouseRelease(mouseX, mouseY, button);
        listScrollBehavior.handleMouseRelease(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (detailsScrollBehavior.handleScroll(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        if (listScrollBehavior.handleScroll(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}