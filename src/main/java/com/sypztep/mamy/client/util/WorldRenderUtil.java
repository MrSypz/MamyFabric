package com.sypztep.mamy.client.util;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

public class WorldRenderUtil {

    /**
     * Draw a box with standard color
     */
    public static void drawBox(MatrixStack matrices, VertexConsumer buffer, Box box, float r, float g, float b, float alpha) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        // All 6 faces
        // Bottom
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, alpha);

        // Top
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, alpha);

        // Sides (4 faces)
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, alpha);

        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, alpha);

        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, alpha);

        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, alpha);
    }
}