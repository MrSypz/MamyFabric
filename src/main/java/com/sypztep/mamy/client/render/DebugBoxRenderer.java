package com.sypztep.mamy.client.render;

import com.sypztep.mamy.ModConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DebugBoxRenderer {
    private static final List<DebugBox> BOXES = new ArrayList<>();

    public static void addBox(Box box, float r, float g, float b, float a, int ticks) {
        BOXES.add(new DebugBox(box, r, g, b, a, ticks));
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider consumers, Camera camera) {
        if (ModConfig.skillVisualDebug) return;
        if (BOXES.isEmpty()) return;

        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getDebugLineStrip(1));

        double camX = camera.getPos().x;
        double camY = camera.getPos().y;
        double camZ = camera.getPos().z;

        for (Iterator<DebugBox> it = BOXES.iterator(); it.hasNext();) {
            DebugBox dbg = it.next();

            Box shifted = dbg.box.offset(-camX, -camY, -camZ);
            renderBox(matrices, consumer, shifted, dbg.r, dbg.g, dbg.b, dbg.a);

            dbg.ticks--;
            if (dbg.ticks <= 0) it.remove();
        }
    }
    private static void renderBox(MatrixStack matrices, VertexConsumer consumer, Box box, float r, float g, float b, float a) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        double x1 = box.minX;
        double y1 = box.minY;
        double z1 = box.minZ;
        double x2 = box.maxX;
        double y2 = box.maxY;
        double z2 = box.maxZ;

        // Bottom square
        drawLine(matrix, consumer, x1, y1, z1, x2, y1, z1, r, g, b, a);
        drawLine(matrix, consumer, x2, y1, z1, x2, y1, z2, r, g, b, a);
        drawLine(matrix, consumer, x2, y1, z2, x1, y1, z2, r, g, b, a);
        drawLine(matrix, consumer, x1, y1, z2, x1, y1, z1, r, g, b, a);

        // Top square
        drawLine(matrix, consumer, x1, y2, z1, x2, y2, z1, r, g, b, a);
        drawLine(matrix, consumer, x2, y2, z1, x2, y2, z2, r, g, b, a);
        drawLine(matrix, consumer, x2, y2, z2, x1, y2, z2, r, g, b, a);
        drawLine(matrix, consumer, x1, y2, z2, x1, y2, z1, r, g, b, a);

        // Vertical edges
        drawLine(matrix, consumer, x1, y1, z1, x1, y2, z1, r, g, b, a);
        drawLine(matrix, consumer, x2, y1, z1, x2, y2, z1, r, g, b, a);
        drawLine(matrix, consumer, x2, y1, z2, x2, y2, z2, r, g, b, a);
        drawLine(matrix, consumer, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    private static void drawLine(Matrix4f matrix, VertexConsumer consumer,
                                 double x1, double y1, double z1,
                                 double x2, double y2, double z2,
                                 float r, float g, float b, float a) {
        consumer.vertex(matrix, (float)x1, (float)y1, (float)z1).color(r, g, b, a);
        consumer.vertex(matrix, (float)x2, (float)y2, (float)z2).color(r, g, b, a);
    }
    private static class DebugBox {
        Box box;
        float r, g, b, a;
        int ticks;

        DebugBox(Box box, float r, float g, float b, float a, int ticks) {
            this.box = box;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.ticks = ticks;
        }
    }
}
