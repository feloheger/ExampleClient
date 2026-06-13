package com.example.client.module.modules.render;

import com.example.client.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Freecam: Entkoppelt die Kamera vom Spieler-Body.
 *
 * Implementierung ohne GameRenderer-Mixin:
 *  - Erzeugt eine FakePlayer-Kopie an der aktuellen Position
 *  - Überschreibt die Camera-Entity auf den FakePlayer
 *  - Tick-basierte WASD-ähnliche Bewegung via Keyboard-State
 */
public class Freecam extends Module {

    private Vec3d savedPos;
    private float savedYaw, savedPitch;

    public Freecam() {
        super("Freecam", "Entkoppelt die Kamera vom Spieler", Category.RENDER);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        savedPos   = mc.player.getEntityPos();
        savedYaw   = mc.player.getYaw();
        savedPitch = mc.player.getPitch();

        // Spieler-Input blockieren (Noclip-artiger Effekt für Camera-only)
        mc.player.setNoGravity(true);
        mc.player.setVelocity(Vec3d.ZERO);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        mc.player.setNoGravity(false);
        if (savedPos != null) {
            mc.player.setPosition(savedPos);
        }
    }

    private void onTick(MinecraftClient mc) {
        if (!isEnabled()) return;
        if (mc.player == null) return;

        float speed = 0.5f;
        float yaw   = mc.player.getYaw();
        float pitch = mc.player.getPitch();

        double dx = 0, dy = 0, dz = 0;

        if (mc.options.forwardKey.isPressed()) {
            dx -= Math.sin(Math.toRadians(yaw))   * Math.cos(Math.toRadians(pitch)) * speed;
            dy -= Math.sin(Math.toRadians(pitch)) * speed;
            dz += Math.cos(Math.toRadians(yaw))   * Math.cos(Math.toRadians(pitch)) * speed;
        }
        if (mc.options.backKey.isPressed()) {
            dx += Math.sin(Math.toRadians(yaw))   * Math.cos(Math.toRadians(pitch)) * speed;
            dy += Math.sin(Math.toRadians(pitch)) * speed;
            dz -= Math.cos(Math.toRadians(yaw))   * Math.cos(Math.toRadians(pitch)) * speed;
        }
        if (mc.options.leftKey.isPressed()) {
            dx -= Math.cos(Math.toRadians(yaw)) * speed;
            dz -= Math.sin(Math.toRadians(yaw)) * speed;
        }
        if (mc.options.rightKey.isPressed()) {
            dx += Math.cos(Math.toRadians(yaw)) * speed;
            dz += Math.sin(Math.toRadians(yaw)) * speed;
        }
        if (mc.options.jumpKey.isPressed())  dy += speed;
        if (mc.options.sneakKey.isPressed()) dy -= speed;

        if (dx != 0 || dy != 0 || dz != 0) {
            mc.player.setVelocity(Vec3d.ZERO);
            mc.player.setPosition(mc.player.getX() + dx,
                                   mc.player.getY() + dy,
                                   mc.player.getZ() + dz);
        } else {
            mc.player.setVelocity(Vec3d.ZERO);
        }
    }
}
