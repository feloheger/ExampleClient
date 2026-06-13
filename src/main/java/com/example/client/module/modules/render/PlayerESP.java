package com.example.client.module.modules.render;

import com.example.client.module.Module;
import com.example.client.module.settings.*;
import com.example.client.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.awt.Color;

public class PlayerESP extends Module {

    public static PlayerESP INSTANCE;

    public final BoolSetting  showBox     = addSetting(new BoolSetting("Box",        "Wireframe-Box zeichnen",        true));
    public final BoolSetting  showFill    = addSetting(new BoolSetting("Fill",       "Gefüllte Box zeichnen",         true));
    public final BoolSetting  showTracers = addSetting(new BoolSetting("Tracers",    "Tracer-Linie zeichnen",         true));
    public final BoolSetting  teamColor   = addSetting(new BoolSetting("TeamColor",  "Teammates grün einfärben",      true));
    public final ColorSetting colorBox    = addSetting(new ColorSetting("BoxColor",  "Outline-Farbe",                 new Color(0, 200, 255, 255)));
    public final ColorSetting colorFill   = addSetting(new ColorSetting("FillColor", "Füll-Farbe",                    new Color(0, 200, 255, 40)));
    public final ColorSetting colorTeam   = addSetting(new ColorSetting("TeamColor2","Team-Farbe",                    new Color(0, 255, 100, 220)));

    public PlayerESP() {
        super("PlayerESP", "Zeigt alle Spieler durch Wände an", Category.RENDER);
        INSTANCE = this;
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}

    public void renderESP(MatrixStack ms, VertexConsumerProvider vcp, Vec3d camPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || player.isInvisible()) continue;

            Box bb = player.getBoundingBox().expand(0.05, 0, 0.05);

            boolean isTeam = teamColor.getValue() && isTeammate(mc.player, player);
            Color outline = isTeam ? colorTeam.getValue() : colorBox.getValue();
            Color fill = isTeam ? new Color(colorTeam.getValue().getRed(), colorTeam.getValue().getGreen(), colorTeam.getValue().getBlue(), 35) : colorFill.getValue();

            if (showFill.getValue())    RenderUtil.drawFilledBox(ms, vcp, bb, fill);
            if (showBox.getValue())     RenderUtil.drawBox(ms, vcp, bb, outline);

            // FIX: Tracer-Linie direkt zum Spieler-Entity zeichnen!
            if (showTracers.getValue()) {
                RenderUtil.drawTracer(ms, vcp, player, outline);
            }
        }
    }

    private boolean isTeammate(PlayerEntity self, PlayerEntity other) {
        if (self.getScoreboardTeam() == null) return false;
        return self.getScoreboardTeam().equals(other.getScoreboardTeam());
    }
}