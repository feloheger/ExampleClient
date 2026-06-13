package com.example.client.module.modules.gui;

import com.example.client.module.Module;
import com.example.client.module.settings.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.Registries;

import java.util.List;

/**
 * ModuleButton mit ausklappbaren Settings.
 * Klick auf den Button → toggle Modul.
 * Klick auf ▶ rechts → Settings ausklappen.
 */
public class ModuleButton {

    private final Module module;
    private int x, y, width;
    private static final int H        = 14;
    private static final int SETTING_H = 13;

    private static final int COL_ON      = 0xFF1A8CFF;
    private static final int COL_OFF     = 0xFF2A2A2A;
    private static final int COL_HOVER   = 0xFF3A3A3A;
    private static final int COL_SETTING = 0xFF222222;
    private static final int COL_TEXT    = 0xFFFFFFFF;
    private static final int COL_SUBTEXT = 0xFFAAAAAA;

    private boolean hovered      = false;
    private boolean expanded     = false;

    // Slider-Drag State
    private SliderSetting draggingSlider = null;

    public ModuleButton(Module module, int x, int y, int width) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
    }

    // ── Render ────────────────────────────────────────────────────────────────

    public void render(DrawContext ctx, TextRenderer tr, int mouseX, int mouseY) {
        hovered = mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+H;

        int bg = module.isEnabled() ? COL_ON : (hovered ? COL_HOVER : COL_OFF);
        ctx.fill(x, y, x+width, y+H, bg);
        if (module.isEnabled()) ctx.fill(x, y, x+2, y+H, 0xFF00CFFF);

        ctx.drawText(tr, module.getName(), x+5, y+(H-8)/2, module.isEnabled() ? COL_TEXT : COL_SUBTEXT, false);

        // Expand-Arrow rechts
        if (!module.getSettings().isEmpty()) {
            String arrow = expanded ? "▼" : "▶";
            ctx.drawText(tr, arrow, x+width-10, y+(H-8)/2, 0xFF888888, false);
        }

        // Settings
        if (expanded) renderSettings(ctx, tr, mouseX, mouseY);
    }

    private void renderSettings(DrawContext ctx, TextRenderer tr, int mouseX, int mouseY) {
        List<Setting<?>> settings = module.getSettings();
        int sy = y + H;
        for (Setting<?> s : settings) {
            ctx.fill(x, sy, x+width, sy+SETTING_H, COL_SETTING);
            ctx.fill(x, sy, x+1, sy+SETTING_H, 0xFF444444);

            if (s instanceof BoolSetting bs) {
                ctx.drawText(tr, s.getName(), x+5, sy+2, COL_SUBTEXT, false);
                String val = bs.getValue() ? "§aON" : "§cOFF";
                ctx.drawText(tr, val, x+width-26, sy+2, COL_TEXT, false);

            } else if (s instanceof SliderSetting ss) {
                ctx.drawText(tr, s.getName(), x+5, sy+2, COL_SUBTEXT, false);
                // Slider-Bar
                int barX = x+5, barW = width-10, barY = sy+SETTING_H-4;
                ctx.fill(barX, barY, barX+barW, barY+2, 0xFF444444);
                float pct = (float)((ss.getValue()-ss.getMin())/(ss.getMax()-ss.getMin()));
                ctx.fill(barX, barY, barX+(int)(barW*pct), barY+2, 0xFF1A8CFF);
                String valStr = String.format("%.1f", ss.getValue());
                ctx.drawText(tr, valStr, x+width-tr.getWidth(valStr)-4, sy+2, COL_TEXT, false);

            } else if (s instanceof ColorSetting cs) {
                ctx.drawText(tr, s.getName(), x+5, sy+2, COL_SUBTEXT, false);
                java.awt.Color c = cs.getValue();
                int argb = (c.getAlpha()<<24)|(c.getRed()<<16)|(c.getGreen()<<8)|c.getBlue();
                ctx.fill(x+width-14, sy+2, x+width-4, sy+SETTING_H-2, 0xFF000000|argb);

            } else if (s instanceof BlockListSetting bls) {
                ctx.drawText(tr, s.getName() + ": " + bls.getValue().size() + " Blöcke",
                             x+5, sy+2, COL_SUBTEXT, false);
            }
            sy += SETTING_H;
        }
    }

    // ── Clicks ────────────────────────────────────────────────────────────────

    public boolean mouseClicked(double mx, double my, int button) {
        // Expand-Toggle
        if (!module.getSettings().isEmpty()
                && mx >= x+width-14 && mx <= x+width && my >= y && my <= y+H) {
            expanded = !expanded;
            return true;
        }
        // Module toggle
        if (mx >= x && mx <= x+width && my >= y && my <= y+H) {
            if (button == 0) module.toggle();
            return true;
        }
        // Settings
        if (expanded) return handleSettingClick(mx, my, button);
        return false;
    }

    private boolean handleSettingClick(double mx, double my, int button) {
        List<Setting<?>> settings = module.getSettings();
        int sy = y + H;
        for (Setting<?> s : settings) {
            if (my >= sy && my <= sy+SETTING_H) {
                if (s instanceof BoolSetting bs && button == 0) {
                    bs.toggle();
                    return true;
                }
                if (s instanceof SliderSetting ss) {
                    int barX = x+5, barW = width-10;
                    if (mx >= barX && mx <= barX+barW) {
                        double pct = (mx-barX)/barW;
                        ss.setValue(ss.getMin() + pct*(ss.getMax()-ss.getMin()));
                        draggingSlider = ss;
                        return true;
                    }
                }
                // ← NEU
                if (s instanceof BlockListSetting bls && button == 0) {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    mc.execute(() -> mc.setScreen(new BlockSelectorScreen(bls, mc.currentScreen)));
                    return true;
                }
            }
            sy += SETTING_H;
        }
        return false;
    }

    public void mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingSlider == null || !expanded) return;
        int barX = x+5, barW = width-10;
        double pct = Math.max(0, Math.min(1, (mx-barX)/barW));
        draggingSlider.setValue(draggingSlider.getMin() + pct*(draggingSlider.getMax()-draggingSlider.getMin()));
    }

    public void mouseReleased(double mx, double my, int button) {
        draggingSlider = null;
    }

    // ── Geometry ──────────────────────────────────────────────────────────────

    public void setPosition(int x, int y) { this.x = x; this.y = y; }

    public int getTotalHeight() {
        if (!expanded) return H;
        return H + module.getSettings().size() * SETTING_H;
    }

    public Module getModule() { return module; }
}
