package com.sypztep.mamy.client.screen.widget;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.network.server.IncreaseStatsPayloadC2S;
import com.sypztep.mamy.common.system.stat.Stat;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ScrollableStat {
    private static final int ITEM_HEIGHT = 25;
    private static final int CONTENT_PADDING = 6;
    private static final int BUTTON_SIZE = 16;
    private static final float SCALE = 0.7f;

    // Colors
    private static final int BASE_COLOR = 0xFFFFD700;        // Yellow for base stats
    private static final int MAX_COLOR = 0xCCCCCC;           // Gray for max value
    private static final int ADDITION_COLOR = 0xFF90EE90;    // Green for additions
    private static final int HOVER_COLOR = 0xFFFFFF80;       // Yellow for hover

    private final LivingLevelComponent playerStats;
    private final MinecraftClient client;

    // UI state
    private int x, y, width, height;
    private int hoveredStatIndex = -1;       // For stat name areas
    private int hoveredBaseTextIndex = -1;   // For base text [value/max] areas
    private int hoveredButtonIndex = -1;     // For button areas
    private boolean wasButtonHovered = false; // Track button hover state for sound

    // Enhanced scroll behavior
    private final ScrollBehavior scrollBehavior;
    private final List<StatEntry> statEntries;
    private final List<ButtonInfo> buttonInfos;

    public ScrollableStat(LivingLevelComponent playerStats, MinecraftClient client) {
        this.playerStats = playerStats;
        this.client = client;
        this.statEntries = createStatEntries();
        this.buttonInfos = createButtonInfos();

        this.scrollBehavior = new ScrollBehavior().setScrollbarWidth(5).setScrollbarPadding(0).setMinHandleSize(24).setScrollbarEnabled(true);
    }

    private List<StatEntry> createStatEntries() {
        List<StatEntry> entries = new ArrayList<>();
        for (StatTypes statType : StatTypes.values()) {
            entries.add(new StatEntry(statType));
        }
        return entries;
    }

    private List<ButtonInfo> createButtonInfos() {
        List<ButtonInfo> infos = new ArrayList<>();
        for (StatTypes statType : StatTypes.values()) {
            infos.add(new ButtonInfo(statType));
        }
        return infos;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height, float deltaTime, double mouseX, double mouseY) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // Early return if invalid parameters
        if (width <= 0 || height <= 0 || statEntries.isEmpty()) {
            return;
        }

        updateHoverState(mouseX, mouseY);

        // Update scroll behavior bounds and content
        updateScrollBounds();

        // Draw the container background
        DrawContextUtils.drawRect(context, x, y, width, height, 0xFF1E1E1E);
        context.drawBorder(x, y, width, height, 0xFF404040);
        // Update scroll behavior
        scrollBehavior.update(context, (int) mouseX, (int) mouseY, deltaTime);

        // Enable scissor for content clipping
        scrollBehavior.enableScissor(context);

        // Render content with proper positioning
        renderContent(context, textRenderer);

        // Disable scissor
        scrollBehavior.disableScissor(context);

        // Render tooltips OUTSIDE of scissor area
        if (hoveredStatIndex >= 0 && hoveredStatIndex < statEntries.size()) {
            renderStatNameTooltip(context, textRenderer, statEntries.get(hoveredStatIndex), mouseX, mouseY);
        } else if (hoveredBaseTextIndex >= 0 && hoveredBaseTextIndex < statEntries.size()) {
            renderBaseTextTooltip(context, textRenderer, statEntries.get(hoveredBaseTextIndex), mouseX, mouseY);
        }
    }

    private void updateScrollBounds() {
        scrollBehavior.setBounds(x, y, width, height);

        int totalContentHeight = statEntries.size() * ITEM_HEIGHT;
        scrollBehavior.setContentHeight(totalContentHeight);
    }

    private void renderContent(DrawContext context, TextRenderer textRenderer) {
        int scrollOffset = scrollBehavior.getScrollOffset();
        int currentY = y + CONTENT_PADDING - scrollOffset;

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.scale(SCALE, SCALE, 1.0F);

        // Render each stat entry
        for (int i = 0; i < statEntries.size(); i++) {
            StatEntry entry = statEntries.get(i);

            // Skip items that are completely out of view
            if (currentY + ITEM_HEIGHT < y || currentY >= y + height) {
                currentY += ITEM_HEIGHT;
                continue;
            }

            boolean isStatNameHovered = (i == hoveredStatIndex);
            boolean isBaseTextHovered = (i == hoveredBaseTextIndex);
            boolean isButtonHovered = (i == hoveredButtonIndex);
            renderStatEntry(context, textRenderer, entry, i, currentY, isStatNameHovered, isBaseTextHovered, isButtonHovered);

            currentY += ITEM_HEIGHT;
        }

        matrixStack.pop();
    }

    private void renderStatEntry(DrawContext context, TextRenderer textRenderer, StatEntry entry, int index, int currentY, boolean isStatNameHovered, boolean isBaseTextHovered, boolean isButtonHovered) {
        Stat stat = playerStats.getStatByType(entry.statType);

        int gradientX = (int) ((x + 5) / SCALE);
        int gradientY = (int) (currentY / SCALE);
        int gradientWidth = (int) ((width - 10) / SCALE);

        int edgeColor = 0;

        DrawContextUtils.renderHorizontalLineWithCenterGradient(context, gradientX, gradientY, gradientWidth, 1, 0, 0xFFCCCCCC, edgeColor, 1);


        int textX = (int) ((x + 10) / SCALE);
        int textY = (int) ((currentY + 6) / SCALE);

        // Stat name
        String statName = entry.statType.getAka().toUpperCase();
        int nameColor = isStatNameHovered ? 0xFFFFFF : 0xCCCCCC;
        context.drawTextWithShadow(textRenderer, Text.literal(statName), textX, textY, nameColor);

        // Move to value area
        textX += 50;

        // Base value [current/max] - base yellow, max gray
        String baseText = String.valueOf(stat.getCurrentValue());

        context.drawTextWithShadow(textRenderer, Text.literal("[").withColor(MAX_COLOR).append(Text.literal(baseText).withColor(isBaseTextHovered ? HOVER_COLOR : BASE_COLOR)), textX, textY, 0);

        textX += textRenderer.getWidth("[" + baseText + "]");


        String maxText = "/" + ModConfig.maxStatValue + "]";
        context.drawTextWithShadow(textRenderer, Text.literal(maxText), textX, textY, isBaseTextHovered ? 0xFFFFFF : MAX_COLOR);
        textX += textRenderer.getWidth(maxText) + 5;

        // Class bonus [+X] - green
        if (stat.getClassBonus() > 0) {
            String bonusText = "[+" + stat.getClassBonus() + "]";
            context.drawTextWithShadow(textRenderer, Text.literal(bonusText), textX, textY, isBaseTextHovered ? HOVER_COLOR : ADDITION_COLOR);
            textX += textRenderer.getWidth(bonusText) + 5;
        }

        // Temporary modifiers [+X] - green for positive, red for negative
        if (stat.getTotalTemporaryModifiers() != 0) {
            short temp = stat.getTotalTemporaryModifiers();
            String tempText = "[" + (temp > 0 ? "+" : "") + temp + "]";
            int tempColor = isBaseTextHovered ? HOVER_COLOR : (temp > 0 ? ADDITION_COLOR : 0xFFFF6B6B);
            context.drawTextWithShadow(textRenderer, Text.literal(tempText), textX, textY, tempColor);
        }
        // Render button manually
        renderButton(context, textRenderer, entry, index, currentY, isButtonHovered);
    }

    private void renderButton(DrawContext context, TextRenderer textRenderer, StatEntry entry, int index, int currentY, boolean isHovered) {
        Stat stat = playerStats.getStatByType(entry.statType);
        ButtonInfo buttonInfo = buttonInfos.get(index);

        // Update button position
        int buttonX = (int) ((x + width - BUTTON_SIZE - 15) / SCALE);
        int buttonY = (int) ((currentY + 5) / SCALE);
        buttonInfo.x = buttonX;
        buttonInfo.y = buttonY;

        // Update button state
        boolean canAfford = playerStats.getAvailableStatPoints() >= stat.getIncreasePerPoint();
        boolean isMaxed = stat.getCurrentValue() >= ModConfig.maxStatValue;
        boolean isEnabled = canAfford && !isMaxed;

        buttonInfo.enabled = isEnabled;

        // Render button text
        int textColor = isEnabled ? (isHovered ? 0xFFD700 : 0xCCCCCC) : 0x666666;
        String buttonText = "+";

        int textX = buttonX + (BUTTON_SIZE - textRenderer.getWidth(buttonText)) / 2;
        int textY = buttonY + (BUTTON_SIZE - textRenderer.fontHeight) / 2;

        context.drawTextWithShadow(textRenderer, Text.literal(buttonText), textX, textY, textColor);
    }

    private void updateHoverState(double mouseX, double mouseY) {
        hoveredStatIndex = -1;
        hoveredBaseTextIndex = -1;
        hoveredButtonIndex = -1;

        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
            // Handle hover sound when leaving button area
            if (wasButtonHovered) {
                wasButtonHovered = false;
            }
            return;
        }

        int scrollOffset = scrollBehavior.getScrollOffset();
        int currentY = y + CONTENT_PADDING - scrollOffset;

        for (int i = 0; i < statEntries.size(); i++) {
            if (mouseY >= currentY && mouseY < currentY + ITEM_HEIGHT) {
                double scaledMouseX = mouseX / SCALE;
                double scaledMouseY = mouseY / SCALE;

                // Calculate scaled coordinates for different areas
                int statNameStartX = (int) ((x + 10) / SCALE);
                int statNameEndX = statNameStartX + 20; // Approximately 45 pixels for stat name

                int baseTextStartX = (int) ((x + 44) / SCALE); // After stat name area
                int baseTextEndX = baseTextStartX + 42; // Approximate width of base text area

                int buttonX = (int) ((x + width - BUTTON_SIZE - 15) / SCALE);
                int buttonY = (int) ((currentY + 5) / SCALE);

                // Check which area is being hovered
                if (scaledMouseX >= buttonX && scaledMouseX <= buttonX + BUTTON_SIZE && scaledMouseY >= buttonY && scaledMouseY <= buttonY + BUTTON_SIZE) {
                    // Button area
                    hoveredButtonIndex = i;

                    // Play hover sound when starting to hover over button
                    if (!wasButtonHovered && buttonInfos.get(i).enabled) {
                        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_HAT, 1.8F));
                        wasButtonHovered = true;
                    }
                } else if (scaledMouseX >= baseTextStartX && scaledMouseX <= baseTextEndX) {
                    // Base text area [current/max]
                    hoveredBaseTextIndex = i;
                    if (wasButtonHovered) {
                        wasButtonHovered = false;
                    }
                } else if (scaledMouseX >= statNameStartX && scaledMouseX <= statNameEndX) {
                    // Stat name area
                    hoveredStatIndex = i;
                    if (wasButtonHovered) {
                        wasButtonHovered = false;
                    }
                } else {
                    // Not hovering over any specific area
                    if (wasButtonHovered) {
                        wasButtonHovered = false;
                    }
                }
                return;
            }
            currentY += ITEM_HEIGHT;
        }

        // Not hovering over anything
        if (wasButtonHovered) {
            wasButtonHovered = false;
        }
    }

    // Mouse interaction methods
    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (button != 0) return false; // Only left click

        // Check if clicking on a button
        if (hoveredButtonIndex >= 0) {
            ButtonInfo buttonInfo = buttonInfos.get(hoveredButtonIndex);
            if (buttonInfo.enabled) {
                // Play click sound
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                // Perform action
                StatTypes statType = statEntries.get(hoveredButtonIndex).statType;
                IncreaseStatsPayloadC2S.send(statType);
                return true;
            }
        }

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

    private void renderStatNameTooltip(DrawContext context, TextRenderer textRenderer, StatEntry entry, double mouseX, double mouseY) {
        Stat stat = playerStats.getStatByType(entry.statType);
        List<Text> tooltip = new ArrayList<>();

        // Main header with stat name
        tooltip.add(Text.literal(entry.statType.getName().toUpperCase()).formatted(Formatting.GOLD));

        // Show effect descriptions using getEffectDescription
        try {
            List<Text> descriptions = stat.getEffectDescription(1); // Assuming 1 point for description
            if (descriptions != null && !descriptions.isEmpty()) {
                tooltip.addAll(descriptions);
            } else {
                // Fallback description if getEffectDescription returns empty
                tooltip.add(Text.literal(getStatDescription(entry.statType)).formatted(Formatting.WHITE));
            }
        } catch (Exception e) {
            // Fallback if getEffectDescription method doesn't exist or fails
            tooltip.add(Text.literal(getStatDescription(entry.statType)).formatted(Formatting.WHITE));
        }

        context.drawTooltip(textRenderer, tooltip, (int) mouseX, (int) mouseY);
    }

    private void renderBaseTextTooltip(DrawContext context, TextRenderer textRenderer, StatEntry entry, double mouseX, double mouseY) {
        Stat stat = playerStats.getStatByType(entry.statType);
        List<Text> tooltip = new ArrayList<>();

        tooltip.add(Text.literal(entry.statType.getName().toUpperCase()).formatted(Formatting.GOLD));
        tooltip.add(Text.literal("Effective: " + stat.getEffective()).formatted(Formatting.WHITE));

        if (stat.getCurrentValue() > 0) {
            tooltip.add(Text.literal("Base: " + stat.getCurrentValue()).formatted(Formatting.GRAY));
        }
        if (stat.getClassBonus() > 0) {
            tooltip.add(Text.literal("Class: +" + stat.getClassBonus()).formatted(Formatting.GREEN));
        }
        MutableText result = Text.literal("Addition: ");
        if (stat.getTotalTemporaryModifiers() != 0) {
            for (Map.Entry<String, Short> entryz : stat.getTemporaryModifiers().entrySet()) {
                String key = entryz.getKey();   // the String
                Short value = entryz.getValue(); // the Short
                String sign = value > 0 ? "+ " : "";
                result.append(sign + key + " " + value).formatted(value > 0 ? Formatting.AQUA : Formatting.RED);
            }
            tooltip.add(result);
        }

        context.drawTooltip(textRenderer, tooltip, (int) mouseX, (int) mouseY);
    }

    private String getStatDescription(StatTypes statType) {
        return switch (statType) {
            case STRENGTH -> "Increases physical damage and melee power";
            case AGILITY -> "Improves speed, evasion, and dexterity";
            case VITALITY -> "Enhances health and defensive capabilities";
            case INTELLIGENCE -> "Boosts magic damage and mana capacity";
            case DEXTERITY -> "Increases accuracy and critical hit chance";
            case LUCK -> "Improves critical rates and rare item drops";
        };
    }

    // Helper classes
    private record StatEntry(StatTypes statType) {}

    private static class ButtonInfo {
        final StatTypes statType;
        int x, y;
        boolean enabled;

        ButtonInfo(StatTypes statType) {
            this.statType = statType;
        }
    }
}