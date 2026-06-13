package com.example.client.module.modules.render;

import com.example.client.module.Module;
import com.example.client.module.settings.*;
import com.example.client.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.awt.Color;

public class ItemESP extends Module {

    public static ItemESP INSTANCE;

    public final BoolSetting  showBox     = addSetting(new BoolSetting("Box",        "Wireframe-Box zeichnen",   true));
    public final BoolSetting  showFill    = addSetting(new BoolSetting("Fill",       "Gefüllte Box zeichnen",    true));
    public final BoolSetting  showTracers = addSetting(new BoolSetting("Tracers",    "Tracer-Linien zeichnen",   true));
    public final ColorSetting color       = addSetting(new ColorSetting("Color",     "ESP-Farbe",                new Color(255, 220, 0, 255)));

    public ItemESP() {
        super("ItemESP", "Zeigt liegende Items durch Wände an", Category.RENDER);
        INSTANCE = this;
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}

    public void render(MatrixStack ms, VertexConsumerProvider vcp, Vec3d camPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        Color c = color.getValue();
        Color f = new Color(c.getRed(), c.getGreen(), c.getBlue(), 35);
        mc.world.getEntities().forEach(e -> {
            if (!(e instanceof ItemEntity)) return;

            Box bb = e.getBoundingBox().expand(0.1, 0.1, 0.1);

            if (showFill.getValue())    RenderUtil.drawFilledBox(ms, vcp, bb, f);
            if (showBox.getValue())     RenderUtil.drawBox(ms, vcp, bb, c);

            // FIX: Tracer-Linie direkt zum liegenden Item-Entity zeichnen!
            if (showTracers.getValue()) {
                RenderUtil.drawTracer(ms, vcp, e, c);
            }
        });
    }
}