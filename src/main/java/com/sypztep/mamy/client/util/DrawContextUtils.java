package com.sypztep.mamy.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sypztep.mamy.Mamy;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
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

    // ===== SCALING UTILITIES =====

    /**
     * Executes a rendering operation with matrix scaling applied
     */
    public static void withScale(DrawContext context, float scale, int x, int y, Runnable renderOperation) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-x, -y, 0);

        renderOperation.run();

        context.getMatrices().pop();
    }

    /**
     * Executes a rendering operation with matrix scaling applied at origin
     */
    public static void withScaleAt(DrawContext context, float scale, int originX, int originY, Runnable renderOperation) {
        context.getMatrices().push();
        context.getMatrices().translate(originX, originY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);

        renderOperation.run();

        context.getMatrices().pop();
    }

    // ===== IMAGE SCALING MODES =====

    /**
     * Different ways to scale/fit images within a target area
     */
    public enum ImageScaleMode {
        STRETCH,      // Stretch to fit exactly (may distort aspect ratio)
        FIT,          // Scale to fit entirely within bounds (maintains aspect ratio, may have empty space)
        FILL,         // Scale to fill entire area (maintains aspect ratio, may crop edges)
        CENTER,       // No scaling, center the image (may crop or have empty space)
        TILE,         // Repeat the image to fill the area
        NINE_PATCH    // Scale edges and corners separately (for UI elements)
    }

    // ===== STATIC IMAGE BLENDING WITH SCALE MODES =====

    /**
     * Renders a static image with custom blend mode and scale mode
     */
    public static void renderBlendedImage(DrawContext context, int x, int y, int width, int height,
                                          Identifier texture, float alpha, BlendMode blendMode, ImageScaleMode scaleMode) {
        renderBlendedImage(context, x, y, width, height, texture, alpha, blendMode, scaleMode, 512, 512);
    }

    /**
     * Renders a static image with custom blend mode, scale mode, and known texture dimensions
     */
    public static void renderBlendedImage(DrawContext context, int x, int y, int width, int height,
                                          Identifier texture, float alpha, BlendMode blendMode, ImageScaleMode scaleMode,
                                          int textureWidth, int textureHeight) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Set up render state
        RenderSystem.enableBlend();
        BlendMode.applyBlendMode(blendMode);
        RenderSystem.depthMask(false);

        // Bind texture
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);

        // Calculate color with alpha
        int color = ((int)(alpha * 255) << 24) | 0xFFFFFF;

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        // Calculate UV coordinates based on scale mode
        UVCoords uvCoords = calculateUVCoords(scaleMode, width, height, textureWidth, textureHeight);

        // Render the quad with calculated UV coordinates
        bufferBuilder.vertex(matrix, x + width, y, 0).texture(uvCoords.u2, uvCoords.v1).color(color);
        bufferBuilder.vertex(matrix, x, y, 0).texture(uvCoords.u1, uvCoords.v1).color(color);
        bufferBuilder.vertex(matrix, x, y + height, 0).texture(uvCoords.u1, uvCoords.v2).color(color);
        bufferBuilder.vertex(matrix, x + width, y + height, 0).texture(uvCoords.u2, uvCoords.v2).color(color);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        // Restore render state
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    /**
     * Simplified method with default parameters
     */
    public static void renderBlendedImage(DrawContext context, int x, int y, int width, int height, Identifier texture, float alpha) {
        renderBlendedImage(context, x, y, width, height, texture, alpha, BlendMode.MULTIPLY_SCREEN_HYBRID, ImageScaleMode.STRETCH);
    }

    /**
     * Renders a static image with custom color tinting, blend mode, and scale mode
     */
    public static void renderBlendedImageWithTint(DrawContext context, int x, int y, int width, int height,
                                                  Identifier texture, int tintColor, BlendMode blendMode,
                                                  ImageScaleMode scaleMode, int textureWidth, int textureHeight) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Set up render state
        RenderSystem.enableBlend();
        BlendMode.applyBlendMode(blendMode);
        RenderSystem.depthMask(false);

        // Bind texture
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        // Calculate UV coordinates based on scale mode
        UVCoords uvCoords = calculateUVCoords(scaleMode, width, height, textureWidth, textureHeight);

        // Render with custom tint color
        bufferBuilder.vertex(matrix, x + width, y, 0).texture(uvCoords.u2, uvCoords.v1).color(tintColor);
        bufferBuilder.vertex(matrix, x, y, 0).texture(uvCoords.u1, uvCoords.v1).color(tintColor);
        bufferBuilder.vertex(matrix, x, y + height, 0).texture(uvCoords.u1, uvCoords.v2).color(tintColor);
        bufferBuilder.vertex(matrix, x + width, y + height, 0).texture(uvCoords.u2, uvCoords.v2).color(tintColor);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        // Restore render state
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    // ===== UV COORDINATE CALCULATION =====

    /**
     * Helper class to store UV coordinates
     */
    private static class UVCoords {
        float u1, v1, u2, v2;

        UVCoords(float u1, float v1, float u2, float v2) {
            this.u1 = u1;
            this.v1 = v1;
            this.u2 = u2;
            this.v2 = v2;
        }
    }

    /**
     * Calculates UV coordinates based on the scale mode
     */
    private static UVCoords calculateUVCoords(ImageScaleMode scaleMode, int targetWidth, int targetHeight,
                                              int textureWidth, int textureHeight) {
        switch (scaleMode) {
            case STRETCH:
                // Simply stretch the entire texture (0,0 to 1,1)
                return new UVCoords(0.0f, 0.0f, 1.0f, 1.0f);

            case FIT:
                // Scale uniformly to fit entirely within bounds
                float targetAspect = (float) targetWidth / targetHeight;
                float textureAspect = (float) textureWidth / textureHeight;

                if (textureAspect > targetAspect) {
                    // Texture is wider, fit by width
                    float scale = (float) targetWidth / textureWidth;
                    float scaledHeight = textureHeight * scale;
                    float vOffset = (targetHeight - scaledHeight) / (2.0f * targetHeight);
                    float vScale = scaledHeight / targetHeight;
                    return new UVCoords(0.0f, 0.0f, 1.0f, vScale);
                } else {
                    // Texture is taller, fit by height
                    float scale = (float) targetHeight / textureHeight;
                    float scaledWidth = textureWidth * scale;
                    float uOffset = (targetWidth - scaledWidth) / (2.0f * targetWidth);
                    float uScale = scaledWidth / targetWidth;
                    return new UVCoords(0.0f, 0.0f, uScale, 1.0f);
                }

            case FILL:
                // Scale uniformly to fill entire area (may crop)
                float targetAspectFill = (float) targetWidth / targetHeight;
                float textureAspectFill = (float) textureWidth / textureHeight;

                if (textureAspectFill > targetAspectFill) {
                    // Texture is wider, crop sides
                    float scale = (float) targetHeight / textureHeight;
                    float scaledWidth = textureWidth * scale;
                    float cropAmount = (scaledWidth - targetWidth) / scaledWidth;
                    float uOffset = cropAmount / 2.0f;
                    return new UVCoords(uOffset, 0.0f, 1.0f - uOffset, 1.0f);
                } else {
                    // Texture is taller, crop top/bottom
                    float scale = (float) targetWidth / textureWidth;
                    float scaledHeight = textureHeight * scale;
                    float cropAmount = (scaledHeight - targetHeight) / scaledHeight;
                    float vOffset = cropAmount / 2.0f;
                    return new UVCoords(0.0f, vOffset, 1.0f, 1.0f - vOffset);
                }

            case CENTER:
                // No scaling, center the image
                float uScale = Math.min(1.0f, (float) targetWidth / textureWidth);
                float vScale = Math.min(1.0f, (float) targetHeight / textureHeight);
                float uOffset = (1.0f - uScale) / 2.0f;
                float vOffset = (1.0f - vScale) / 2.0f;
                return new UVCoords(uOffset, vOffset, uOffset + uScale, vOffset + vScale);

            case TILE:
                // Repeat the texture to fill the area
                float uRepeat = (float) targetWidth / textureWidth;
                float vRepeat = (float) targetHeight / textureHeight;
                return new UVCoords(0.0f, 0.0f, uRepeat, vRepeat);

            case NINE_PATCH:
                // For now, treat as stretch (nine-patch needs more complex implementation)
                return new UVCoords(0.0f, 0.0f, 1.0f, 1.0f);

            default:
                return new UVCoords(0.0f, 0.0f, 1.0f, 1.0f);
        }
    }

    // ===== ANIMATED PORTAL EFFECTS (RENAMED FOR CLARITY) =====

    /**
     * Renders an animated scrolling texture effect (like End Portal)
     */
    public static void renderScrollingTexture(DrawContext context, int x, int y, int width, int height, Identifier texture, float time) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Set up render state for portal effect
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        // Bind the texture
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        // Calculate UV coordinates with time-based animation
        float u1 = time * 0.5f;
        float v1 = time * 0.3f;
        float u2 = u1 + (width / 32.0f);
        float v2 = v1 + (height / 32.0f);

        // Create animated quad with scrolling texture
        bufferBuilder.vertex(matrix, x + width, y, 0).texture(u2, v1);
        bufferBuilder.vertex(matrix, x, y, 0).texture(u1, v1);
        bufferBuilder.vertex(matrix, x, y + height, 0).texture(u1, v2);
        bufferBuilder.vertex(matrix, x + width, y + height, 0).texture(u2, v2);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        // Restore render state
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    /**
     * Renders a magical portal effect with multiple animated layers
     */
    public static void renderMagicalPortalEffect(DrawContext context, int x, int y, int width, int height,
                                                 Identifier skyTexture, float time, float alpha) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Bind texture once
        RenderSystem.setShaderTexture(0, Mamy.id("textures/vfx/dust_spark.png")); // SPARKLE
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.depthMask(false);

        // === LAYER 1: BASE SCREEN BLEND ===
        RenderSystem.enableBlend();
        BlendMode.applyBlendMode(BlendMode.SCREEN);
        renderAnimatedTextureLayer(matrix, x, y, width, height, time * 0.15f, time * 0.1f,
                width / 50.0f, height / 50.0f, (int)(alpha * 180) << 24 | 0xFFFFFF);

        RenderSystem.setShaderTexture(0, skyTexture);
        BlendMode.applyBlendMode(BlendMode.MULTIPLY_SCREEN_HYBRID);
        renderAnimatedTextureLayer(matrix, x, y, width, height, time * -0.25f, time * 0.2f,
                width / 35.0f, height / 35.0f, (int)(alpha * 120) << 24 | 0xAABBFF);

        BlendMode.applyBlendMode(BlendMode.MULTIPLY_SCREEN_HYBRID);

        renderAnimatedTextureLayer(matrix, x, y, width, height, time * 0.4f, time * -0.3f,
                width / 25.0f, height / 25.0f, (int)(alpha * 80) << 24 | 0x6688FF);

        // === LAYER 4: PURE SCREEN FOR FINAL GLOW ===
        BlendMode.applyBlendMode(BlendMode.SCREEN);
        renderAnimatedTextureLayer(matrix, x, y, width, height, time * -0.6f, time * 0.5f,
                width / 15.0f, height / 15.0f, (int)(alpha * 60) << 24 | 0x4466FF);

        // Restore render state
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    /**
     * Renders a single animated texture layer (renamed from renderPortalLayer)
     */
    private static void renderAnimatedTextureLayer(Matrix4f matrix, int x, int y, int width, int height,
                                                   float uOffset, float vOffset, float uScale, float vScale, int color) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        float u1 = uOffset;
        float v1 = vOffset;
        float u2 = u1 + uScale;
        float v2 = v1 + vScale;

        bufferBuilder.vertex(matrix, x + width, y, 0).texture(u2, v1).color(color);
        bufferBuilder.vertex(matrix, x, y, 0).texture(u1, v1).color(color);
        bufferBuilder.vertex(matrix, x, y + height, 0).texture(u1, v2).color(color);
        bufferBuilder.vertex(matrix, x + width, y + height, 0).texture(u2, v2).color(color);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
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
     * Renders a vertical gradient with an overlaid blended image
     */
    public static void renderVerticalGradientWithBlendedImage(DrawContext context, int x, int y, int width, int height,
                                                              int topColor, int bottomColor,
                                                              Identifier overlayTexture, float overlayAlpha,
                                                              BlendMode mode, ImageScaleMode scaleMode,
                                                              int textureWidth, int textureHeight) {
        // First, render the gradient
        renderVerticalGradient(context, x, y, width, height, topColor, bottomColor);

        // Then, render the blended image on top
        renderBlendedImage(context, x, y, width, height, overlayTexture, overlayAlpha,
                mode, scaleMode, textureWidth, textureHeight);
    }
    public static void renderVerticalGradientWithBlendedImage(DrawContext context, int x, int y, int width, int height,
                                                              int topColor, int bottomColor,
                                                              Identifier overlayTexture, float overlayAlpha,
                                                              ImageScaleMode scaleMode,
                                                              int textureWidth, int textureHeight) {
        renderVerticalGradientWithBlendedImage(context,x,y,width,height,topColor,bottomColor,overlayTexture,overlayAlpha,BlendMode.MULTIPLY_SCREEN_HYBRID,scaleMode,textureWidth,textureHeight);
    }

    /**
     * Simplified version with default scale mode
     */
    public static void renderVerticalGradientWithBlendedImage(DrawContext context, int x, int y, int width, int height,
                                                              int topColor, int bottomColor,
                                                              Identifier overlayTexture, float overlayAlpha) {
        renderVerticalGradientWithBlendedImage(context, x, y, width, height, topColor, bottomColor,
                overlayTexture, overlayAlpha, ImageScaleMode.FILL,256,256);
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

    // UTILITY //

    public static int darkenColor(int color, float factor) {
        int alpha = ColorHelper.Argb.getAlpha(color);
        int red = (int) (ColorHelper.Argb.getRed(color) * factor);
        int green = (int) (ColorHelper.Argb.getGreen(color) * factor);
        int blue = (int) (ColorHelper.Argb.getBlue(color) * factor);

        return ColorHelper.Argb.getArgb(alpha, red, green, blue);
    }
    public static float enhancedEaseInOut(float t) {
        if (t < 0.5f) {
            return 4.0f * t * t * t;
        } else {
            float p = 2.0f * t - 2.0f;
            return 1.0f + p * p * p / 2.0f;
        }
    }
}