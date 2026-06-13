package com.example.client.module.modules.render;

import com.example.client.module.Module;
import com.example.client.module.settings.*;
import com.example.client.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.awt.Color;

public class CrystalESP extends Module {

    public static CrystalESP INSTANCE;

    public final BoolSetting  showBox     = addSetting(new BoolSetting("Box",     "Wireframe-Box zeichnen",  true));
    public final BoolSetting  showFill    = addSetting(new BoolSetting("Fill",    "Gefüllte Box zeichnen",   true));
    public final BoolSetting  showTracers = addSetting(new BoolSetting("Tracers", "Tracer-Linien zeichnen",  false));
    public final ColorSetting color       = addSetting(new ColorSetting("Color",  "ESP-Farbe",               new Color(255, 100, 220, 255)));

    public CrystalESP() {
        super("CrystalESP", "Zeigt End-Kristalle durch Wände an", Category.RENDER);
        INSTANCE = this;
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}

    public void render(MatrixStack ms, VertexConsumerProvider vcp, Vec3d camPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        Color c = color.getValue();
        Color f = new Color(c.getRed(), c.getGreen(), c.getBlue(), 40);
        mc.world.getEntities().forEach(e -> {
            if (!(e instanceof EndCrystalEntity)) return;

            // FIX: Absolute Welt-Box direkt übergeben, KEIN Kamera-Offset!
            Box bb = e.getBoundingBox().expand(0.05, 0.05, 0.05);

            if (showFill.getValue())    RenderUtil.drawFilledBox(ms, vcp, bb, f);
            if (showBox.getValue())     RenderUtil.drawBox(ms, vcp, bb, c);
        });
    }
}