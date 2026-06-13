package com.example.client.module.modules.gui;

import com.example.client.module.Module;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {

    private final Module.Category category;
    private final List<ModuleButton> buttons = new ArrayList<>();

    private int x, y;
    public static final int WIDTH    = 110;
    private static final int HEADER_H = 16;

    private static final int COL_HEADER = 0xFF0F0F0F;
    private static final int COL_ACCENT = 0xFF1A8CFF;
    private static final int COL_BG     = 0xDD1A1A1A;

    private boolean collapsed = false;

    public CategoryPanel(Module.Category category, int x, int y, List<Module> modules) {
        this.category = category;
        this.x = x;
        this.y = y;
        for (Module m : modules) {
            if (m.getCategory() == category)
                buttons.add(new ModuleButton(m, x, 0, WIDTH));
        }
    }

    public void render(DrawContext ctx, TextRenderer tr, int mouseX, int mouseY) {
        // Header
        ctx.fill(x, y, x+WIDTH, y+HEADER_H, COL_HEADER);
        ctx.fill(x, y, x+WIDTH, y+2, COL_ACCENT);
        ctx.drawText(tr, category.name(), x+5, y+(HEADER_H-8)/2, 0xFFFFFFFF, false);
        String collapseIcon = collapsed ? "+" : "-";
        ctx.drawText(tr, collapseIcon, x+WIDTH-10, y+(HEADER_H-8)/2, 0xFF888888, false);

        if (collapsed) return;

        // Background hinter Buttons
        ctx.fill(x, y+HEADER_H, x+WIDTH, y+HEADER_H+getContentHeight(), COL_BG);

        int btnY = y + HEADER_H;
        for (ModuleButton btn : buttons) {
            btn.setPosition(x, btnY);
            btn.render(ctx, tr, mouseX, mouseY);
            btnY += btn.getTotalHeight() + 1;
        }
    }

    public boolean mouseClicked(double mx, double my, int button) {
        // Header-Klick
        if (mx >= x && mx <= x+WIDTH && my >= y && my <= y+HEADER_H) {
            if (button == 0) collapsed = !collapsed;
            return true;
        }
        if (collapsed) return false;
        for (ModuleButton btn : buttons) {
            if (btn.mouseClicked(mx, my, button)) return true;
        }
        return false;
    }

    public void mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (collapsed) return;
        for (ModuleButton btn : buttons) btn.mouseDragged(mx, my, button, dx, dy);
    }

    public void mouseReleased(double mx, double my, int button) {
        for (ModuleButton btn : buttons) btn.mouseReleased(mx, my, button);
    }

    private int getContentHeight() {
        int h = 0;
        for (ModuleButton btn : buttons) h += btn.getTotalHeight() + 1;
        return h;
    }

    public int getTotalHeight() {
        return HEADER_H + (collapsed ? 0 : getContentHeight());
    }

    public static int getWidth() { return WIDTH; }
}
