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
     */
    public static void renderVerticalGradient(DrawContext context, int x, int y, int width, int height, int topColor, int bottomColor) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        // Set up render state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // Create quad with vertex colors (GPU will interpolate)
        bufferBuilder.vertex(matrix, x + width, y, 0).color(topColor);
        bufferBuilder.vertex(matrix, x, y, 0).color(topColor);
        bufferBuilder.vertex(matrix, x, y + height, 0).color(bottomColor);
        bufferBuilder.vertex(matrix, x + width, y + height, 0).color(bottomColor);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    /**
     * Renders a horizontal gradient using GPU vertex interpolation (left to right)
     */
    public static void renderHorizontalGradient(DrawContext context, int x, int y, int width, int height, int leftColor, int rightColor) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Set up render state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // Create quad with vertex colors (GPU will interpolate)
        bufferBuilder.vertex(matrix, x + width, y, 0).color(rightColor);
        bufferBuilder.vertex(matrix, x, y, 0).color(leftColor);
        bufferBuilder.vertex(matrix, x, y + height, 0).color(leftColor);
        bufferBuilder.vertex(matrix, x + width, y + height, 0).color(rightColor);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    /**
     * OPTIMIZED: Renders a horizontal center gradient (edge -> center -> edge) using optimized approach
     * Uses a single BufferBuilder with proper vertex positioning for correct gradient
     */
    public static void renderHorizontalLineWithCenterGradient(DrawContext context, int x, int y, int width, int height, int z, int centerColor, int edgeColor) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Set up render state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        int centerX = x + width / 2;

        // Left half: edge -> center gradient
        bufferBuilder.vertex(matrix, centerX, y, z).color(centerColor);     // Center top
        bufferBuilder.vertex(matrix, x, y, z).color(edgeColor);                   // Left top
        bufferBuilder.vertex(matrix, x, y + height, z).color(edgeColor);          // Left bottom
        bufferBuilder.vertex(matrix, centerX, y + height, z).color(centerColor); // Center bottom

        // Right half: center -> edge gradient
        bufferBuilder.vertex(matrix, x + width, y, z).color(edgeColor);           // Right top
        bufferBuilder.vertex(matrix, centerX, y, z).color(centerColor);    // Center top
        bufferBuilder.vertex(matrix, centerX, y + height, z).color(centerColor); // Center bottom
        bufferBuilder.vertex(matrix, x + width, y + height, z).color(edgeColor); // Right bottom

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    /**
     * Optimized horizontal center gradient with alpha blending
     */
    public static void renderHorizontalLineWithCenterGradient(DrawContext context, int x, int y, int width, int height, int z, int centerColor, int edgeColor, float alpha) {
        // Apply alpha to both colors using ColorHelper
        int alphaCenterColor = ColorHelper.Argb.getArgb((int) (ColorHelper.Argb.getAlpha(centerColor) * alpha), ColorHelper.Argb.getRed(centerColor), ColorHelper.Argb.getGreen(centerColor), ColorHelper.Argb.getBlue(centerColor));
        int alphaEdgeColor = ColorHelper.Argb.getArgb((int) (ColorHelper.Argb.getAlpha(edgeColor) * alpha), ColorHelper.Argb.getRed(edgeColor), ColorHelper.Argb.getGreen(edgeColor), ColorHelper.Argb.getBlue(edgeColor));

        renderHorizontalLineWithCenterGradient(context, x, y, width, height, z, alphaCenterColor, alphaEdgeColor);
    }

    /**
     * OPTIMIZED: Renders a vertical center gradient (edge -> center -> edge) using optimized approach
     */
    public static void renderVerticalLineWithCenterGradient(DrawContext context, int x, int y, int width, int height, int z, int centerColor, int edgeColor) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Set up render state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        int centerY = y + height / 2;

        // Top half: edge -> center gradient
        bufferBuilder.vertex(matrix, x + width, centerY, 0).color(centerColor); // Center right
        bufferBuilder.vertex(matrix, x, centerY, 0).color(centerColor);         // Center left
        bufferBuilder.vertex(matrix, x, y, 0).color(edgeColor);                       // Top left
        bufferBuilder.vertex(matrix, x + width, y, 0).color(edgeColor);              // Top right

        // Bottom half: center -> edge gradient
        bufferBuilder.vertex(matrix, x + width, y + height, 0).color(edgeColor);     // Bottom right
        bufferBuilder.vertex(matrix, x, y + height, 0).color(edgeColor);             // Bottom left
        bufferBuilder.vertex(matrix, x, centerY, 0).color(centerColor);       // Center left
        bufferBuilder.vertex(matrix, x + width, centerY, 0).color(centerColor); // Center right

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }
}