package com.sypztep.mamy.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sypztep.mamy.common.util.ColorUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

public final class DrawContextUtils {

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
    /**
     * Fills the screen with three horizontal sections, each occupying
     * approximately one-third of the screen height.
     *
     * <p><b>Example Usage:</b></p>
     * <pre>{@code
     * public static void exampleHorizontal(DrawContext context) {
     * ScreenFiller.fillScreenHorizontalRatio(context,
     * 0xFF1E1E1E, // Top - dark gray
     * 0xFF4CAF50, // Middle - green
     * 0xFF2196F3  // Bottom - blue
     * );
     * }
     * }</pre>
     *
     * @param context    The drawing context providing screen dimensions and fill capabilities.
     * @param topColor   The ARGB color for the top one-third section of the screen.
     * @param middleColor The ARGB color for the middle one-third section of the screen.
     * @param bottomColor The ARGB color for the bottom one-third section of the screen.
     */
    public static void fillScreenHorizontalRatio(DrawContext context, int topColor, int middleColor, int bottomColor) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        int sectionHeight = height / 3;

        // Top section (1/3)
        context.fill(0, 0, width, sectionHeight, topColor);

        // Middle section (1/3)
        context.fill(0, sectionHeight, width, sectionHeight * 2, middleColor);

        // Bottom section (1/3)
        context.fill(0, sectionHeight * 2, width, height, bottomColor);
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
    /**
     * Fills the screen with a 3x3 grid of nine equal sections, each colored
     * according to the provided array of colors.
     *
     * <p>The colors are applied row by row, from left to right within each row.
     * The {@code colors} array must contain exactly 9 ARGB color integers.</p>
     *
     * <p><b>Example Usage:</b></p>
     * <pre>{@code
     * public static void exampleGrid(DrawContext context) {
     * int[] gridColors = {
     * 0xFF1E1E1E, 0xFF4CAF50, 0xFF2196F3, // Top row
     * 0xFFFF5722, 0xFFFFEB3B, 0xFF9C27B0, // Middle row
     * 0xFF795548, 0xFF607D8B, 0xFFFFC107  // Bottom row
     * };
     * ScreenFiller.fillScreenGridRatio(context, gridColors);
     * }
     * }</pre>
     *
     * @param context The drawing context providing screen dimensions and fill capabilities.
     * @param colors  An array of exactly 9 ARGB color integers for the 3x3 grid sections.
     * @throws IllegalArgumentException If the {@code colors} array does not contain exactly 9 elements.
     */
    public static void fillScreenGridRatio(DrawContext context, int[] colors) {
        if (colors.length != 9) {
            throw new IllegalArgumentException("Grid ratio requires exactly 9 colors");
        }

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        int sectionWidth = width / 3;
        int sectionHeight = height / 3;

        int colorIndex = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x1 = col * sectionWidth;
                int y1 = row * sectionHeight;
                int x2 = (col == 2) ? width : (col + 1) * sectionWidth;  // Handle remainder
                int y2 = (row == 2) ? height : (row + 1) * sectionHeight; // Handle remainder

                context.fill(x1, y1, x2, y2, colors[colorIndex++]);
            }
        }
    }
    /**
     * Fills the screen with sections based on custom ratios, either horizontally or vertically.
     *
     * <p>This method allows for flexible screen partitioning where each section's size
     * is proportional to its corresponding ratio value. The sum of all ratios
     * determines the total proportion.</p>
     *
     * <p><b>Example Usage (Horizontal 2:3:1):</b></p>
     * <pre>{@code
     * public static void exampleCustom(DrawContext context) {
     * int[] ratios = {2, 3, 1}; // 2:3:1 ratio
     * int[] colors = {0xFF1E1E1E, 0xFF4CAF50, 0xFF2196F3};
     * ScreenFiller.fillScreenCustomRatio(context, ratios, colors, true); // true = horizontal
     * }
     * }</pre>
     *
     * <p><b>Example Usage (Equal 1:1:1 Horizontal):</b></p>
     * <pre>{@code
     * public static void exampleEqual(DrawContext context) {
     * int[] ratios = {1, 1, 1}; // 1:1:1 ratio
     * int[] colors = {0xFF1E1E1E, 0xFF4CAF50, 0xFF2196F3};
     * ScreenFiller.fillScreenCustomRatio(context, ratios, colors, true); // true = horizontal
     * }
     * }</pre>
     *
     * @param context    The drawing context providing screen dimensions and fill capabilities.
     * @param ratios     An array of integers representing the proportional sizes of each section.
     * For example, {@code {1, 2, 1}} means the sections will have sizes
     * in a 1:2:1 proportion.
     * @param colors     An array of ARGB color integers, where each color corresponds
     * to a ratio in the {@code ratios} array. The length of this array
     * must match the length of the {@code ratios} array.
     * @param horizontal If {@code true}, the sections will be arranged horizontally;
     * otherwise, they will be arranged vertically.
     * @throws IllegalArgumentException If the {@code ratios} and {@code colors} arrays
     * do not have the same length.
     */
    public static void fillScreenCustomRatio(DrawContext context, int[] ratios, int[] colors, boolean horizontal) {
        if (ratios.length != colors.length) {
            throw new IllegalArgumentException("Ratios and colors arrays must have the same length");
        }

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        // Calculate total ratio
        int totalRatio = 0;
        for (int ratio : ratios) {
            totalRatio += ratio;
        }

        if (horizontal) {
            // Horizontal sections
            int currentY = 0;
            for (int i = 0; i < ratios.length; i++) {
                int sectionHeight = (height * ratios[i]) / totalRatio;
                int nextY = (i == ratios.length - 1) ? height : currentY + sectionHeight;

                context.fill(0, currentY, width, nextY, colors[i]);
                currentY = nextY;
            }
        } else {
            // Vertical sections
            int currentX = 0;
            for (int i = 0; i < ratios.length; i++) {
                int sectionWidth = (width * ratios[i]) / totalRatio;
                int nextX = (i == ratios.length - 1) ? width : currentX + sectionWidth;

                context.fill(currentX, 0, nextX, height, colors[i]);
                currentX = nextX;
            }
        }
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
    public static int applyAlpha(int color, float alpha) {
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