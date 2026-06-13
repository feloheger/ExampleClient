package com.example.client.module.modules.render;

import com.example.client.module.Module;
import com.example.client.module.settings.*;
import com.example.client.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.awt.Color;

public class VehicleESP extends Module {

    public static VehicleESP INSTANCE;

    public final BoolSetting  showBoats     = addSetting(new BoolSetting("Boats",      "Boote anzeigen",          true));
    public final BoolSetting  showMinecarts = addSetting(new BoolSetting("Minecarts",  "Loren anzeigen",          true));
    public final BoolSetting  showBox       = addSetting(new BoolSetting("Box",        "Wireframe-Box zeichnen",  true));
    public final BoolSetting  showFill      = addSetting(new BoolSetting("Fill",       "Gefüllte Box zeichnen",   true));
    public final BoolSetting  showTracers   = addSetting(new BoolSetting("Tracers",    "Tracer-Linien zeichnen",  false));
    public final ColorSetting colorBoat     = addSetting(new ColorSetting("BoatColor", "Boot-Farbe",              new Color(120, 80,  40,  255)));
    public final ColorSetting colorCart     = addSetting(new ColorSetting("CartColor", "Loren-Farbe",             new Color(160, 160, 160, 255)));

    public VehicleESP() {
        super("VehicleESP", "Zeigt Boote und Loren durch Wände an", Category.RENDER);
        INSTANCE = this;
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}

    public void render(MatrixStack ms, VertexConsumerProvider vcp, Vec3d camPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        mc.world.getEntities().forEach(e -> {
            Color c = null;
            if (e instanceof BoatEntity && showBoats.getValue())     c = colorBoat.getValue();
            if (e instanceof MinecartEntity && showMinecarts.getValue()) c = colorCart.getValue();
            if (c == null) return;

            // FIX: Absolute Welt-Box direkt übergeben, KEIN Kamera-Offset!
            Box bb = e.getBoundingBox().expand(0.05, 0.05, 0.05);
            Color f = new Color(c.getRed(), c.getGreen(), c.getBlue(), 30);

            if (showFill.getValue())    RenderUtil.drawFilledBox(ms, vcp, bb, f);
            if (showBox.getValue())     RenderUtil.drawBox(ms, vcp, bb, c);
        });
    }
}