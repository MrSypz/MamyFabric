package com.sypztep.mamy.client.screen.widget;

import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class ScrollablePlayerInfo {
    private static final int CONTENT_PADDING = 10;
    private static final int HEAD_SIZE = 32;
    private static final int SECTION_SPACING = 2;
    private static final float SCALE = 1f;

    // Core data
    private final MinecraftClient client;
    private final LivingLevelComponent playerStats;
    private final PlayerClassComponent playerClassComponent;
    private final List<InfoItem> items;

    // UI state
    private int x, y, width, height;
    private int totalContentHeight = 0;

    private final ScrollBehavior scrollBehavior;

    public ScrollablePlayerInfo(MinecraftClient client, LivingLevelComponent playerStats,
                                PlayerClassComponent playerClassComponent) {
        this.client = client;
        this.playerStats = playerStats;
        this.playerClassComponent = playerClassComponent;
        this.items = createInfoItems();

        this.scrollBehavior = new ScrollBehavior()
                .setScrollbarWidth(5)
                .setScrollbarPadding(0)
                .setMinHandleSize(24)
                .setScrollbarEnabled(true);
    }

    private List<InfoItem> createInfoItems() {
        List<InfoItem> infoItems = new ArrayList<>();

        // Player basic info section
        infoItems.add(new InfoItem(InfoItemType.HEADER, "Player Info", ""));
        infoItems.add(new InfoItem(InfoItemType.PLAYER_HEAD, "", ""));
        infoItems.add(new InfoItem(InfoItemType.PLAYER_NAME,"",""));
        infoItems.add(new InfoItem(InfoItemType.LEVEL_INFO,"",""));
        infoItems.add(new InfoItem(InfoItemType.CLASS_INFO,"",""));

        return infoItems;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height,
                       float deltaTime, double mouseX, double mouseY) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // Early return if invalid parameters
        if (width <= 0 || height <= 0) {
            return;
        }

        // Calculate content height
        calculateContentHeight(textRenderer);

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
    }

    private void calculateContentHeight(TextRenderer textRenderer) {
        totalContentHeight = CONTENT_PADDING;
        int textWidth = (int) ((width - CONTENT_PADDING * 2) / SCALE);

        for (InfoItem item : items) {
            switch (item.type) {
                case HEADER:
                    totalContentHeight += (int) ((textRenderer.fontHeight + 8) / SCALE);
                    break;
                case PLAYER_HEAD:
                    totalContentHeight += (int) ((HEAD_SIZE + 10) / SCALE);
                    break;
                case PLAYER_NAME:
                case LEVEL_INFO:
                case CLASS_INFO:
                    totalContentHeight += (int) ((textRenderer.fontHeight + 4) / SCALE);
                    break;
                case TEXT:
                    // Calculate wrapped text height
                    List<String> titleLines = TextUtil.wrapText(textRenderer, item.title, textWidth - 20);
                    List<String> descLines = TextUtil.wrapText(textRenderer, item.description, textWidth - 20);
                    int textHeight = (titleLines.size() + descLines.size()) * textRenderer.fontHeight + 8;
                    totalContentHeight += (int) (textHeight / SCALE);
                    break;
                case SPACER:
                    totalContentHeight += (int) (SECTION_SPACING / SCALE);
                    break;
            }
        }

        totalContentHeight += CONTENT_PADDING;
    }

    private void updateScrollBounds() {
        scrollBehavior.setBounds(x, y, width, height);
        scrollBehavior.setContentHeight(totalContentHeight);
    }

    private void renderContent(DrawContext context, TextRenderer textRenderer) {
        if (client.player == null) return;

        int scrollOffset = scrollBehavior.getScrollOffset();
        int currentY = y + CONTENT_PADDING - scrollOffset;

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.scale(SCALE, SCALE, 1.0F);

        for (InfoItem item : items) {
            // Skip items that are completely out of view
            int itemHeight = getItemHeight(textRenderer, item);
            if (currentY + itemHeight < y || currentY >= y + height) {
                currentY += itemHeight;
                continue;
            }

            switch (item.type) {
                case HEADER:
                    renderHeader(context, textRenderer, item, currentY);
                    currentY += (int) ((textRenderer.fontHeight + 8) / SCALE);
                    break;
                case PLAYER_HEAD:
                    renderPlayerHead(context, currentY);
                    currentY += (int) ((HEAD_SIZE + 10) / SCALE);
                    break;
                case PLAYER_NAME:
                    renderPlayerName(context, textRenderer, currentY);
                    currentY += (int) ((textRenderer.fontHeight + 4) / SCALE);
                    break;
                case LEVEL_INFO:
                    renderLevelInfo(context, textRenderer, currentY);
                    currentY += (int) ((textRenderer.fontHeight + 4) / SCALE);
                    break;
                case CLASS_INFO:
                    renderClassInfo(context, textRenderer, currentY);
                    currentY += (int) ((textRenderer.fontHeight + 4) / SCALE);
                    break;
                case TEXT:
                    int textHeight = renderTextItem(context, textRenderer, item, currentY);
                    currentY += textHeight;
                    break;
                case SPACER:
                    currentY += (int) (SECTION_SPACING / SCALE);
                    break;
            }
        }

        matrixStack.pop();
    }

    private int getItemHeight(TextRenderer textRenderer, InfoItem item) {
        int textWidth = (int) ((width - CONTENT_PADDING * 2) / SCALE);

        return switch (item.type) {
            case HEADER -> (int) ((textRenderer.fontHeight + 8) / SCALE);
            case PLAYER_HEAD -> (int) ((HEAD_SIZE + 10) / SCALE);
            case PLAYER_NAME, LEVEL_INFO, CLASS_INFO -> (int) ((textRenderer.fontHeight + 4) / SCALE);
            case TEXT -> {
                List<String> titleLines = TextUtil.wrapText(textRenderer, item.title, textWidth - 20);
                List<String> descLines = TextUtil.wrapText(textRenderer, item.description, textWidth - 20);
                int textHeight = (titleLines.size() + descLines.size()) * textRenderer.fontHeight + 8;
                yield (int) (textHeight / SCALE);
            }
            case SPACER -> (int) (SECTION_SPACING / SCALE);
        };
    }

    private void renderHeader(DrawContext context, TextRenderer textRenderer, InfoItem item, int currentY) {
        int textX = (int) ((x + CONTENT_PADDING) / SCALE);
        int scaledY = (int) (currentY / SCALE);

        if (item.icon != null) {
            // Draw icon
            MatrixStack matrixStack = context.getMatrices();
            matrixStack.push();
            matrixStack.translate(textX, scaledY - 2, 0);
            context.drawGuiTexture(item.icon, 0, 0, 16, 16);
            matrixStack.pop();
            textX += 20;
        }

        context.drawTextWithShadow(textRenderer,
                Text.literal(item.title).formatted(net.minecraft.util.Formatting.BOLD),
                textX, scaledY, 0xFFD700);
    }

    private void renderPlayerHead(DrawContext context, int currentY) {
        int headX = (int) ((x + (width - HEAD_SIZE) / 2) / SCALE);
        int scaledY = (int) (currentY / SCALE);
        PlayerSkinDrawer.draw(context, client.player.getSkinTextures(), headX, scaledY, HEAD_SIZE);
        context.drawBorder(headX, scaledY, HEAD_SIZE, HEAD_SIZE, 0xFFFFFFFF);
    }

    private void renderPlayerName(DrawContext context, TextRenderer textRenderer, int currentY) {
        String playerName = client.player.getName().getString();
        int nameX = (int) ((x + (width - textRenderer.getWidth(playerName)) / 2) / SCALE);
        int scaledY = (int) (currentY / SCALE);
        context.drawTextWithShadow(textRenderer, Text.literal(playerName), nameX, scaledY, 0xFFDAA520);
    }

    private void renderLevelInfo(DrawContext context, TextRenderer textRenderer, int currentY) {
        int level = playerStats.getLevel();
        boolean isMaxLevel = playerStats.getLevelSystem().isMaxLevel();
        String levelText = isMaxLevel ? "MAX LEVEL" : "Base Lv." + level;
        int levelColor = isMaxLevel ? 0xFFDAA520 : 0xFFFFFFFF;

        int levelX = (int) ((x + (width - textRenderer.getWidth(levelText)) / 2) / SCALE);
        int scaledY = (int) (currentY / SCALE);
        context.drawTextWithShadow(textRenderer, Text.literal(levelText), levelX, scaledY, levelColor);
    }

    private void renderClassInfo(DrawContext context, TextRenderer textRenderer, int currentY) {
        String className = playerClassComponent.getClassManager().getCurrentClass() != null ?
                playerClassComponent.getClassManager().getCurrentClass().getDisplayName() : "No Class";
        int classLevel = playerClassComponent.getClassManager().getClassLevel();
        String classText = className + " Lv." + classLevel;

        int classX = (int) ((x + (width - textRenderer.getWidth(classText)) / 2) / SCALE);
        int scaledY = (int) (currentY / SCALE);
        context.drawTextWithShadow(textRenderer, Text.literal(classText), classX, scaledY, 0xFF87CEEB);
    }

    private int renderTextItem(DrawContext context, TextRenderer textRenderer, InfoItem item, int currentY) {
        int textX = (int) ((x + CONTENT_PADDING) / SCALE);
        int scaledY = (int) (currentY / SCALE);
        int textWidth = (int) ((width - CONTENT_PADDING * 2) / SCALE) - 20;

        // Wrap title and description text
        List<String> titleLines = TextUtil.wrapText(textRenderer, item.title, textWidth);
        List<String> descLines = TextUtil.wrapText(textRenderer, item.description, textWidth);

        int lineY = scaledY;

        // Render title lines
        for (String line : titleLines) {
            context.drawTextWithShadow(textRenderer, Text.literal(line), textX, lineY, 0xFFFFFFFF);
            lineY += textRenderer.fontHeight + 1;
        }

        // Add small gap between title and description
        lineY += 2;

        // Render description lines
        for (String line : descLines) {
            context.drawText(textRenderer, Text.literal(line), textX, lineY, 0xFFCCCCCC, false);
            lineY += textRenderer.fontHeight + 1;
        }

        // Return the actual height used (in scaled coordinates)
        return (int) ((lineY - scaledY + 8) / SCALE);
    }

    // Mouse interaction methods
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

    // Data classes
    private static class InfoItem {
        final InfoItemType type;
        final String title;
        final String description;
        final Identifier icon;

        InfoItem(InfoItemType type, String title, String description) {
            this(type, title, description, null);
        }

        InfoItem(InfoItemType type, String title, Identifier icon) {
            this(type, title, null, icon);
        }

        InfoItem(InfoItemType type, String title, String description, Identifier icon) {
            this.type = type;
            this.title = title;
            this.description = description;
            this.icon = icon;
        }
    }

    private enum InfoItemType {
        HEADER,
        PLAYER_HEAD,
        PLAYER_NAME,
        LEVEL_INFO,
        CLASS_INFO,
        TEXT,
        SPACER
    }
}