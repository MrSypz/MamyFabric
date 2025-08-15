package com.sypztep.test;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class RotationBenchmark {
    public static void main(String[] args) {
        final int ITER = 1_000_000;
        float particleX = 1f, particleY = 2f, particleZ = 3f;
        float yaw = 45f, pitch = 30f;
        float scale = 0.03f;
        Quaternionf camQuat = new Quaternionf().rotateXYZ((float)Math.toRadians(pitch), (float)Math.toRadians(yaw), 0f);

        // แบบ 1
        long start1 = System.nanoTime();
        for (int i=0;i<ITER;i++) {
            Matrix4f m = new Matrix4f()
                    .translation(particleX, particleY, particleZ)
                    .rotateY((float)Math.toRadians(-yaw))
                    .rotateX((float)Math.toRadians(pitch))
                    .scale(-scale, -scale, scale);
        }
        long end1 = System.nanoTime();

        // แบบ 2
        long start2 = System.nanoTime();
        for (int i=0;i<ITER;i++) {
            Matrix4f m = new Matrix4f()
                    .translation(particleX, particleY, particleZ)
                    .rotate(camQuat)
                    .rotate((float)Math.PI, 0f,1f,0f)
                    .scale(scale, scale, scale);
        }
        long end2 = System.nanoTime();

        System.out.printf("Method 1: %.3f ms%n", (end1-start1)/1_000_000.0);
        System.out.printf("Method 2: %.3f ms%n", (end2-start2)/1_000_000.0);
    }
}
