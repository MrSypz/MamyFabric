package com.sypztep.mamy.client.screen;

import com.sypztep.mamy.client.toast.ToastRenderer;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.payload.ClassEvolutionPayloadC2S;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.classes.PlayerClassManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sypztep.tyrannus.client.screen.widget.Animation;
import sypztep.tyrannus.client.screen.widget.ScrollBehavior;
import sypztep.tyrannus.client.util.AnimationUtils;
import sypztep.tyrannus.client.util.DrawContextUtils;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class ClassEvolutionScreen extends Screen {
    // Animation constants
    private static final float ANIMATION_DURATION = 0.8f;
    private static final float FINAL_Y_OFFSET = 30.0f;

    // UI constants
    private static final int CONTENT_PADDING = 50;
    private static final int SECTION_SPACING = 10;
    private static final int SUMMARY_HEIGHT = 25;
    private static final int MIN_ITEM_HEIGHT = 25;
    private static final int ITEM_PADDING = 8;

    // Component data
    private final LivingLevelComponent playerStats;
    private final PlayerClassComponent classComponent;
    private final PlayerClassManager classManager;

    // UI state
    private List<PlayerClass> displayedClasses;
    private PlayerClass selectedClass;
    private List<Integer> itemHeights; // Store calculated heights for each item
    private PlayerClass clickedOnceClass = null; // For double-click confirmation

    // Scroll behaviors
    private ScrollBehavior listScrollBehavior;
    private ScrollBehavior detailsScrollBehavior;

    // Animations
    private Animation fadeAnimation;
    private Animation slideAnimation;

    public ClassEvolutionScreen(MinecraftClient client) {
        super(Text.literal("Class Evolution"));
        assert client.player != null;
        this.playerStats = ModEntityComponents.LIVINGLEVEL.get(client.player);
        this.classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
        this.classManager = classComponent.getClassManager();

        // Load available evolutions and transcendences
        this.displayedClasses = new ArrayList<>();
        this.displayedClasses.addAll(classManager.getAvailableEvolutions());
        this.displayedClasses.addAll(classManager.getAvailableTranscendence());
        this.itemHeights = new ArrayList<>();

        // Initialize scroll behaviors
        this.listScrollBehavior = new ScrollBehavior()
                .setScrollbarWidth(8)
                .setScrollbarPadding(3)
                .setMinHandleSize(24);

        this.detailsScrollBehavior = new ScrollBehavior()
                .setScrollbarWidth(8)
                .setScrollbarPadding(3)
                .setMinHandleSize(24);
    }

    @Override
    protected void init() {
        super.init();
        this.fadeAnimation = new Animation(ANIMATION_DURATION);
        this.slideAnimation = new Animation(ANIMATION_DURATION);

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
                List<String> descLines = wrapText(description, textWidth);
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
        int yOffset = (int) AnimationUtils.getPositionOffset(slideAnimation.getProgress(), FINAL_Y_OFFSET, height);
        int contentY = yOffset + CONTENT_PADDING + SUMMARY_HEIGHT + SECTION_SPACING;
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
        List<String> descLines = wrapText(selectedClass.getDescription(), maxWidth);
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
        fadeAnimation.update(delta);
        slideAnimation.update(delta);

        // Recalculate item heights if needed (in case of window resize)
        if (itemHeights.size() != displayedClasses.size()) {
            calculateItemHeights();
        }

        // Update scroll bounds (in case of window resize)
        updateScrollBounds();

        // Set dark background
        DrawContextUtils.fillScreen(context, 0xF0121212);

        // Calculate animated offsets
        int yOffset = (int) AnimationUtils.getPositionOffset(slideAnimation.getProgress(), FINAL_Y_OFFSET, height);

        // Main content area
        int contentX = CONTENT_PADDING;
        int contentY = yOffset + CONTENT_PADDING;
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
        renderClassEvolutionList(context, contentX, contentY + SUMMARY_HEIGHT + SECTION_SPACING,
                listWidth, contentHeight - SUMMARY_HEIGHT - SECTION_SPACING, mouseX, mouseY, delta);

        // Render details panel with scroll behavior
        if (selectedClass != null) {
            renderClassEvolutionDetails(context, detailsX, contentY + SUMMARY_HEIGHT + SECTION_SPACING,
                    detailsWidth, contentHeight - SUMMARY_HEIGHT - SECTION_SPACING, mouseX, mouseY, delta);
        }

        // Render toasts over screen
        renderToastsOverScreen(context, delta);
    }

    private void renderTitle(DrawContext context, int x, int y, int width) {
        Text titleText = Text.literal("Class Evolution").formatted(Formatting.GOLD, Formatting.BOLD);
        int titleX = x + (width - textRenderer.getWidth(titleText)) / 2;

        AnimationUtils.drawFadeText(context, textRenderer, titleText, titleX, y, 0xFFD700,
                AnimationUtils.getAlpha(fadeAnimation.getProgress()));

        // Decorative lines
        int lineY = y + textRenderer.fontHeight + 3;
        DrawContextUtils.renderHorizontalLineWithCenterGradient(context, x, lineY, width, 1, 400,
                0xFFFFFFFF, 0, fadeAnimation.getProgress());
    }

    private void renderEvolutionSummary(DrawContext context, int x, int y, int width, float delta) {
        PlayerClass currentClass = classManager.getCurrentClass();
        int level = classManager.getClassLevel();
        boolean ready = classManager.isReadyForEvolution() || classManager.isReadyForTranscendence();

        int evolutionCount = classManager.getAvailableEvolutions().size();
        int transcendenceCount = classManager.getAvailableTranscendence().size();
        int totalCount = displayedClasses.size();

        // Background with border
        context.fill(x, y, x + width, y + SUMMARY_HEIGHT, 0xFF1E1E1E);
        context.fill(x, y, x + width, y + 1, 0xFF4CAF50);
        context.fill(x, y + SUMMARY_HEIGHT - 1, x + width, y + SUMMARY_HEIGHT, 0xFF4CAF50);

        // Summary text
        String summaryText = String.format("Current: %s (Lv.%d) | Available: %d evolutions, %d transcendences",
                currentClass.getDisplayName(), level, evolutionCount, transcendenceCount);

        AnimationUtils.drawFadeText(context, textRenderer,
                Text.literal(summaryText).formatted(Formatting.WHITE),
                x + 10, y + 8, 0xFFFFFF, AnimationUtils.getAlpha(fadeAnimation.getProgress()));

        // Status indicator with correct required level
        String statusText;
        int statusColor;
        if (ready) {
            statusText = "✓ Ready!";
            statusColor = 0xFF4CAF50;
        } else {
            // Show correct required level based on current class
            int requiredLevel = currentClass.getTier() == 0 ? currentClass.getMaxLevel() : 45;
            statusText = String.format("Requires Lv.%d", requiredLevel);
            statusColor = 0xFFFF6B6B;
        }

        int statusTextWidth = textRenderer.getWidth(statusText);
        AnimationUtils.drawFadeText(context, textRenderer, Text.literal(statusText),
                x + width - statusTextWidth - 10, y + 8, statusColor, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
    }

    private void renderClassEvolutionList(DrawContext context, int x, int y, int width, int height,
                                          int mouseX, int mouseY, float delta) {

        // Background with border
        context.fill(x, y, x + width, y + height, 0xFF1E1E1E);
        drawBorder(context, x, y, width, height, 0xFF444444);

        if (displayedClasses.isEmpty()) {
            AnimationUtils.drawFadeCenteredText(context, textRenderer,
                    Text.literal("No evolutions available").formatted(Formatting.GRAY),
                    x + width / 2, y + height / 2, 0x888888, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
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

            boolean canEvolve = true; // If it's in displayedClasses, it's already available
            boolean isHovered = mouseX >= x + 5 && mouseX <= x + width - 25 &&
                    mouseY >= currentY && mouseY <= currentY + itemHeight - 2;
            boolean isSelected = clazz == selectedClass;

            renderClassListItem(context, clazz, x + 5, currentY, width - 30, itemHeight - 2,
                    canEvolve, isHovered, isSelected, delta);

            currentY += itemHeight;
        }

        // Disable scissor
        listScrollBehavior.disableScissor(context);
    }

    private void renderClassListItem(DrawContext context, PlayerClass clazz, int x, int y, int width, int height,
                                     boolean canEvolve, boolean isHovered, boolean isSelected, float delta) {

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
        AnimationUtils.drawFadeText(context, textRenderer, className, textX, currentTextY,
                color.getColorValue() != null ? color.getColorValue() : 0xFFFFFF,
                AnimationUtils.getAlpha(fadeAnimation.getProgress()));

        currentTextY += textRenderer.fontHeight + 3;

        // Tier and code info
        String tierInfo = String.format("[%s] Tier %d", clazz.getClassCode(), clazz.getTier());
        AnimationUtils.drawFadeText(context, textRenderer,
                Text.literal(tierInfo).formatted(Formatting.DARK_GRAY),
                textX, currentTextY, 0x888888, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
        currentTextY += textRenderer.fontHeight + 2;

        // Description (wrapped for long text)
        String description = clazz.getDescription();
        if (description != null && !description.isEmpty()) {
            List<String> descLines = wrapText(description, width - ITEM_PADDING * 2);
            for (String line : descLines) {
                AnimationUtils.drawFadeText(context, textRenderer,
                        Text.literal(line).formatted(Formatting.GRAY),
                        textX, currentTextY, 0xAAAAAA, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
                currentTextY += textRenderer.fontHeight + 2;
            }
        }

        // Selection highlight
        if (isSelected) {
            context.fill(x + width - 7, y, x + width, y + height, clazz.isTranscendent() ? 0xFFFFD700 : 0xFF4CAF50);
        }
    }

    private void renderClassEvolutionDetails(DrawContext context, int x, int y, int width, int height,
                                             int mouseX, int mouseY, float delta) {
        // Background with border
        context.fill(x, y, x + width, y + height, 0xFF1E1E1E);
        drawBorder(context, x, y, width, height, 0xFF444444);

        if (selectedClass == null) {
            AnimationUtils.drawFadeCenteredText(context, textRenderer,
                    Text.literal("Select an evolution").formatted(Formatting.GRAY),
                    x + width / 2, y + height / 2, 0x888888, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
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

        boolean canEvolve = true; // If selectedClass is in displayedClasses, it's available
        boolean ready = classManager.isReadyForEvolution() || classManager.isReadyForTranscendence();
        boolean requiresDoubleClick = clickedOnceClass != selectedClass;

        // Title with status
        String statusSuffix = canEvolve && ready ? " ✓" : " ✗";
        Formatting titleColor = selectedClass.isTranscendent() ? Formatting.GOLD : Formatting.GREEN;

        Text titleText = selectedClass.getFormattedName().copy().formatted(titleColor, Formatting.BOLD)
                .append(Text.literal(statusSuffix).formatted(Formatting.WHITE));

        AnimationUtils.drawFadeText(context, textRenderer, titleText, textX, currentY,
                titleColor.getColorValue() != null ? titleColor.getColorValue() : 0xFFFFFF,
                AnimationUtils.getAlpha(fadeAnimation.getProgress()));
        currentY += textRenderer.fontHeight + 8;

        // Status description
        String statusText;
        if (!ready) {
            statusText = "§c● REQUIRES LEVEL 45";
        } else if (requiresDoubleClick) {
            statusText = "§e● CLICK TO CONFIRM";
        } else {
            statusText = "§a● READY TO EVOLVE";
        }

        if (selectedClass.isTranscendent()) {
            statusText += " (TRANSCENDENT)";
        }
        AnimationUtils.drawFadeText(context, textRenderer, Text.literal(statusText), textX, currentY, 0xFFFFFF,
                AnimationUtils.getAlpha(fadeAnimation.getProgress()));
        currentY += textRenderer.fontHeight + 10;

        // Description
        String description = selectedClass.getDescription();
        if (description != null && !description.isEmpty()) {
            List<String> descLines = wrapText(description, maxWidth);
            for (String line : descLines) {
                AnimationUtils.drawFadeText(context, textRenderer, Text.literal(line).formatted(Formatting.GRAY),
                        textX, currentY, 0xAAAAAA, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
                currentY += textRenderer.fontHeight + 2;
            }
            currentY += 10;
        }

        // Requirements header
        if (!selectedClass.getRequirements().isEmpty()) {
            AnimationUtils.drawFadeText(context, textRenderer, Text.literal("Requirements:").formatted(Formatting.GOLD),
                    textX, currentY, 0xFFD700, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
            currentY += textRenderer.fontHeight + 5;

            // Requirements list
            for (var req : selectedClass.getRequirements()) {
                PlayerClass requiredClass = req.previousClass();
                int requiredLevel = req.requiredLevel();
                PlayerClass currentClass = classManager.getCurrentClass();
                int currentLevel = classManager.getClassLevel();

                boolean meets = requiredClass == currentClass && currentLevel >= requiredLevel;
                Formatting reqColor = meets ? Formatting.GREEN : Formatting.RED;
                String reqText = String.format("  %s Level %d", requiredClass.getDisplayName(), requiredLevel);

                AnimationUtils.drawFadeText(context, textRenderer, Text.literal(reqText).formatted(reqColor),
                        textX, currentY, reqColor.getColorValue() != null ? reqColor.getColorValue() : 0xFFFFFF,
                        AnimationUtils.getAlpha(fadeAnimation.getProgress()));
                currentY += textRenderer.fontHeight + 2;
            }
            currentY += 10;
        }

        // Attribute bonuses
        if (!selectedClass.getAttributeModifiers().isEmpty()) {
            AnimationUtils.drawFadeText(context, textRenderer, Text.literal("Attribute Bonuses:").formatted(Formatting.GOLD),
                    textX, currentY, 0xFFD700, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
            currentY += textRenderer.fontHeight + 5;

            for (var entry : selectedClass.getAttributeModifiers().entrySet()) {
                String attrName = entry.getKey().value().getTranslationKey();
                double value = entry.getValue();
                String bonusText = String.format("  %s: %+.1f", attrName, value);
                AnimationUtils.drawFadeText(context, textRenderer, Text.literal(bonusText).formatted(Formatting.AQUA),
                        textX, currentY, 0x55FFFF, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
                currentY += textRenderer.fontHeight + 2;
            }
            currentY += 10;
        }

        // Warning for transcendence
        if (selectedClass.isTranscendent()) {
            AnimationUtils.drawFadeText(context, textRenderer,
                    Text.literal("⚠ WARNING: Transcendence will reset your level to 1!").formatted(Formatting.RED, Formatting.BOLD),
                    textX, currentY, 0xFFFF6B6B, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
            currentY += textRenderer.fontHeight + 10;
        }

        // Double-click instruction
        if (ready) {
            String instructionText = requiresDoubleClick ? "Click button twice to confirm evolution" : "Click again to confirm";
            AnimationUtils.drawFadeText(context, textRenderer,
                    Text.literal(instructionText).formatted(Formatting.YELLOW),
                    textX, currentY, 0xFFFFD700, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
            currentY += textRenderer.fontHeight + 5;
        }

        // Apply button (only if ready)
        if (ready) {
            String buttonText;
            if (requiresDoubleClick) {
                buttonText = selectedClass.isTranscendent() ? "CLICK TO TRANSCEND" : "CLICK TO EVOLVE";
            } else {
                buttonText = selectedClass.isTranscendent() ? "CONFIRM TRANSCEND" : "CONFIRM EVOLVE";
            }

            int buttonWidth = textRenderer.getWidth(buttonText) + 20;
            int buttonHeight = 20;
            int buttonX = textX;

            boolean buttonHovered = mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
                    mouseY >= currentY && mouseY <= currentY + buttonHeight;

            int buttonColor;
            int textColor;
            if (!requiresDoubleClick) {
                // Second click - use bright colors
                buttonColor = buttonHovered ? (selectedClass.isTranscendent() ? 0xFFFFD700 : 0xFF4CAF50) :
                        (selectedClass.isTranscendent() ? 0xFFB8860B : 0xFF2E7D32);
                textColor = 0xFFFFFFFF;
            } else {
                // First click - use muted colors
                buttonColor = buttonHovered ? 0xFF555555 : 0xFF2C2C2C;
                textColor = buttonHovered ? 0xFFFFFFFF : 0xAAAAAA;
            }

            context.fill(buttonX, currentY, buttonX + buttonWidth, currentY + buttonHeight, buttonColor);
            drawBorder(context, buttonX, currentY, buttonWidth, buttonHeight, 0xFF777777);

            int textWidth = textRenderer.getWidth(buttonText);
            AnimationUtils.drawFadeText(context, textRenderer, Text.literal(buttonText),
                    buttonX + buttonWidth / 2 - textWidth / 2, currentY + 6, textColor, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
        }

        // Disable scissor
        detailsScrollBehavior.disableScissor(context);
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color); // Top
        context.fill(x, y + height - 1, x + width, y + height, color); // Bottom
        context.fill(x, y, x + 1, y + height, color); // Left
        context.fill(x + width - 1, y, x + width, y + height, color); // Right
    }

    private void renderToastsOverScreen(DrawContext context, float delta) {
        float deltaTime = delta / 20.0f;
        ToastRenderer.renderToasts(context, this.width, deltaTime);
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (textRenderer.getWidth(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
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

        int yOffset = (int) AnimationUtils.getPositionOffset(slideAnimation.getProgress(), FINAL_Y_OFFSET, height);
        int contentX = CONTENT_PADDING;
        int contentY = yOffset + CONTENT_PADDING + SUMMARY_HEIGHT + SECTION_SPACING;
        int contentWidth = width - (CONTENT_PADDING * 2);
        int listWidth = (contentWidth - SECTION_SPACING) / 2; // Updated for 2:2 layout
        int contentHeight = height - contentY - 20;

        // Use the same bounds as hover detection
        int listX = contentX;
        int listY = contentY;
        int itemAreaX = listX + 5;
        int itemAreaWidth = listWidth - 30; // This makes the right bound listX + listWidth - 25

        // Check if clicking in the same area as hover detection
        if (mouseX >= itemAreaX && mouseX <= itemAreaX + itemAreaWidth &&
                mouseY >= listY && mouseY <= listY + contentHeight) {

            int scrollOffset = listScrollBehavior.getScrollOffset();

            // Calculate the same way as in hover detection with dynamic heights
            int adjustedMouseY = (int) mouseY;
            int currentY = listY + 10 - scrollOffset; // Same as hover calculation

            // Find which item was clicked by iterating through items like hover does
            for (int i = 0; i < displayedClasses.size(); i++) {
                int itemHeight = itemHeights.get(i);
                int itemY = currentY;
                int actualItemHeight = itemHeight - 2; // Same as hover detection

                if (adjustedMouseY >= itemY && adjustedMouseY <= itemY + actualItemHeight) {
                    if (itemY + actualItemHeight >= listY && itemY <= listY + contentHeight) {
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
                // Check button area (approximate - would need exact calculation from details rendering)
                int detailsX = contentX + listWidth + SECTION_SPACING;
                int detailsWidth = (contentWidth - SECTION_SPACING) / 2;

                if (mouseX >= detailsX && mouseX <= detailsX + detailsWidth) {
                    // Check if this is the first or second click
                    if (clickedOnceClass != selectedClass) {
                        // First click - set confirmation
                        clickedOnceClass = selectedClass;
                        return true;
                    } else {
                        // Second click - perform evolution
                        ClassEvolutionPayloadC2S.send(selectedClass.getId(), selectedClass.isTranscendent());
                        close();
                        return true;
                    }
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