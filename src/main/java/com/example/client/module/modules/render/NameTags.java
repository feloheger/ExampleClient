package com.example.client.module.modules.render;

import com.example.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class NameTags extends Module {

    public static NameTags INSTANCE;

    public NameTags() {
        super("NameTags", "Zeigt Namen und Distanz über Spielern an", Category.RENDER);
        INSTANCE = this;
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}

    public void render(MatrixStack ms, VertexConsumerProvider vcp, Camera camera, Vec3d camPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        TextRenderer tr = mc.textRenderer;

        mc.world.getPlayers().forEach(player -> {
            if (player == mc.player) return;

            Vec3d pos = player.getEntityPos();
            double relX = pos.x - camPos.x;
            double relY = pos.y + player.getHeight() + 0.3 - camPos.y;
            double relZ = pos.z - camPos.z;

            double dist = mc.player.distanceTo(player);
            String label = player.getName().getString() + " §7[" + (int) dist + "m]";

            ms.push();
            ms.translate(relX, relY, relZ);
            ms.multiply(camera.getRotation());

            float scale = 0.025f;
            ms.scale(-scale, -scale, scale);

            Matrix4f mat = ms.peek().getPositionMatrix();
            float textWidth = tr.getWidth(label) / 2f;

            tr.draw(label, -textWidth, 0, 0xFFFFFF, false, mat, vcp, TextRenderer.TextLayerType.SEE_THROUGH, 0x55000000, 0xF000F0);

            ms.pop();
        });
    }
}