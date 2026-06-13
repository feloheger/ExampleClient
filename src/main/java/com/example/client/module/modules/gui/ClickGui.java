package com.example.client.module.modules.gui;

import com.example.client.module.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGui extends Screen {

    private final List<CategoryPanel> panels = new ArrayList<>();
    private static final int PADDING     = 5;
    private static final int START_Y     = 8;
    private static final int PANEL_WIDTH = CategoryPanel.WIDTH;
    private static final int COL_BG      = 0x88000000;

    public ClickGui(List<Module> modules) {
        super(Text.literal("ClickGUI"));
        int x = PADDING;
        for (Module.Category cat : Module.Category.values()) {
            panels.add(new CategoryPanel(cat, x, START_Y, modules));
            x += PANEL_WIDTH + PADDING;
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, COL_BG);
        for (CategoryPanel p : panels) {
            p.render(ctx, this.textRenderer, mouseX, mouseY);
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();
        for (CategoryPanel p : panels) {
            if (p.mouseClicked(mx, my, button)) return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseReleased(Click click) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();
        for (CategoryPanel p : panels) {
            p.mouseReleased(mx, my, button);
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();
        for (CategoryPanel p : panels) {
            p.mouseDragged(mx, my, button, dx, dy);
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
