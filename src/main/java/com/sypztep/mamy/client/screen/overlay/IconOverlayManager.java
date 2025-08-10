package com.sypztep.mamy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sypztep.mamy.client.util.DrawContextUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class IconOverlayManager {

    /**
     * Icon positioning modes
     */
    public enum IconPosition {
        HOTBAR_RIGHT,    // Right of hotbar
        TOP_LEFT,        // Top left corner
        TOP_RIGHT,       // Top right corner
        BOTTOM_LEFT,     // Bottom left corner
        BOTTOM_RIGHT,    // Bottom right corner
        CUSTOM           // Custom positioning with x,y coordinates
    }

    /**
     * Icon group registration
     */
    public static class IconGroup {
        public final String groupId;
        public final IconPosition position;
        public final int customX, customY;
        public final List<IconEntry> icons;
        public final boolean horizontal;

        public IconGroup(String groupId, IconPosition position, boolean horizontal) {
            this(groupId, position, 0, 0, horizontal);
        }

        public IconGroup(String groupId, IconPosition position, int customX, int customY, boolean horizontal) {
            this.groupId = groupId;
            this.position = position;
            this.customX = customX;
            this.customY = customY;
            this.horizontal = horizontal;
            this.icons = new ArrayList<>();
        }
    }

    /**
     * Individual icon data - ONLY screens, no HUD actions
     */
    public static class IconEntry {
        public final Identifier texture;
        public final String header;
        public final String description;
        public final Function<MinecraftClient, Screen> screenFactory;
        public final int size;

        public IconEntry(Identifier texture, String header, String description,
                         Function<MinecraftClient, Screen> screenFactory, int size) {
            this.texture = texture;
            this.header = header;
            this.description = description;
            this.screenFactory = screenFactory;
            this.size = size;
        }

        // Default 8px size
        public IconEntry(Identifier texture, String header, String description,
                         Function<MinecraftClient, Screen> screenFactory) {
            this(texture, header, description, screenFactory, 8);
        }
    }

    private static final List<IconGroup> iconGroups = new ArrayList<>();
    private static boolean isOverlayMode = false;
    private static boolean wasLeftAltPressed = false;
    private static IconEntry hoveredIcon = null;
    private static int hoveredIconX = 0, hoveredIconY = 0;

    // Styling constants
    private static final int ICON_SPACING = 12;
    private static final int GROUP_MARGIN = 10;
    private static final int TOOLTIP_PADDING = 8;
    private static final int TOOLTIP_MARGIN = 6;
    private static final int GRADIENT_HEIGHT = 1;

    // Colors
    private static final int TOOLTIP_BG_COLOR = 0xE0000000;
    private static final int TOOLTIP_BORDER_COLOR = 0xFFB8860B;
    private static final int HEADER_COLOR = 0xFFFFD700;
    private static final int DESCRIPTION_COLOR = 0xFFDDDDDD;
    private static final int GRADIENT_START_COLOR = 0xFFFFD700;
    private static final int GRADIENT_END_COLOR = 0x0F8B7300;

    /**
     * Register a new icon group
     */
    public static void registerIconGroup(String groupId, IconPosition position, boolean horizontal) {
        IconGroup group = new IconGroup(groupId, position, horizontal);
        iconGroups.add(group);
    }

    /**
     * Register a custom positioned icon group
     */
    public static IconGroup registerIconGroup(String groupId, int x, int y, boolean horizontal) {
        IconGroup group = new IconGroup(groupId, IconPosition.CUSTOM, x, y, horizontal);
        iconGroups.add(group);
        return group;
    }

    /**
     * Add screen icon to a group
     */
    public static void addScreenIcon(String groupId, Identifier texture, String header, String description,
                                     Function<MinecraftClient, Screen> screenFactory) {
        addScreenIcon(groupId, texture, header, description, screenFactory, 8);
    }

    /**
     * Add screen icon with custom size to a group
     */
    public static void addScreenIcon(String groupId, Identifier texture, String header, String description,
                                     Function<MinecraftClient, Screen> screenFactory, int size) {
        IconGroup group = findGroup(groupId);
        if (group != null) {
            group.icons.add(new IconEntry(texture, header, description, screenFactory, size));
        }
    }

    private static IconGroup findGroup(String groupId) {
        return iconGroups.stream()
                .filter(group -> group.groupId.equals(groupId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Initialize the overlay system
     */
    public static void initialize() {
        HudRenderCallback.EVENT.register(IconOverlayManager::render);
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        updateOverlayState(client);
        renderAllIconGroups(context, client);
    }

    private static void updateOverlayState(MinecraftClient client) {
        boolean isLeftAltPressed = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_ALT);

        if (isLeftAltPressed && !wasLeftAltPressed) {
            toggleOverlayMode(client);
        }
        handleKeyPress(client);
        wasLeftAltPressed = isLeftAltPressed;
    }

    private static void toggleOverlayMode(MinecraftClient client) {
        if (client.player == null || client.options.hudHidden) return;
        isOverlayMode = !isOverlayMode;

        if (isOverlayMode) {
            client.mouse.lockCursor();
            client.mouse.unlockCursor();
        } else {
            if (client.currentScreen == null) {
                client.mouse.lockCursor();
            }
        }
    }

    private static void renderAllIconGroups(DrawContext context, MinecraftClient client) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        hoveredIcon = null;

        for (IconGroup group : iconGroups) {
            if (!group.icons.isEmpty()) {
                renderIconGroup(context, client, group);
            }
        }

        // Render tooltip for hovered icon
        if (isOverlayMode && hoveredIcon != null) {
            renderTooltip(context, client, hoveredIcon, hoveredIconX, hoveredIconY - 4);
        }

        RenderSystem.disableBlend();
    }

    private static void renderIconGroup(DrawContext context, MinecraftClient client, IconGroup group) {
        int[] groupPos = calculateGroupPosition(context, group);
        int startX = groupPos[0];
        int startY = groupPos[1];

        for (int i = 0; i < group.icons.size(); i++) {
            IconEntry icon = group.icons.get(i);

            int iconX, iconY;
            if (group.horizontal) {
                iconX = startX + i * (icon.size + ICON_SPACING);
                iconY = startY;
            } else {
                iconX = startX;
                iconY = startY + i * (icon.size + ICON_SPACING);
            }

            boolean isHovered = isMouseOverIcon(iconX, iconY, icon.size, client);

            if (isHovered) {
                hoveredIcon = icon;
                hoveredIconX = iconX;
                hoveredIconY = iconY;
            }

            renderIcon(context, iconX, iconY, icon, isHovered);
        }
    }

    private static int[] calculateGroupPosition(DrawContext context, IconGroup group) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        switch (group.position) {
            case HOTBAR_RIGHT:
                int hotbarCenterX = screenWidth / 2;
                int hotbarY = screenHeight - 14;
                int hotbarRightEdge = hotbarCenterX + 91;
                return new int[]{hotbarRightEdge + GROUP_MARGIN, hotbarY + 1};

            case TOP_LEFT:
                return new int[]{GROUP_MARGIN, GROUP_MARGIN};

            case TOP_RIGHT:
                int groupWidth = calculateGroupWidth(group);
                return new int[]{screenWidth - groupWidth - GROUP_MARGIN, GROUP_MARGIN};

            case BOTTOM_LEFT:
                return new int[]{GROUP_MARGIN, screenHeight - calculateGroupHeight(group) - GROUP_MARGIN};

            case BOTTOM_RIGHT:
                int gWidth = calculateGroupWidth(group);
                int gHeight = calculateGroupHeight(group);
                return new int[]{screenWidth - gWidth - GROUP_MARGIN, screenHeight - gHeight - GROUP_MARGIN};

            case CUSTOM:
                return new int[]{group.customX, group.customY};

            default:
                return new int[]{0, 0};
        }
    }

    private static int calculateGroupWidth(IconGroup group) {
        if (group.horizontal && !group.icons.isEmpty()) {
            IconEntry lastIcon = group.icons.getLast();
            return (group.icons.size() - 1) * (8 + ICON_SPACING) + lastIcon.size;
        }
        return group.icons.isEmpty() ? 0 : group.icons.getFirst().size;
    }

    private static int calculateGroupHeight(IconGroup group) {
        if (!group.horizontal && !group.icons.isEmpty()) {
            IconEntry lastIcon = group.icons.getLast();
            return (group.icons.size() - 1) * (8 + ICON_SPACING) + lastIcon.size;
        }
        return group.icons.isEmpty() ? 0 : group.icons.getFirst().size;
    }

    private static void renderIcon(DrawContext context, int x, int y, IconEntry icon, boolean isHovered) {
        if (isOverlayMode && isHovered)
            RenderSystem.setShaderColor(2.0f, 2.0f, 2.0f, 1.0f);
        context.drawGuiTexture(icon.texture, x, y, icon.size, icon.size);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static boolean isMouseOverIcon(int iconX, int iconY, int iconSize, MinecraftClient client) {
        if (!isOverlayMode) return false;

        double scaledMouseX = client.mouse.getX() * client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth();
        double scaledMouseY = client.mouse.getY() * client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight();

        return scaledMouseX >= iconX && scaledMouseX < iconX + iconSize + 4 &&
                scaledMouseY >= iconY && scaledMouseY < iconY + iconSize + 4;
    }

    private static void renderTooltip(DrawContext context, MinecraftClient client, IconEntry icon, int iconX, int iconY) {
        var textRenderer = client.textRenderer;
        int headerWidth = textRenderer.getWidth(icon.header);
        int descriptionWidth = textRenderer.getWidth(icon.description);
        int tooltipWidth = Math.max(headerWidth, descriptionWidth) + TOOLTIP_PADDING * 2;
        int tooltipHeight = textRenderer.fontHeight * 2 + TOOLTIP_PADDING * 2 + GRADIENT_HEIGHT + 2;

        int tooltipX = iconX - tooltipWidth / 2 + icon.size / 2;
        int tooltipY = iconY - tooltipHeight - TOOLTIP_MARGIN;

        int screenWidth = context.getScaledWindowWidth();
        tooltipX = Math.max(5, Math.min(tooltipX, screenWidth - tooltipWidth - 5));
        tooltipY = Math.max(5, tooltipY);

        renderTooltipBackground(context, tooltipX, tooltipY, tooltipWidth, tooltipHeight);

        DrawContextUtils.renderHorizontalLineWithCenterGradient(
                context,
                tooltipX + TOOLTIP_PADDING,
                tooltipY + TOOLTIP_PADDING + textRenderer.fontHeight + 2,
                tooltipWidth - TOOLTIP_PADDING * 2,
                GRADIENT_HEIGHT,700,
                GRADIENT_START_COLOR,
                GRADIENT_END_COLOR
        );

        int textX = tooltipX + TOOLTIP_PADDING;
        int headerY = tooltipY + TOOLTIP_PADDING;
        int descriptionY = headerY + textRenderer.fontHeight + GRADIENT_HEIGHT + 4;

        context.drawText(textRenderer, icon.header, textX, headerY, HEADER_COLOR, true);
        context.drawText(textRenderer, icon.description, textX, descriptionY, DESCRIPTION_COLOR, true);
    }

    private static void renderTooltipBackground(DrawContext context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, TOOLTIP_BG_COLOR);
        context.drawBorder(x, y, width, height, TOOLTIP_BORDER_COLOR);
        context.drawBorder(x + 1, y + 1, width - 2, height - 2, 0x40FFD700);
    }

    public static boolean handleIconClick(MinecraftClient client) {
        if (!isOverlayMode) return false;

        if (hoveredIcon != null) {
            isOverlayMode = false;
            Screen screen = hoveredIcon.screenFactory.apply(client);
            client.setScreen(screen);
            return true;
        }

        return false;
    }

    public static boolean isOverlayMode() {
        return isOverlayMode;
    }

    public static void clearAll() {
        iconGroups.clear();
    }
    public static void handleKeyPress(MinecraftClient client) {
        if (isOverlayMode && client.currentScreen == null && InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_ESCAPE)) {
            isOverlayMode = false;
            client.mouse.lockCursor();
        }
    }

}