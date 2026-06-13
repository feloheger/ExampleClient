package com.example.client.module.modules.render;

import com.example.client.module.Module;
import com.example.client.module.settings.*;
import com.example.client.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import java.awt.Color;

public class Tracers extends Module {

    public static Tracers INSTANCE;

    public final BoolSetting  tracePlayers = addSetting(new BoolSetting("Players",      "Tracers zu Spielern",         true));
    public final BoolSetting  traceHostile = addSetting(new BoolSetting("Hostile",      "Tracers zu feindl. Mobs",     true));
    public final ColorSetting colorPlayer  = addSetting(new ColorSetting("PlayerColor", "Spieler-Tracer Farbe",        new Color(0,   200, 255, 200)));
    public final ColorSetting colorHostile = addSetting(new ColorSetting("HostileColor","Mob-Tracer Farbe",            new Color(255, 60,  60,  200)));

    public Tracers() {
        super("Tracers", "Zeichnet Tracer-Linien zu Entities", Category.RENDER);
        INSTANCE = this;
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}

    public void render(MatrixStack ms, VertexConsumerProvider vcp, Vec3d camPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        // Wir holen uns eine Kopie der Liste, damit es während des Schleifen-Durchlaufs
        // durch neu spawnende Entities keine ConcurrentModificationException gibt.
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;

            if (e instanceof PlayerEntity && tracePlayers.getValue()) {
                RenderUtil.drawTracer(ms, vcp, e, colorPlayer.getValue());
            }
            else if (e instanceof HostileEntity && traceHostile.getValue()) {
                RenderUtil.drawTracer(ms, vcp, e, colorHostile.getValue());
            }
        }
    }
}