package com.sypztep.mamy.client.render;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.util.function.Function;

public class VertexContext {
    private final MatrixStack matrices;
    private final VertexConsumerProvider vertexConsumers;

    public VertexContext(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        this.matrices = matrices;
        this.vertexConsumers = vertexConsumers;
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        this.fill(x1, y1, x2, y2, 0, color);
    }

    public void fill(int x1, int y1, int x2, int y2, int z, int color) {
        // Use a world-appropriate render layer instead of GUI
        this.fill(RenderLayer.getDebugQuads(), x1, y1, x2, y2, z, color);
    }

    public void fill(RenderLayer layer, int x1, int y1, int x2, int y2, int color) {
        this.fill(layer, x1, y1, x2, y2, 0, color);
    }

    public void fill(RenderLayer layer, int x1, int y1, int x2, int y2, int z, int color) {
        Matrix4f matrix4f = this.matrices.peek().getPositionMatrix();
        if (x1 > x2) {
            int i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 > y2) {
            int i = y1;
            y1 = y2;
            y2 = i;
        }

        VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(layer);
        vertexConsumer.vertex(matrix4f, (float)x1, (float)y1, (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)x1, (float)y2, (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)x2, (float)y2, (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)x2, (float)y1, (float)z).color(color);
    }

    public void fillGradient(int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
        this.fillGradient(startX, startY, endX, endY, 0, colorStart, colorEnd);
    }

    public void fillGradient(int startX, int startY, int endX, int endY, int z, int colorStart, int colorEnd) {
        this.fillGradient(RenderLayer.getDebugQuads(), startX, startY, endX, endY, colorStart, colorEnd, z);
    }

    public void fillGradient(RenderLayer layer, int startX, int startY, int endX, int endY, int colorStart, int colorEnd, int z) {
        VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(layer);
        this.fillGradient(vertexConsumer, startX, startY, endX, endY, z, colorStart, colorEnd);
    }

    private void fillGradient(VertexConsumer vertexConsumer, int startX, int startY, int endX, int endY, int z, int colorStart, int colorEnd) {
        Matrix4f matrix4f = this.matrices.peek().getPositionMatrix();
        vertexConsumer.vertex(matrix4f, (float)startX, (float)startY, (float)z).color(colorStart);
        vertexConsumer.vertex(matrix4f, (float)startX, (float)endY, (float)z).color(colorEnd);
        vertexConsumer.vertex(matrix4f, (float)endX, (float)endY, (float)z).color(colorEnd);
        vertexConsumer.vertex(matrix4f, (float)endX, (float)startY, (float)z).color(colorStart);
    }
    public void fillGradient(VertexConsumer consumer, Matrix4f matrix,
                             float x1, float y1, float z1, float r1, float g1, float b1, float a1,
                             float x2, float y2, float z2, float r2, float g2, float b2, float a2,
                             float x3, float y3, float z3, float r3, float g3, float b3, float a3,
                             float x4, float y4, float z4, float r4, float g4, float b4, float a4) {
        consumer.vertex(matrix, x1, y1, z1).color(r1, g1, b1, a1);
        consumer.vertex(matrix, x2, y2, z2).color(r2, g2, b2, a2);
        consumer.vertex(matrix, x3, y3, z3).color(r3, g3, b3, a3);
        consumer.vertex(matrix, x4, y4, z4).color(r4, g4, b4, a4);
    }
    /**
     * Draws a textured rectangle from a region in a texture.
     *
     * <p>The width and height of the region are the same as
     * the dimensions of the rectangle.
     */
    public void drawTexture(
            Function<Identifier, RenderLayer> renderLayers,
            Identifier sprite,
            int x,
            int y,
            float u,
            float v,
            int width,
            int height,
            int textureWidth,
            int textureHeight,
            int color
    ) {
        this.drawTexture(renderLayers, sprite, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color);
    }

    /**
     * Draws a textured rectangle from a region in a texture.
     *
     * <p>The width and height of the region are the same as
     * the dimensions of the rectangle.
     */
    public void drawTexture(
            Function<Identifier, RenderLayer> renderLayers,
            Identifier sprite,
            int x,
            int y,
            float u,
            float v,
            int width,
            int height,
            int textureWidth,
            int textureHeight
    ) {
        this.drawTexture(renderLayers, sprite, x, y, u, v, width, height, width, height, textureWidth, textureHeight);
    }

    /**
     * Draws a textured rectangle from a region in a 256x256 texture.
     *
     * <p>The Z coordinate of the rectangle is {@code 0}.
     *
     * <p>The width and height of the region are the same as
     * the dimensions of the rectangle.
     */
    public void drawTexture(
            Function<Identifier, RenderLayer> renderLayers,
            Identifier sprite,
            int x,
            int y,
            float u,
            float v,
            int width,
            int height,
            int regionWidth,
            int regionHeight,
            int textureWidth,
            int textureHeight
    ) {
        this.drawTexture(renderLayers, sprite, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight, -1);
    }

    /**
     * Draws a textured rectangle from a region in a texture.
     */
    public void drawTexture(
            Function<Identifier, RenderLayer> renderLayers,
            Identifier sprite,
            int x,
            int y,
            float u,
            float v,
            int width,
            int height,
            int regionWidth,
            int regionHeight,
            int textureWidth,
            int textureHeight,
            int color
    ) {
        this.drawTexturedQuad(
                renderLayers,
                sprite,
                x,
                x + width,
                y,
                y + height,
                -1, // Default z value of 0
                (u + 0.0F) / (float)textureWidth,
                (u + (float)regionWidth) / (float)textureWidth,
                (v + 0.0F) / (float)textureHeight,
                (v + (float)regionHeight) / (float)textureHeight,
                color
        );
    }

    /**
     * Draws a textured rectangle with specified z-depth
     *
     * <p>This version allows specifying a z-coordinate for world space rendering
     */
    public void drawTexture(
            Function<Identifier, RenderLayer> renderLayers,
            Identifier sprite,
            int x,
            int y,
            int z,
            float u,
            float v,
            int width,
            int height,
            int regionWidth,
            int regionHeight,
            int textureWidth,
            int textureHeight,
            int color
    ) {
        this.drawTexturedQuad(
                renderLayers,
                sprite,
                x,
                x + width,
                y,
                y + height,
                z,
                (u + 0.0F) / (float)textureWidth,
                (u + (float)regionWidth) / (float)textureWidth,
                (v + 0.0F) / (float)textureHeight,
                (v + (float)regionHeight) / (float)textureHeight,
                color
        );
    }

    /**
     * Draws a textured quad with color.
     *
     * @param renderLayers Function to get the render layer for a texture
     * @param sprite The texture identifier
     * @param x1 Left coordinate
     * @param x2 Right coordinate
     * @param y1 Top coordinate
     * @param y2 Bottom coordinate
     * @param u1 Left texture coordinate
     * @param u2 Right texture coordinate
     * @param v1 Top texture coordinate
     * @param v2 Bottom texture coordinate
     * @param color The color to tint the texture, or -1 for no tinting
     */
    private void drawTexturedQuad(
            Function<Identifier, RenderLayer> renderLayers,
            Identifier sprite,
            int x1,
            int x2,
            int y1,
            int y2,
            int z,
            float u1,
            float u2,
            float v1,
            float v2,
            int color
    ) {
        RenderLayer renderLayer = renderLayers.apply(sprite);
        Matrix4f matrix4f = this.matrices.peek().getPositionMatrix();
        VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(renderLayer);

        // Extract color components
        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;
        float a = 1.0F;

        if (color != -1) {
            r = (float)(color >> 16 & 255) / 255.0F;
            g = (float)(color >> 8 & 255) / 255.0F;
            b = (float)(color & 255) / 255.0F;
            a = (float)(color >> 24 & 255) / 255.0F;

            // If alpha is 0 (default for -1), set it to 1.0
            if ((color >> 24 & 255) == 0) {
                a = 1.0F;
            }
        }

        // Check if the vertex format requires normals
        if (renderLayer.getVertexFormat().equals(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL)) {
            // For formats that need position, color, texture, light, normal
            vertexConsumer.vertex(matrix4f, (float)x1, (float)y1, (float)z)
                    .color(r, g, b, a)
                    .texture(u1, v1)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    .normal(0.0F, 0.0F, 1.0F)
                    ;
            vertexConsumer.vertex(matrix4f, (float)x1, (float)y2, (float)z)
                    .color(r, g, b, a)
                    .texture(u1, v2)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    .normal(0.0F, 0.0F, 1.0F)
                    ;
            vertexConsumer.vertex(matrix4f, (float)x2, (float)y2, (float)z)
                    .color(r, g, b, a)
                    .texture(u2, v2)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    .normal(0.0F, 0.0F, 1.0F)
                    ;
            vertexConsumer.vertex(matrix4f, (float)x2, (float)y1, (float)z)
                    .color(r, g, b, a)
                    .texture(u2, v1)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    .normal(0.0F, 0.0F, 1.0F)
                    ;
        } else if (renderLayer.getVertexFormat().equals(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT)) {
            // For formats that need position, color, texture, light
            vertexConsumer.vertex(matrix4f, (float)x1, (float)y1, (float)z)
                    .color(r, g, b, a)
                    .texture(u1, v1)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    ;
            vertexConsumer.vertex(matrix4f, (float)x1, (float)y2, (float)z)
                    .color(r, g, b, a)
                    .texture(u1, v2)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    ;
            vertexConsumer.vertex(matrix4f, (float)x2, (float)y2, (float)z)
                    .color(r, g, b, a)
                    .texture(u2, v2)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    ;
            vertexConsumer.vertex(matrix4f, (float)x2, (float)y1, (float)z)
                    .color(r, g, b, a)
                    .texture(u2, v1)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    ;
        } else if (renderLayer.getVertexFormat().equals(VertexFormats.POSITION_TEXTURE_COLOR_LIGHT)) {
            // For formats that need position, texture, color, light (different order)
            vertexConsumer.vertex(matrix4f, (float)x1, (float)y1, (float)z)
                    .texture(u1, v1)
                    .color(r, g, b, a)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    ;
            vertexConsumer.vertex(matrix4f, (float)x1, (float)y2, (float)z)
                    .texture(u1, v2)
                    .color(r, g, b, a)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    ;
            vertexConsumer.vertex(matrix4f, (float)x2, (float)y2, (float)z)
                    .texture(u2, v2)
                    .color(r, g, b, a)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    ;
            vertexConsumer.vertex(matrix4f, (float)x2, (float)y1, (float)z)
                    .texture(u2, v1)
                    .color(r, g, b, a)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    ;
        } else {
            // For basic position + texture + color format
            vertexConsumer.vertex(matrix4f, (float)x1, (float)y1, (float)z)
                    .texture(u1, v1)
                    .color(r, g, b, a)
                    ;
            vertexConsumer.vertex(matrix4f, (float)x1, (float)y2, (float)z)
                    .texture(u1, v2)
                    .color(r, g, b, a)
                    ;
            vertexConsumer.vertex(matrix4f, (float)x2, (float)y2, (float)z)
                    .texture(u2, v2)
                    .color(r, g, b, a)
                    ;
            vertexConsumer.vertex(matrix4f, (float)x2, (float)y1, (float)z)
                    .texture(u2, v1)
                    .color(r, g, b, a)
                    ;
        }
    }
}