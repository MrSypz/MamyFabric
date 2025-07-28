package com.sypztep.mamy.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sypztep.mamy.client.screen.widget.ListElement;
import com.sypztep.mamy.common.util.ColorUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public final class DrawContextUtils {
    public static void drawTextWithIcon(DrawContext context, TextRenderer textRenderer, ListElement listElement, int x, int y, float scale, float iconscale, int alpha) {
        final int ICON_SIZE = 16;
        Text text = listElement.text();
        Identifier icon = listElement.icon();

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.scale(scale, scale, 1.0F);

        // Calculate position for text considering scaling
        int textX = (int) (x / scale);
        int textY = (int) (y / scale);

        // Render icon if present
        if (icon != null) {
            matrixStack.push();
            matrixStack.translate((x - ICON_SIZE) / scale - 10, (y + (textRenderer.fontHeight * scale) / 2 - (float) ICON_SIZE / 2), 0);
            matrixStack.scale(iconscale, iconscale, 1.0F);
            context.drawGuiTexture(icon, 0, 0, ICON_SIZE, ICON_SIZE);
            matrixStack.pop();
        }

        // Draw text
        AnimationUtils.drawFadeText(context, textRenderer, text, textX, textY, alpha);

        matrixStack.pop();
    }

    public static void drawText(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, float scale, int alpha) {
        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.scale(scale, scale, 1.0F);
        int textX = (int) (x / scale);
        int textY = (int) (y / scale);
        AnimationUtils.drawFadeText(context, textRenderer, text, textX, textY, alpha);
        matrixStack.pop();
    }

    public static void drawBoldText(DrawContext context, TextRenderer renderer, String string, int i, int j, int color, int bordercolor) {
        context.drawText(renderer, string, i+1, j, bordercolor, false);
        context.drawText(renderer, string, i-1, j, bordercolor, false);
        context.drawText(renderer, string, i, j+1, bordercolor, false);
        context.drawText(renderer, string, i, j-1, bordercolor, false);
        context.drawText(renderer, string, i, j, color, false);
    }

    public static void drawBorder(DrawContext context, int color, int thickness) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        // Top border
        context.fill(0, 0, width, thickness, color);
        // Bottom border
        context.fill(0, height - thickness, width, height, color);
        // Left border
        context.fill(0, 0, thickness, height, color);
        // Right border
        context.fill(width - thickness, 0, width, height, color);
    }

    public static void renderVerticalLine(DrawContext context, int positionX, int positionY, int height, int thickness, int z, int color) {
        context.fill(positionX, positionY, positionX + thickness, positionY + height, z, color);
    }

    public static void renderHorizontalLine(DrawContext context, int positionX, int positionY, int width, int thickness, int z, int color) {
        context.fill(positionX, positionY, positionX + width, positionY + thickness, z, color);
    }

    // Fill the entire screen
    public static void fillScreen(DrawContext context, int red, int green, int blue, int alpha) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int color = ColorUtils.rgbaToHex(red, green, blue, alpha);
        context.fill(0, 0, width, height, color);
    }

    public static void fillScreen(DrawContext context, int red, int green, int blue) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int color = ColorUtils.fromRgb(red, green, blue);
        context.fill(0, 0, width, height, color);
    }

    public static void fillScreen(DrawContext context, int color) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        context.fill(0, 0, width, height, color);
    }

    public static void drawRect(DrawContext context, int contentX, int contentY, int contentWidth, int contentHeight, int color) {
        context.fill(contentX, contentY, contentX + contentWidth, contentY + contentHeight, color);
    }

    // ===== OPTIMIZED VERTEX-BASED GRADIENT METHODS =====

    /**
     * Renders a vertical gradient using GPU vertex interpolation (top to bottom)
     * Much more efficient than pixel-by-pixel rendering
     */
    public static void renderVerticalGradient(DrawContext context, int x, int y, int width, int height, int topColor, int bottomColor) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Extract color components
        float topR = ((topColor >> 16) & 0xFF) / 255.0f;
        float topG = ((topColor >> 8) & 0xFF) / 255.0f;
        float topB = (topColor & 0xFF) / 255.0f;
        float topA = ((topColor >> 24) & 0xFF) / 255.0f;

        float bottomR = ((bottomColor >> 16) & 0xFF) / 255.0f;
        float bottomG = ((bottomColor >> 8) & 0xFF) / 255.0f;
        float bottomB = (bottomColor & 0xFF) / 255.0f;
        float bottomA = ((bottomColor >> 24) & 0xFF) / 255.0f;

        // Set up render state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // Create quad with vertex colors (GPU will interpolate)
        bufferBuilder.vertex(matrix, x + width, y, 0).color(topR, topG, topB, topA);
        bufferBuilder.vertex(matrix, x, y, 0).color(topR, topG, topB, topA);
        bufferBuilder.vertex(matrix, x, y + height, 0).color(bottomR, bottomG, bottomB, bottomA);
        bufferBuilder.vertex(matrix, x + width, y + height, 0).color(bottomR, bottomG, bottomB, bottomA);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    /**
     * Renders a horizontal gradient using GPU vertex interpolation (left to right)
     */
    public static void renderHorizontalGradient(DrawContext context, int x, int y, int width, int height, int leftColor, int rightColor) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Extract color components
        float leftR = ((leftColor >> 16) & 0xFF) / 255.0f;
        float leftG = ((leftColor >> 8) & 0xFF) / 255.0f;
        float leftB = (leftColor & 0xFF) / 255.0f;
        float leftA = ((leftColor >> 24) & 0xFF) / 255.0f;

        float rightR = ((rightColor >> 16) & 0xFF) / 255.0f;
        float rightG = ((rightColor >> 8) & 0xFF) / 255.0f;
        float rightB = (rightColor & 0xFF) / 255.0f;
        float rightA = ((rightColor >> 24) & 0xFF) / 255.0f;

        // Set up render state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // Create quad with vertex colors (GPU will interpolate)
        bufferBuilder.vertex(matrix, x + width, y, 0).color(rightR, rightG, rightB, rightA);
        bufferBuilder.vertex(matrix, x, y, 0).color(leftR, leftG, leftB, leftA);
        bufferBuilder.vertex(matrix, x, y + height, 0).color(leftR, leftG, leftB, leftA);
        bufferBuilder.vertex(matrix, x + width, y + height, 0).color(rightR, rightG, rightB, rightA);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    /**
     * Renders a horizontal center gradient (edge -> center -> edge) using two quads
     * This is much more efficient than the old pixel-by-pixel method
     */
    public static void renderHorizontalLineWithCenterGradient(DrawContext context, int x, int y, int width, int height, int z, int centerColor, int edgeColor) {
        int centerX = x + width / 2;

        // Left half: edge to center
        renderHorizontalGradient(context, x, y, width / 2, height, edgeColor, centerColor);

        // Right half: center to edge
        renderHorizontalGradient(context, centerX, y, width - width / 2, height, centerColor, edgeColor);
    }

    /**
     * Renders a horizontal center gradient with alpha blending
     */
    public static void renderHorizontalLineWithCenterGradient(DrawContext context, int x, int y, int width, int height, int z, int centerColor, int edgeColor, float alpha) {
        // Apply alpha to both colors
        int alphaCenterColor = applyAlpha(centerColor, alpha);
        int alphaEdgeColor = applyAlpha(edgeColor, alpha);

        renderHorizontalLineWithCenterGradient(context, x, y, width, height, z, alphaCenterColor, alphaEdgeColor);
    }

    /**
     * Renders a vertical center gradient (edge -> center -> edge) using two quads
     */
    public static void renderVerticalLineWithCenterGradient(DrawContext context, int x, int y, int width, int height, int z, int centerColor, int edgeColor) {
        int centerY = y + height / 2;

        // Top half: edge to center
        renderVerticalGradient(context, x, y, width, height / 2, edgeColor, centerColor);

        // Bottom half: center to edge
        renderVerticalGradient(context, x, centerY, width, height - height / 2, centerColor, edgeColor);
    }

    /**
     * Simple horizontal gradient - kept for backward compatibility but now uses efficient rendering
     */
    public static void renderSimpleHorizontalGradient(DrawContext context, int x, int y, int width, int height, int z, int centerColor, int edgeColor) {
        renderHorizontalLineWithCenterGradient(context, x, y, width, height, z, centerColor, edgeColor);
    }

    // ===== UTILITY METHODS =====

    /**
     * Apply alpha multiplier to a color
     */
    private static int applyAlpha(int color, float alpha) {
        int originalAlpha = (color >> 24) & 0xFF;
        int newAlpha = (int) (originalAlpha * alpha);
        return (color & 0x00FFFFFF) | (newAlpha << 24);
    }

    /**
     * Create a color with specific alpha
     */
    public static int colorWithAlpha(int rgb, int alpha) {
        return (rgb & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    /**
     * Blend two colors with a ratio
     */
    public static int blendColors(int color1, int color2, float ratio) {
        return ColorUtils.interpolateColor(color1, color2, ratio);
    }
}