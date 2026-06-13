package com.example.client.module.modules.movement;

import com.example.client.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Flight: Ermöglicht freies Fliegen in Survival.
 *
 * Strategie: Setzt jedes Tick die Y-Velocity und Gravity-Flag,
 * steuert Bewegung über Input-Keys.
 */
public class Flight extends Module {

    private static final float SPEED = 0.4f;

    public Flight() {
        super("Flight", "Freies Fliegen in Survival", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.setNoGravity(true);
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.setNoGravity(false);
            mc.player.setVelocity(Vec3d.ZERO);
        }
    }

    private void onTick(MinecraftClient mc) {
        if (!isEnabled()) return;
        if (mc.player == null) return;

        mc.player.setNoGravity(true);

        float yaw = mc.player.getYaw();
        double dx = 0, dy = 0, dz = 0;

        if (mc.options.forwardKey.isPressed()) {
            dx -= Math.sin(Math.toRadians(yaw)) * SPEED;
            dz += Math.cos(Math.toRadians(yaw)) * SPEED;
        }
        if (mc.options.backKey.isPressed()) {
            dx += Math.sin(Math.toRadians(yaw)) * SPEED;
            dz -= Math.cos(Math.toRadians(yaw)) * SPEED;
        }
        if (mc.options.leftKey.isPressed()) {
            dx -= Math.cos(Math.toRadians(yaw)) * SPEED;
            dz -= Math.sin(Math.toRadians(yaw)) * SPEED;
        }
        if (mc.options.rightKey.isPressed()) {
            dx += Math.cos(Math.toRadians(yaw)) * SPEED;
            dz += Math.sin(Math.toRadians(yaw)) * SPEED;
        }
        if (mc.options.jumpKey.isPressed())  dy =  SPEED;
        if (mc.options.sneakKey.isPressed()) dy = -SPEED;

        mc.player.setVelocity(dx, dy, dz);
    }
}
