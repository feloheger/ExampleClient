package com.example.client.module.modules.render;

import com.example.client.module.Module;
import com.example.client.module.settings.*;
import com.example.client.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.awt.Color;

public class MobESP extends Module {

    public static MobESP INSTANCE;

    public final BoolSetting  showHostile       = addSetting(new BoolSetting("Hostile",       "Feindliche Mobs anzeigen",      true));
    public final BoolSetting  showPassive        = addSetting(new BoolSetting("Passive",       "Passive Mobs anzeigen",         true));
    public final BoolSetting  showBox            = addSetting(new BoolSetting("Box",           "Wireframe-Box zeichnen",        true));
    public final BoolSetting  showFill           = addSetting(new BoolSetting("Fill",          "Gefüllte Box zeichnen",         true));
    public final BoolSetting  showTracers        = addSetting(new BoolSetting("Tracers",       "Tracer-Linien zeichnen",        false));
    public final ColorSetting colorHostile       = addSetting(new ColorSetting("HostileColor", "Farbe feindliche Mobs",         new Color(255, 60,  60,  255)));
    public final ColorSetting colorPassive       = addSetting(new ColorSetting("PassiveColor", "Farbe passive Mobs",            new Color(60,  255, 60,  255)));

    public MobESP() {
        super("MobESP", "Zeigt Mobs durch Wände an", Category.RENDER);
        INSTANCE = this;
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}

    public void render(MatrixStack ms, VertexConsumerProvider vcp, Vec3d camPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        mc.world.getEntities().forEach(e -> {
            if (!(e instanceof MobEntity)) return;
            boolean hostile = e instanceof HostileEntity;
            boolean passive = e instanceof PassiveEntity;
            if (hostile && !showHostile.getValue()) return;
            if (passive && !showPassive.getValue()) return;
            if (!hostile && !passive) return;

            Color c = hostile ? colorHostile.getValue() : colorPassive.getValue();
            Color f = new Color(c.getRed(), c.getGreen(), c.getBlue(), 30);

            Box absoluteBox = e.getBoundingBox().expand(0.05, 0, 0.05);
            Box bb = absoluteBox.offset(-camPos.x, -camPos.y, -camPos.z);

            if (showFill.getValue())    RenderUtil.drawFilledBox(ms, vcp, bb, f);
            if (showBox.getValue())     RenderUtil.drawBox(ms, vcp, bb, c);
        });
    }
}