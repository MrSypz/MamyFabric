package com.sypztep.mamy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
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

        public IconEntry(Identifier texture, String header, String description,
                         Function<MinecraftClient, Screen> screenFactory) {
            this(texture, header, description, screenFactory, 10);
        }
    }

    private static final List<IconEntry> icons = new ArrayList<>();
    private static boolean isOverlayMode = false;
    private static boolean isCollapsed = false;
    private static boolean wasRightAltPressed = false;
    private static IconEntry hoveredIcon = null;
    private static boolean hoveredCollapseButton = false;
    private static int hoveredIconX = 0, hoveredIconY = 0;

    // Constants
    private static final int ICON_SIZE = 10;
    private static final int ICON_SPACING = 6;
    private static final int MARGIN = 4;
    private static final int COLLAPSE_BUTTON_SIZE = 15;
    private static final Identifier COLLAPSE_TEXTURE = Identifier.ofVanilla("transferable_list/select");
    private static final Identifier EXPAND_TEXTURE = Identifier.ofVanilla("transferable_list/unselect");

    public static void addScreenIcon(Identifier texture, String header, String description,
                                     Function<MinecraftClient, Screen> screenFactory) {
        icons.add(new IconEntry(texture, header, description, screenFactory));
    }

    public static void initialize() {
        HudRenderCallback.EVENT.register(IconOverlayManager::render);
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || icons.isEmpty()) return;

        updateInput(client);
        renderIcons(context, client);
    }

    private static void updateInput(MinecraftClient client) {
        boolean isRightAltPressed = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_ALT);

        if (isRightAltPressed && !wasRightAltPressed) {
            toggleOverlayMode(client);
        }

        if (isOverlayMode && client.currentScreen == null &&
                InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_ESCAPE)) {
            isOverlayMode = false;
            client.mouse.lockCursor();
        }

        wasRightAltPressed = isRightAltPressed;
    }

    private static void toggleOverlayMode(MinecraftClient client) {
        if (client.player == null || client.options.hudHidden) return;
        isOverlayMode = !isOverlayMode;

        if (isOverlayMode) {
            client.mouse.unlockCursor();
        } else {
            if (client.currentScreen == null) {
                client.mouse.lockCursor();
            }
        }
    }

    private static void renderIcons(DrawContext context, MinecraftClient client) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        hoveredIcon = null;
        hoveredCollapseButton = false;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        // Calculate collapse button position (always rightmost)
        int collapseX = screenWidth - COLLAPSE_BUTTON_SIZE - MARGIN;
        int collapseY = screenHeight - COLLAPSE_BUTTON_SIZE - MARGIN;

        // Render collapse/expand button
        renderCollapseButton(context, client, collapseX, collapseY);

        // Render icons if not collapsed
        if (!isCollapsed) {
            int startX = collapseX - ICON_SPACING - ICON_SIZE;
            int startY = collapseY + 4; // Align with collapse button

            for (int i = icons.size() - 1; i >= 0; i--) { // Render from right to left
                IconEntry icon = icons.get(i);
                int iconX = startX - i * (ICON_SIZE + ICON_SPACING);
                int iconY = startY;

                boolean isHovered = isMouseOverIcon(iconX, iconY, ICON_SIZE, client);
                if (isHovered) {
                    hoveredIcon = icon;
                    hoveredIconX = iconX;
                    hoveredIconY = iconY;
                }

                renderIcon(context, iconX, iconY, icon, isHovered);
            }

            // Render tooltip for hovered icon
            if (isOverlayMode && hoveredIcon != null) {
                renderTooltip(context, client, hoveredIcon, hoveredIconX, hoveredIconY - 4);
            }
        }

        RenderSystem.disableBlend();
    }

    private static void renderCollapseButton(DrawContext context, MinecraftClient client, int x, int y) {
        boolean isHovered = isMouseOverIcon(x, y, COLLAPSE_BUTTON_SIZE, client);
        hoveredCollapseButton = isHovered;

        if (isOverlayMode && isHovered) {
            RenderSystem.setShaderColor(1.5f, 1.5f, 1.5f, 1.0f);
        }

        Identifier texture = isCollapsed ? EXPAND_TEXTURE : COLLAPSE_TEXTURE;
        context.drawGuiTexture(texture, x, y, COLLAPSE_BUTTON_SIZE, COLLAPSE_BUTTON_SIZE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderIcon(DrawContext context, int x, int y, IconEntry icon, boolean isHovered) {
        if (isOverlayMode && isHovered) {
            RenderSystem.setShaderColor(2.0f, 2.0f, 2.0f, 1.0f);
        }
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
        int tooltipWidth = Math.max(headerWidth, descriptionWidth) + 16;
        int tooltipHeight = textRenderer.fontHeight * 2 + 16;

        int tooltipX = iconX - tooltipWidth / 2 + icon.size / 2;
        int tooltipY = iconY - tooltipHeight - 6;

        int screenWidth = context.getScaledWindowWidth();
        tooltipX = Math.max(5, Math.min(tooltipX, screenWidth - tooltipWidth - 5));
        tooltipY = Math.max(5, tooltipY);

        // Simple tooltip background
        context.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, 0xE0000000);
        context.drawBorder(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 0xFFB8860B);

        int textX = tooltipX + 8;
        int headerY = tooltipY + 8;
        int descriptionY = headerY + textRenderer.fontHeight + 4;

        context.drawText(textRenderer, icon.header, textX, headerY, 0xFFFFD700, true);
        context.drawText(textRenderer, icon.description, textX, descriptionY, 0xFFDDDDDD, true);
    }

    public static boolean handleIconClick(MinecraftClient client) {
        if (!isOverlayMode) return false;

        if (hoveredCollapseButton) {
            isCollapsed = !isCollapsed;
            return true;
        }

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
        icons.clear();
    }
}