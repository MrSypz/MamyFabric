package com.sypztep.mamy.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;

public final class DrawContextUtils {

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
        int color = ColorHelper.Argb.getArgb(alpha, red, green, blue);
        context.fill(0, 0, width, height, color);
    }

    public static void fillScreen(DrawContext context, int red, int green, int blue) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int color = ColorHelper.Argb.getArgb(red, green, blue);
        context.fill(0, 0, width, height, color);
    }

    public static void fillScreen(DrawContext context, int color) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        context.fill(0, 0, width, height, color);
    }

    /**
     * Fills the screen with three vertical sections, each occupying
     * approximately one-third of the screen width.
     *
     * <p><b>Example Usage:</b></p>
     * <pre>{@code
     * public static void exampleVertical(DrawContext context) {
     * ScreenFiller.fillScreenVerticalRatio(context,
     * 0xFFFF5722, // Left - red
     * 0xFFFFEB3B, // Center - yellow
     * 0xFF9C27B0  // Right - purple
     * );
     * }
     * }</pre>
     *
     * @param context     The drawing context providing screen dimensions and fill capabilities.
     * @param leftColor   The ARGB color for the left one-third section of the screen.
     * @param centerColor The ARGB color for the center one-third section of the screen.
     * @param rightColor  The ARGB color for the right one-third section of the screen.
     */
    public static void fillScreenVerticalRatio(DrawContext context, int leftColor, int centerColor, int rightColor) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        int sectionWidth = width / 3;

        // Left section (1/3)
        context.fill(0, 0, sectionWidth, height, leftColor);

        // Center section (1/3)
        context.fill(sectionWidth, 0, sectionWidth * 2, height, centerColor);

        // Right section (1/3)
        context.fill(sectionWidth * 2, 0, width, height, rightColor);
    }

    public static void drawRect(DrawContext context, int contentX, int contentY, int contentWidth, int contentHeight, int color) {
        context.fill(contentX, contentY, contentX + contentWidth, contentY + contentHeight, color);
    }

    // ===== OPTIMIZED VERTEX-BASED GRADIENT METHODS =====

    /**
     * Renders a vertical gradient using GPU vertex interpolation (top to bottom)
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
     * OPTIMIZED: Renders a horizontal center gradient (edge -> center -> edge) using optimized approach
     * Uses a single BufferBuilder with proper vertex positioning for correct gradient
     */
    public static void renderHorizontalLineWithCenterGradient(DrawContext context, int x, int y, int width, int height, int z, int centerColor, int edgeColor) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Extract color components for edge color
        float edgeR = ((edgeColor >> 16) & 0xFF) / 255.0f;
        float edgeG = ((edgeColor >> 8) & 0xFF) / 255.0f;
        float edgeB = (edgeColor & 0xFF) / 255.0f;
        float edgeA = ((edgeColor >> 24) & 0xFF) / 255.0f;

        // Extract color components for center color
        float centerR = ((centerColor >> 16) & 0xFF) / 255.0f;
        float centerG = ((centerColor >> 8) & 0xFF) / 255.0f;
        float centerB = (centerColor & 0xFF) / 255.0f;
        float centerA = ((centerColor >> 24) & 0xFF) / 255.0f;

        // Set up render state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        int centerX = x + width / 2;

        // Left half: edge -> center gradient
        bufferBuilder.vertex(matrix, centerX, y, 0).color(centerR, centerG, centerB, centerA);     // Center top
        bufferBuilder.vertex(matrix, x, y, 0).color(edgeR, edgeG, edgeB, edgeA);                   // Left top
        bufferBuilder.vertex(matrix, x, y + height, 0).color(edgeR, edgeG, edgeB, edgeA);          // Left bottom
        bufferBuilder.vertex(matrix, centerX, y + height, 0).color(centerR, centerG, centerB, centerA); // Center bottom

        // Right half: center -> edge gradient
        bufferBuilder.vertex(matrix, x + width, y, 0).color(edgeR, edgeG, edgeB, edgeA);           // Right top
        bufferBuilder.vertex(matrix, centerX, y, 0).color(centerR, centerG, centerB, centerA);    // Center top
        bufferBuilder.vertex(matrix, centerX, y + height, 0).color(centerR, centerG, centerB, centerA); // Center bottom
        bufferBuilder.vertex(matrix, x + width, y + height, 0).color(edgeR, edgeG, edgeB, edgeA); // Right bottom

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    /**
     * Optimized horizontal center gradient with alpha blending
     */
    public static void renderHorizontalLineWithCenterGradient(DrawContext context, int x, int y, int width, int height, int z, int centerColor, int edgeColor, float alpha) {
        // Apply alpha to both colors
        int alphaCenterColor = applyAlpha(centerColor, alpha);
        int alphaEdgeColor = applyAlpha(edgeColor, alpha);

        renderHorizontalLineWithCenterGradient(context, x, y, width, height, z, alphaCenterColor, alphaEdgeColor);
    }

    /**
     * OPTIMIZED: Renders a vertical center gradient (edge -> center -> edge) using optimized approach
     */
    public static void renderVerticalLineWithCenterGradient(DrawContext context, int x, int y, int width, int height, int z, int centerColor, int edgeColor) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Extract color components for edge color
        float edgeR = ((edgeColor >> 16) & 0xFF) / 255.0f;
        float edgeG = ((edgeColor >> 8) & 0xFF) / 255.0f;
        float edgeB = (edgeColor & 0xFF) / 255.0f;
        float edgeA = ((edgeColor >> 24) & 0xFF) / 255.0f;

        // Extract color components for center color
        float centerR = ((centerColor >> 16) & 0xFF) / 255.0f;
        float centerG = ((centerColor >> 8) & 0xFF) / 255.0f;
        float centerB = (centerColor & 0xFF) / 255.0f;
        float centerA = ((centerColor >> 24) & 0xFF) / 255.0f;

        // Set up render state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.BLIT_SCREEN);

        int centerY = y + height / 2;

        // Top half: edge -> center gradient
        bufferBuilder.vertex(matrix, x + width, centerY, 0).color(centerR, centerG, centerB, centerA); // Center right
        bufferBuilder.vertex(matrix, x, centerY, 0).color(centerR, centerG, centerB, centerA);         // Center left
        bufferBuilder.vertex(matrix, x, y, 0).color(edgeR, edgeG, edgeB, edgeA);                       // Top left
        bufferBuilder.vertex(matrix, x + width, y, 0).color(edgeR, edgeG, edgeB, edgeA);              // Top right

        // Bottom half: center -> edge gradient
        bufferBuilder.vertex(matrix, x + width, y + height, 0).color(edgeR, edgeG, edgeB, edgeA);     // Bottom right
        bufferBuilder.vertex(matrix, x, y + height, 0).color(edgeR, edgeG, edgeB, edgeA);             // Bottom left
        bufferBuilder.vertex(matrix, x, centerY, 0).color(centerR, centerG, centerB, centerA);       // Center left
        bufferBuilder.vertex(matrix, x + width, centerY, 0).color(centerR, centerG, centerB, centerA); // Center right

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    // ===== UTILITY METHODS =====

    /**
     * Apply alpha multiplier to a color
     */
    public static int applyAlpha(int color, float alpha) {
        int originalAlpha = (color >> 24) & 0xFF;
        int newAlpha = (int) (originalAlpha * alpha);
        return (color & 0x00FFFFFF) | (newAlpha << 24);
    }
}