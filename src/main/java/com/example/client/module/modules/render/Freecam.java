package com.example.client.module.modules.render;

import com.example.client.ExampleClientMod;
import com.example.client.mixin.ICameraAccessor;
import com.example.client.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Freecam extends Module {

    // ---------------------------------------------------------------
    // LOKALER ZUSTAND
    // ---------------------------------------------------------------

    private static Freecam instance;

    /** Gespeicherte originale Spieler-Position zum Wiederherstellen */
    private Vec3d savedPos;
    private float savedYaw, savedPitch;

    /** Aktuelle Kamera-Position (frei beweglich) */
    private double camX, camY, camZ;
    private float camYaw, camPitch;

    /** Bewegungsgeschwindigkeit */
    private float speed = 1.0f;

    /** Verhindert doppelte Tick-Registrierung */
    private boolean tickRegistered = false;
    // In der Klasse:

    // Im Konstruktor:
    public Freecam() {
        super("Freecam", "Kamera frei durch die Welt bewegen", Category.RENDER);
        instance = this;

    }

    public static Freecam getInstance() {
        return instance;
    }

    // ---------------------------------------------------------------
    // MODUL EIN/AUS
    // ---------------------------------------------------------------

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Originale Position speichern
        savedPos   = mc.player.getEntityPos();
        savedYaw   = mc.player.getYaw();
        savedPitch = mc.player.getPitch();

        // Kamera startet an der Spieler-Position
        camX   = mc.player.getX();
        camY   = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
        camZ   = mc.player.getZ();
        camYaw   = savedYaw;
        camPitch = savedPitch;

        // Spieler einfrieren
        mc.player.setNoGravity(true);
        mc.player.setVelocity(Vec3d.ZERO);

        if (!tickRegistered) {
            ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
            tickRegistered = true;
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Spieler zurück an originale Position
        mc.player.setNoGravity(false);
        if (savedPos != null) {
            mc.player.setPosition(savedPos);
            mc.player.setYaw(savedYaw);
            mc.player.setPitch(savedPitch);
        }
    }

    // ---------------------------------------------------------------
    // TICK – KAMERA BEWEGEN (WASD + Space/Shift)
    // ---------------------------------------------------------------

    private void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;

        // Spieler eingefroren halten
        mc.player.setVelocity(Vec3d.ZERO);
        mc.player.noClip = true;
        mc.player.setNoGravity(true);

        // Kamera-Blickwinkel vom Spieler lesen
        camYaw   = mc.player.getYaw();
        camPitch = mc.player.getPitch();

        double maxDist = mc.options.getViewDistance().getValue() * 16.0;

        double dx = 0, dy = 0, dz = 0;

        if (mc.options.forwardKey.isPressed()) {
            dx -= Math.sin(Math.toRadians(camYaw)) * Math.cos(Math.toRadians(camPitch)) * speed;
            dy -= Math.sin(Math.toRadians(camPitch)) * speed;
            dz += Math.cos(Math.toRadians(camYaw)) * Math.cos(Math.toRadians(camPitch)) * speed;
        }
        if (mc.options.backKey.isPressed()) {
            dx += Math.sin(Math.toRadians(camYaw)) * Math.cos(Math.toRadians(camPitch)) * speed;
            dy += Math.sin(Math.toRadians(camPitch)) * speed;
            dz -= Math.cos(Math.toRadians(camYaw)) * Math.cos(Math.toRadians(camPitch)) * speed;
        }
        if (mc.options.leftKey.isPressed()) {
            dx += Math.cos(Math.toRadians(camYaw)) * speed;
            dz += Math.sin(Math.toRadians(camYaw)) * speed;
        }
        if (mc.options.rightKey.isPressed()) {
            dx -= Math.cos(Math.toRadians(camYaw)) * speed;
            dz -= Math.sin(Math.toRadians(camYaw)) * speed;
        }
        if (mc.options.jumpKey.isPressed())  dy += speed;
        if (mc.options.sneakKey.isPressed()) dy -= speed;

        double newX = camX + dx;
        double newY = camY + dy;
        double newZ = camZ + dz;

        // Render-Distanz-Grenze
        double distFromPlayer = Math.sqrt(
                Math.pow(newX - savedPos.x, 2) +
                        Math.pow(newY - savedPos.y, 2) +
                        Math.pow(newZ - savedPos.z, 2)
        );

        if (distFromPlayer <= maxDist) {
            camX = newX;
            camY = newY;
            camZ = newZ;
        }

        // Spieler-Entity an Kamera-Position setzen (Chunks laden)
        mc.player.setPosition(camX, camY, camZ);

        // Kamera manuell via Accessor positionieren
        net.minecraft.client.render.Camera camera = mc.gameRenderer.getCamera();
        if (camera != null) {
            ((ICameraAccessor) camera).invokeSetPos(camX, camY, camZ);

        }
    }

    // ---------------------------------------------------------------
    // NETZWERK-SCHUTZ
    // ---------------------------------------------------------------

    /**
     * Blockiert alle Move-Pakete solange Freecam aktiv.
     * Wird vom ClientCommonNetworkHandlerMixin aufgerufen.
     */
    public boolean shouldBlockPacket(net.minecraft.network.packet.Packet<?> packet) {
        if (!isEnabled()) return false;
        return packet instanceof net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
    }
}