package com.sypztep.mamy.client.screen;

import com.sypztep.mamy.client.screen.widget.ScrollBehavior;
import com.sypztep.mamy.client.toast.ToastRenderer;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.network.server.ClassEvolutionPayloadC2S;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.classes.PlayerClassManager;
import com.sypztep.mamy.common.init.ModClassesSkill;
import com.sypztep.mamy.common.util.TextUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class ClassEvolutionScreen extends Screen {
    // UI constants
    private static final int CONTENT_PADDING = 50;
    private static final int SECTION_SPACING = 10;
    private static final int SUMMARY_HEIGHT = 25;
    private static final int MIN_ITEM_HEIGHT = 25;
    private static final int ITEM_PADDING = 8;

    // Component data
    private final PlayerClassManager classManager;

    // UI state
    private final List<PlayerClass> displayedClasses;
    private PlayerClass selectedClass;
    private final List<Integer> itemHeights; // Store calculated heights for each item
    private PlayerClass clickedOnceClass = null; // For double-click confirmation

    // Scroll behaviors
    private final ScrollBehavior listScrollBehavior;
    private final ScrollBehavior detailsScrollBehavior;

    public ClassEvolutionScreen(MinecraftClient client) {
        super(Text.literal("Class Evolution"));
        assert client.player != null;
        this.classManager = ModEntityComponents.PLAYERCLASS.get(client.player).getClassManager();

        // Load available evolutions and transcendences
        this.displayedClasses = new ArrayList<>();
        this.displayedClasses.addAll(classManager.getAvailableEvolutions());
        this.displayedClasses.addAll(classManager.getAvailableTranscendence());
        this.itemHeights = new ArrayList<>();

        this.displayedClasses.sort(Comparator.comparingInt(PlayerClass::getBranch).thenComparingInt(PlayerClass::getTier).thenComparing(pc -> pc.isTranscendent() ? 1 : 0));

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

        for (PlayerClass clazz : displayedClasses) {
            int height = MIN_ITEM_HEIGHT;

            // Calculate height needed for description text
            String description = clazz.getDescription();
            if (description != null && !description.isEmpty()) {
                List<String> descLines = TextUtil.wrapText(textRenderer, description, textWidth);
                // Add height for description lines (beyond the first line that fits in MIN_ITEM_HEIGHT)
                if (descLines.size() > 1) {
                    height += (descLines.size() - 1) * (textRenderer.fontHeight + 2);
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
        if (selectedClass == null) return 0;

        int totalHeight = 0;

        // Status height
        totalHeight += textRenderer.fontHeight;

        // Description height
        List<String> descLines = TextUtil.wrapText(textRenderer, selectedClass.getDescription(), maxWidth);
        totalHeight += descLines.size() * (textRenderer.fontHeight + 2) + 10;
        int magicNumber = 14; // it all screen pad

        // Requirements list
        totalHeight += selectedClass.getRequirements().size() * (textRenderer.fontHeight + 2) + magicNumber;

        // Attribute modifiers description
        totalHeight += selectedClass.getAttributeModifiers().size() * (textRenderer.fontHeight + 2);

        // Add some extra padding (including instruction text)
        totalHeight += 70;

        return totalHeight;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Recalculate item heights if needed (in case of window resize)
        if (itemHeights.size() != displayedClasses.size()) {
            calculateItemHeights();
        }

        updateScrollBounds();

        // Set dark background
        context.fill(0, 0, width, height, 0xF0121212);

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
        renderEvolutionSummary(context, contentX, contentY, contentWidth, delta);

        // Render class list with scroll behavior
        renderClassEvolutionList(context, contentX, contentY + SUMMARY_HEIGHT + SECTION_SPACING, listWidth, contentHeight - SUMMARY_HEIGHT - SECTION_SPACING, mouseX, mouseY, delta);

        // Render details panel with scroll behavior
        if (selectedClass != null) {
            renderClassEvolutionDetails(context, detailsX, contentY + SUMMARY_HEIGHT + SECTION_SPACING, detailsWidth, contentHeight - SUMMARY_HEIGHT - SECTION_SPACING, mouseX, mouseY, delta);
        }

        renderToastsOverScreen(context, delta);
    }

    private void renderTitle(DrawContext context, int x, int y, int width) {
        Text titleText = Text.literal("Class Evolution").formatted(Formatting.GOLD, Formatting.BOLD);
        int titleX = x + (width - textRenderer.getWidth(titleText)) / 2;

        context.drawTextWithShadow(textRenderer, titleText, titleX, y, 0xFFD700);

        // Simple horizontal line
        int lineY = y + textRenderer.fontHeight + 3;
        context.fill(x, lineY, x + width, lineY + 1, 0xFFFFFFFF);
    }

    private void renderEvolutionSummary(DrawContext context, int x, int y, int width, float delta) {
        PlayerClass currentClass = classManager.getCurrentClass();
        int level = classManager.getClassLevel();
        boolean ready = classManager.isReadyForEvolution() || classManager.isReadyForTranscendence();

        // Background with border
        context.fill(x, y, x + width, y + SUMMARY_HEIGHT, 0xFF1E1E1E);
        context.fill(x, y, x + width, y + 1, 0xFF4CAF50);
        context.fill(x, y + SUMMARY_HEIGHT - 1, x + width, y + SUMMARY_HEIGHT, 0xFF4CAF50);

        // Summary text
        String summaryText = String.format("Current: %s (Class Lv.%d)", currentClass.getDisplayName(), level);
        context.drawTextWithShadow(textRenderer, Text.literal(summaryText).formatted(Formatting.WHITE), x + 10, y + 8, 0xFFFFFF);

        // Simple status indicator
        String statusText;
        int statusColor;
        if (ready) {
            statusText = "✓ Ready!";
            statusColor = 0xFF4CAF50;
        } else if (classManager.reachCap()) {
            statusText = "✓ Reach Cap!";
            statusColor = 0xFF4CAF50;
        } else {
            statusText = String.format("Requires Lv.%d", classManager.getEvolutionRequiredLevel());
            statusColor = 0xFFFF6B6B;
        }

        int statusTextWidth = textRenderer.getWidth(statusText);
        context.drawTextWithShadow(textRenderer, Text.literal(statusText), x + width - statusTextWidth - 10, y + 8, statusColor);
    }

    private void renderClassEvolutionList(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {

        // Background with border
        context.fill(x, y, x + width, y + height, 0xFF1E1E1E);
        context.drawBorder(x, y, width, height, 0xFF444444);

        if (displayedClasses.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("No evolutions available").formatted(Formatting.GRAY), x + width / 2, y + height / 2, 0x888888);
            return;
        }

        // Update and render scroll behavior
        listScrollBehavior.update(context, mouseX, mouseY, delta);

        // Enable scissor for content clipping
        listScrollBehavior.enableScissor(context);

        // Calculate visible items with dynamic heights
        int scrollOffset = listScrollBehavior.getScrollOffset();
        int currentY = y + 10 - scrollOffset;

        // Render classes with dynamic heights
        for (int i = 0; i < displayedClasses.size(); i++) {
            PlayerClass clazz = displayedClasses.get(i);
            int itemHeight = itemHeights.get(i);

            if (currentY + itemHeight < y || currentY > y + height) {
                currentY += itemHeight;
                continue;
            }

            boolean isHovered = mouseX >= x + 5 && mouseX <= x + width - 25 && mouseY >= currentY && mouseY <= currentY + itemHeight - 2;
            boolean isSelected = clazz == selectedClass;

            renderClassListItem(context, clazz, x + 5, currentY, width - 30, itemHeight - 2, isHovered, isSelected);

            currentY += itemHeight;
        }

        // Disable scissor
        listScrollBehavior.disableScissor(context);
    }

    private void renderClassListItem(DrawContext context, PlayerClass clazz, int x, int y, int width, int height, boolean isHovered, boolean isSelected) {

        // Background for item
        int itemBg = 0xFF1A1A1A;
        if (isSelected) {
            itemBg = clazz.isTranscendent() ? 0xFF4A4A2A : 0xFF2A4A2A; // Gold for transcendent, green for normal
        } else if (isHovered) {
            itemBg = 0xFF2A2A2A;
        }

        context.fill(x, y, x + width, y + height, itemBg);

        // Status indicator
        String statusIcon;
        Formatting color;
        if (clazz.isTranscendent()) {
            statusIcon = "⚡";
            color = Formatting.GOLD;
        } else {
            statusIcon = "➤";
            color = Formatting.GREEN;
        }

        // Render class info
        int textX = x + ITEM_PADDING;
        int currentTextY = y + 5;

        // Name with status
        Text className = Text.literal(statusIcon + " " + clazz.getDisplayName()).formatted(color);
        context.drawTextWithShadow(textRenderer, className, textX, currentTextY, color.getColorValue() != null ? color.getColorValue() : 0xFFFFFF);

        currentTextY += textRenderer.fontHeight + 3;

        // Tier and code info
        String tierInfo = String.format("[%s] Tier %d", clazz.getClassCode(), clazz.getTier());
        context.drawTextWithShadow(textRenderer, Text.literal(tierInfo).formatted(Formatting.DARK_GRAY), textX, currentTextY, 0x888888);
        currentTextY += textRenderer.fontHeight + 2;

        // Description (wrapped for long text)
        String description = clazz.getDescription();
        if (description != null && !description.isEmpty()) {
            String shortLine = TextUtil.warpLine(description, width - ITEM_PADDING * 2, textRenderer);

            context.drawTextWithShadow(textRenderer, Text.literal(shortLine).formatted(Formatting.GRAY), textX, currentTextY, 0xAAAAAA);
        }


        // Selection highlight
        if (isSelected) {
            context.fill(x + width - 7, y, x + width, y + height, clazz.isTranscendent() ? 0xFFFFD700 : 0xFF4CAF50);
        }
    }

    private void renderClassEvolutionDetails(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
        // Background with border
        context.fill(x, y, x + width, y + height, 0xFF1E1E1E);
        context.drawBorder(x, y, width, height, 0xFF444444);

        if (selectedClass == null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Select an evolution").formatted(Formatting.GRAY), x + width / 2, y + height / 2, 0x888888);
            return;
        }

        // Update and render scroll behavior for details
        detailsScrollBehavior.update(context, mouseX, mouseY, delta);

        // Enable scissor for content clipping
        detailsScrollBehavior.enableScissor(context);

        int scrollOffset = detailsScrollBehavior.getScrollOffset();
        int currentY = y + 10 - scrollOffset;
        int textX = x + 10;
        int maxWidth = width - 26;

        boolean ready = classManager.isReadyForEvolution() || classManager.isReadyForTranscendence();
        boolean requiresDoubleClick = clickedOnceClass != selectedClass;

        // Title with status - wrapped
        String statusSuffix = ready ? " ✓" : " ✗";
        Formatting titleColor = selectedClass.isTranscendent() ? Formatting.GOLD : Formatting.GREEN;

        String titleString = selectedClass.getFormattedName().getString() + statusSuffix;
        List<String> titleLines = TextUtil.wrapText(textRenderer, titleString, maxWidth);
        for (String line : titleLines) {
            Text titleText = Text.literal(line).formatted(titleColor, Formatting.BOLD);
            context.drawTextWithShadow(textRenderer, titleText, textX, currentY, titleColor.getColorValue() != null ? titleColor.getColorValue() : 0xFFFFFF);
            currentY += textRenderer.fontHeight + 2;
        }
        currentY += 6;

        // Status description - wrapped
        String statusText;
        if (!ready) {
            statusText = "§c● REQUIREMENT NOT MET";
        } else if (requiresDoubleClick) {
            statusText = "§e● CLICK TO CONFIRM";
        } else {
            statusText = "§a● READY TO EVOLVE";
        }

        if (selectedClass.isTranscendent()) {
            statusText += " (TRANSCENDENT)";
        }

        List<String> statusLines = TextUtil.wrapText(textRenderer, statusText, maxWidth);
        for (String line : statusLines) {
            context.drawTextWithShadow(textRenderer, Text.literal(line), textX, currentY, 0xFFFFFF);
            currentY += textRenderer.fontHeight + 2;
        }
        currentY += 8;

        // Description
        String description = selectedClass.getDescription();
        if (description != null && !description.isEmpty()) {
            List<String> descLines = TextUtil.wrapText(textRenderer, description, maxWidth);
            for (String line : descLines) {
                context.drawTextWithShadow(textRenderer, Text.literal(line).formatted(Formatting.GRAY), textX, currentY, 0xAAAAAA);
                currentY += textRenderer.fontHeight + 2;
            }
            currentY += 10;
        }

        // Requirements header
        if (!selectedClass.getRequirements().isEmpty()) {
            context.drawTextWithShadow(textRenderer, Text.literal("Requirements:").formatted(Formatting.GOLD), textX, currentY, 0xFFD700);
            currentY += textRenderer.fontHeight + 5;

            // Default class requirements from PlayerClass
            for (var req : selectedClass.getRequirements()) {
                PlayerClass requiredClass = req.previousClass();
                int requiredLevel = req.requiredLevel();
                PlayerClass currentClass = classManager.getCurrentClass();
                int currentLevel = classManager.getClassLevel();

                boolean meets = requiredClass == currentClass && currentLevel >= requiredLevel;
                Formatting reqColor = meets ? Formatting.GREEN : Formatting.RED;
                String reqText = String.format("  %s Level %d", requiredClass.getDisplayName(), requiredLevel);

                List<String> reqLines = TextUtil.wrapText(textRenderer, reqText, maxWidth);
                for (String line : reqLines) {
                    context.drawTextWithShadow(textRenderer, Text.literal(line).formatted(reqColor), textX, currentY, reqColor.getColorValue() != null ? reqColor.getColorValue() : 0xFFFFFF);
                    currentY += textRenderer.fontHeight + 2;
                }
            }

            // ADDITIONAL CUSTOM REQUIREMENTS based on current class
            PlayerClass currentClass = classManager.getCurrentClass();

            if (currentClass.getTier() == 0) { // Novice evolving
                int basicSkillLevel = classManager.getSkillLevel(ModClassesSkill.BASICSKILL);
                boolean basicSkillMet = basicSkillLevel == 10;
                Formatting basicSkillColor = basicSkillMet ? Formatting.GREEN : Formatting.RED;
                String basicSkillText = String.format("  Basic Skill Level 10 (Current: %d)", basicSkillLevel);

                List<String> basicSkillLines = TextUtil.wrapText(textRenderer, basicSkillText, maxWidth);
                for (String line : basicSkillLines) {
                    context.drawTextWithShadow(textRenderer, Text.literal(line).formatted(basicSkillColor), textX, currentY, basicSkillColor.getColorValue() != null ? basicSkillColor.getColorValue() : 0xFFFFFF);
                    currentY += textRenderer.fontHeight + 2;
                }
            } else {
                // Other classes - must spend all skill points
                int skillPoints = classManager.getClassStatPoints();
                boolean skillPointsMet = skillPoints == 0;
                Formatting skillPointsColor = skillPointsMet ? Formatting.GREEN : Formatting.RED;
                String skillPointsText = skillPointsMet ? "  All skill points spent ✓" : String.format("  Must spend %d remaining skill points", skillPoints);

                List<String> skillPointsLines = TextUtil.wrapText(textRenderer, skillPointsText, maxWidth);
                for (String line : skillPointsLines) {
                    context.drawTextWithShadow(textRenderer, Text.literal(line).formatted(skillPointsColor), textX, currentY, skillPointsColor.getColorValue() != null ? skillPointsColor.getColorValue() : 0xFFFFFF);
                    currentY += textRenderer.fontHeight + 2;
                }
            }

            currentY += 10;
        }

        // Enhanced version of your attribute bonuses display
        if (!selectedClass.getAttributeModifiers().isEmpty()) {
            context.drawTextWithShadow(textRenderer, Text.literal("Class Bonus").formatted(Formatting.GOLD), textX, currentY, 0xFFD700);
            currentY += textRenderer.fontHeight + 5;

            for (var entry : selectedClass.getAttributeModifiers().entrySet()) {
                var attribute = entry.getKey();
                double value = entry.getValue();

                String attrName = Text.translatable(attribute.value().getTranslationKey()).getString();
                String growthDesc = selectedClass.getGrowthDescription(attribute);

                // Base bonus in white/light gray
                String baseName = String.format(" %s:", attrName);
                String bonusBonus = String.format(" %+.1f", value);
                context.drawTextWithShadow(textRenderer, Text.literal(baseName).formatted(Formatting.WHITE).append(Text.literal(bonusBonus).formatted(Formatting.YELLOW).formatted(Formatting.ITALIC)), textX, currentY, 0xFFFFFF);

                // Growth description in green on the same line if it fits, otherwise new line
                if (!growthDesc.isEmpty()) {
                    String growthText = " (" + growthDesc + ")";
                    int baseWidth = textRenderer.getWidth(baseName + bonusBonus);
                    int growthWidth = textRenderer.getWidth(growthText);

                    if (baseWidth + growthWidth <= maxWidth) {
                        context.drawTextWithShadow(textRenderer, Text.literal(growthText).formatted(Formatting.GREEN).formatted(Formatting.ITALIC), textX + baseWidth, currentY, 0x55FF55);
                    } else {
                        currentY += textRenderer.fontHeight + 1;
                        context.drawTextWithShadow(textRenderer, Text.literal("    " + growthText).formatted(Formatting.GREEN), textX, currentY, 0x55FF55);
                    }
                }
                currentY += textRenderer.fontHeight + 2;
            }
            currentY += 7;
        }
        // Warning for transcendence - wrapped
        if (selectedClass.isTranscendent()) {
            String warningText = "⚠ WARNING: Transcendence will reset your level to 1!";
            List<String> warningLines = TextUtil.wrapText(textRenderer, warningText, maxWidth);
            for (String line : warningLines) {
                context.drawTextWithShadow(textRenderer, Text.literal(line).formatted(Formatting.RED, Formatting.BOLD), textX, currentY, 0xFFFF6B6B);
                currentY += textRenderer.fontHeight + 2;
            }
        }
        // Disable scissor
        detailsScrollBehavior.disableScissor(context);
    }

    private void renderToastsOverScreen(DrawContext context, float delta) {
        float deltaTime = delta / 20.0f;
        ToastRenderer.renderToasts(context, this.width, deltaTime);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (detailsScrollBehavior.handleMouseClick(mouseX, mouseY, button)) {
            return true;
        }

        if (listScrollBehavior.handleMouseClick(mouseX, mouseY, button)) {
            return true;
        }

        if (button != 0) return super.mouseClicked(mouseX, mouseY, button); // Left click only

        int contentX = CONTENT_PADDING;
        int contentY = CONTENT_PADDING + SUMMARY_HEIGHT + SECTION_SPACING;
        int contentWidth = width - (CONTENT_PADDING * 2);
        int listWidth = (contentWidth - SECTION_SPACING) / 2;
        int contentHeight = height - contentY - 20;

        // Use the same bounds as hover detection
        int itemAreaX = contentX + 5;
        int itemAreaWidth = listWidth - 30;

        // Check if clicking in the same area as hover detection
        if (mouseX >= itemAreaX && mouseX <= itemAreaX + itemAreaWidth && mouseY >= contentY && mouseY <= contentY + contentHeight) {

            int scrollOffset = listScrollBehavior.getScrollOffset();
            int adjustedMouseY = (int) mouseY;
            int currentY = contentY + 10 - scrollOffset;

            // Find which item was clicked
            for (int i = 0; i < displayedClasses.size(); i++) {
                int itemHeight = itemHeights.get(i);
                int itemY = currentY;
                int actualItemHeight = itemHeight - 2;

                if (adjustedMouseY >= itemY && adjustedMouseY <= itemY + actualItemHeight) {
                    if (itemY + actualItemHeight >= contentY && itemY <= contentY + contentHeight) {
                        selectedClass = displayedClasses.get(i);
                        clickedOnceClass = null; // Reset confirmation when selecting different class
                        listScrollBehavior.scrollToItem(i, itemHeight);
                        return true;
                    }
                    break;
                }
                currentY += itemHeight;
            }
        }

        // Check if clicking on the apply button
        if (selectedClass != null) {
            boolean ready = classManager.isReadyForEvolution() || classManager.isReadyForTranscendence();

            if (ready) {
                // Check button area
                int detailsX = contentX + listWidth + SECTION_SPACING;
                int detailsWidth = (contentWidth - SECTION_SPACING) / 2;

                if (mouseX >= detailsX && mouseX <= detailsX + detailsWidth) {
                    // Check if this is the first or second click
                    if (clickedOnceClass != selectedClass) {
                        // First click - set confirmation
                        clickedOnceClass = selectedClass;
                    } else {
                        // Second click - perform evolution
                        ClassEvolutionPayloadC2S.send(selectedClass.getId(), selectedClass.isTranscendent());
                        close();
                    }
                    return true;
                }
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