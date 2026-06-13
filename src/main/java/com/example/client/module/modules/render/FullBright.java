package com.example.client.module.modules.render;

import com.example.client.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * FullBright setzt den Gamma-Wert auf 16.0 (maximale Helligkeit).
 * Beim Deaktivieren wird der Originalwert wiederhergestellt.
 */
public class FullBright extends Module {

    private double originalGamma = 1.0;

    public FullBright() {
        super("FullBright", "Maximale Helligkeit – kein Dunkel mehr", Category.RENDER);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null) return;
        originalGamma = mc.options.getGamma().getValue();
        mc.options.getGamma().setValue(16.0);
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null) return;
        mc.options.getGamma().setValue(originalGamma);
    }
}
