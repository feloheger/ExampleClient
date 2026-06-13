package com.example.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.awt.Color;

public class RenderUtil {

    private static void flushBuffer(VertexConsumerProvider vertexConsumers) {
        if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }

    private static Vec3d getInterpolatedCameraPos() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.gameRenderer == null) return Vec3d.ZERO;
        return client.gameRenderer.getCamera().getCameraPos();
    }

    // Projiziert Punkte vor die Linse, fängt aber Rotationen und Bobbing über die Spieler-Blickrichtung ab
    private static Vec3d projectToFront(Vec3d targetPos, Vec3d camPos) {
        Vec3d dir = targetPos.subtract(camPos);
        double distance = dir.length();
        if (distance < 0.1) return dir;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return dir;

        // Nutzt die echten Rotationswinkel des Spielers (Garantierte Bobbing-Unabhängigkeit)
        Vec3d lookVec = Vec3d.fromPolar(client.player.getPitch(), client.player.getYaw());

        double targetDist = 0.2;
        double scale = targetDist / distance;

        return lookVec.multiply(targetDist).add(dir.subtract(lookVec.multiply(distance)).multiply(scale));
    }

    // ─── Tracer zu Entity ────────────────────────────────────────────────────
    public static void drawTracer(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                  Entity target, Color color) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || target == null) return;

        Vec3d actualCamPos = getInterpolatedCameraPos();
        double targetYOffset = (double) target.getHeight() / 2.0;
        Vec3d targetPos = target.getLerpedPos(client.getRenderTickCounter().getFixedDeltaTicks()).add(0, targetYOffset, 0);

        // Startpunkt stabilisieren
        Vec3d lookVec = Vec3d.fromPolar(client.player.getPitch(), client.player.getYaw()).multiply(0.2);
        float sx = (float) lookVec.x;
        float sy = (float) lookVec.y;
        float sz = (float) lookVec.z;

        Vec3d projectedTarget = projectToFront(targetPos, actualCamPos);
        float tx = (float) projectedTarget.x;
        float ty = (float) projectedTarget.y;
        float tz = (float) projectedTarget.z;

        drawTracerLine(matrices, vertexConsumers, sx, sy, sz, tx, ty, tz, color);
    }

    // ─── Tracer zu Block ─────────────────────────────────────────────────────
    public static void drawTracerToBlock(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                         BlockPos pos, Color color) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || pos == null) return;

        Vec3d actualCamPos = getInterpolatedCameraPos();
        Vec3d targetPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        Vec3d lookVec = Vec3d.fromPolar(client.player.getPitch(), client.player.getYaw()).multiply(0.2);
        float sx = (float) lookVec.x;
        float sy = (float) lookVec.y;
        float sz = (float) lookVec.z;

        Vec3d projectedTarget = projectToFront(targetPos, actualCamPos);
        float tx = (float) projectedTarget.x;
        float ty = (float) projectedTarget.y;
        float tz = (float) projectedTarget.z;

        drawTracerLine(matrices, vertexConsumers, sx, sy, sz, tx, ty, tz, color);
    }

    // ─── Tracer-Linie ────────────────────────────────────────────────────────
    private static void drawTracerLine(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                       float x1, float y1, float z1,
                                       float x2, float y2, float z2,
                                       Color color) {
        matrices.push();
        Matrix4f posMat = matrices.peek().getPositionMatrix();
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayers.lines());

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0) { dx /= len; dy /= len; dz /= len; } else { dy = 1f; }

        buffer.vertex(posMat, x1, y1, z1).color(r, g, b, a).lineWidth(1.0f).normal(dx, dy, dz);
        buffer.vertex(posMat, x2, y2, z2).color(r, g, b, a).lineWidth(1.0f).normal(dx, dy, dz);

        matrices.pop();
        flushBuffer(vertexConsumers);
    }

    // ─── Box Outline (Kanten-ESP) ────────────────────────────────────────────
    public static void drawBox(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                               Box box, Color color) {
        if (box == null) return;
        matrices.push();

        Vec3d camPos = getInterpolatedCameraPos();
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayers.lines());
        Matrix4f mat = matrices.peek().getPositionMatrix();

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        Vec3d p1 = projectToFront(new Vec3d(box.minX, box.minY, box.minZ), camPos);
        Vec3d p2 = projectToFront(new Vec3d(box.maxX, box.minY, box.minZ), camPos);
        Vec3d p3 = projectToFront(new Vec3d(box.maxX, box.minY, box.maxZ), camPos);
        Vec3d p4 = projectToFront(new Vec3d(box.minX, box.minY, box.maxZ), camPos);
        Vec3d p5 = projectToFront(new Vec3d(box.minX, box.maxY, box.minZ), camPos);
        Vec3d p6 = projectToFront(new Vec3d(box.maxX, box.maxY, box.minZ), camPos);
        Vec3d p7 = projectToFront(new Vec3d(box.maxX, box.maxY, box.maxZ), camPos);
        Vec3d p8 = projectToFront(new Vec3d(box.minX, box.maxY, box.maxZ), camPos);

        // Ring Unten
        line(buffer, mat, p1, p2, 1, 0, 0, r, g, b, a);
        line(buffer, mat, p2, p3, 0, 0, 1, r, g, b, a);
        line(buffer, mat, p3, p4, -1, 0, 0, r, g, b, a);
        line(buffer, mat, p4, p1, 0, 0, -1, r, g, b, a);

        // Ring Oben
        line(buffer, mat, p5, p6, 1, 0, 0, r, g, b, a);
        line(buffer, mat, p6, p7, 0, 0, 1, r, g, b, a);
        line(buffer, mat, p7, p8, -1, 0, 0, r, g, b, a);
        line(buffer, mat, p8, p5, 0, 0, -1, r, g, b, a);

        // Säulen
        line(buffer, mat, p1, p5, 0, 1, 0, r, g, b, a);
        line(buffer, mat, p2, p6, 0, 1, 0, r, g, b, a);
        line(buffer, mat, p3, p7, 0, 1, 0, r, g, b, a);
        line(buffer, mat, p4, p8, 0, 1, 0, r, g, b, a);

        matrices.pop();
        flushBuffer(vertexConsumers);
    }

    // ─── Box Filled (Gefülltes ESP) ──────────────────────────────────────────
    public static void drawFilledBox(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                     Box box, Color color) {
        if (box == null) return;
        matrices.push();

        Vec3d camPos = getInterpolatedCameraPos();
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayers.debugQuads());
        Matrix4f mat = matrices.peek().getPositionMatrix();

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        Vec3d p1 = projectToFront(new Vec3d(box.minX, box.minY, box.minZ), camPos);
        Vec3d p2 = projectToFront(new Vec3d(box.maxX, box.minY, box.minZ), camPos);
        Vec3d p3 = projectToFront(new Vec3d(box.maxX, box.minY, box.maxZ), camPos);
        Vec3d p4 = projectToFront(new Vec3d(box.minX, box.minY, box.maxZ), camPos);
        Vec3d p5 = projectToFront(new Vec3d(box.minX, box.maxY, box.minZ), camPos);
        Vec3d p6 = projectToFront(new Vec3d(box.maxX, box.maxY, box.minZ), camPos);
        Vec3d p7 = projectToFront(new Vec3d(box.maxX, box.maxY, box.maxZ), camPos);
        Vec3d p8 = projectToFront(new Vec3d(box.minX, box.maxY, box.maxZ), camPos);

        quad(buffer, mat, p1, p2, p3, p4, r, g, b, a);
        quad(buffer, mat, p5, p6, p7, p8, r, g, b, a);
        quad(buffer, mat, p1, p2, p6, p5, r, g, b, a);
        quad(buffer, mat, p4, p3, p7, p8, r, g, b, a);
        quad(buffer, mat, p1, p4, p8, p5, r, g, b, a);
        quad(buffer, mat, p2, p3, p7, p6, r, g, b, a);

        matrices.pop();
        flushBuffer(vertexConsumers);
    }

    private static void line(VertexConsumer vc, Matrix4f m, Vec3d start, Vec3d end,
                             float nx, float ny, float nz, float r, float g, float b, float a) {
        vc.vertex(m, (float)start.x, (float)start.y, (float)start.z).color(r, g, b, a).lineWidth(1.0f).normal(nx, ny, nz);
        vc.vertex(m, (float)end.x, (float)end.y, (float)end.z).color(r, g, b, a).lineWidth(1.0f).normal(nx, ny, nz);
    }

    private static void quad(VertexConsumer vc, Matrix4f m, Vec3d c1, Vec3d c2, Vec3d c3, Vec3d c4,
                             float r, float g, float b, float a) {
        vc.vertex(m, (float)c1.x, (float)c1.y, (float)c1.z).color(r, g, b, a);
        vc.vertex(m, (float)c2.x, (float)c2.y, (float)c2.z).color(r, g, b, a);
        vc.vertex(m, (float)c3.x, (float)c3.y, (float)c3.z).color(r, g, b, a);
        vc.vertex(m, (float)c4.x, (float)c4.y, (float)c4.z).color(r, g, b, a);
    }
}