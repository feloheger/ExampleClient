package com.example.client.module.modules.gui;

import com.example.client.module.settings.BlockListSetting;
import net.minecraft.block.Block;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BlockSelectorScreen extends Screen {

    private final BlockListSetting setting;
    private final List<Block> allBlocks;
    private List<Block> filtered;

    private TextFieldWidget searchBox;
    private int scrollOffset = 0;

    private static final int ROW_HEIGHT  = 20;
    private static final int PANEL_WIDTH = 260;
    private static final int PADDING     = 8;
    private static final int SEARCH_H    = 20;
    private final Screen parent;

    public BlockSelectorScreen(BlockListSetting setting, Screen parent) {
        super(Text.literal("Block auswählen"));
        this.setting   = setting;
        this.parent    = parent;
        this.allBlocks = new ArrayList<>(Registries.BLOCK.stream().toList());
        this.filtered  = new ArrayList<>(allBlocks);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    protected void init() {
        int panelX = (width - PANEL_WIDTH) / 2;
        int searchY = 30;
        searchBox = new TextFieldWidget(
                this.textRenderer, panelX, searchY, PANEL_WIDTH, SEARCH_H,
                Text.literal("Suche..."));
        searchBox.setPlaceholder(Text.literal("Block suchen..."));
        searchBox.setChangedListener(this::onSearch);
        addDrawableChild(searchBox);
        scrollOffset = 0;
    }

    private void onSearch(String query) {
        scrollOffset = 0;
        String q = query.toLowerCase();
        filtered = allBlocks.stream()
                .filter(b -> blockName(b).toLowerCase().contains(q))
                .toList();
    }

    private String blockName(Block b) {
        Identifier id = Registries.BLOCK.getId(b);
        return id != null ? id.getPath() : "unknown";
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        int panelX = (width - PANEL_WIDTH) / 2;
        int listStartY = 30 + SEARCH_H + 6;
        int listH = height - listStartY - 10;
        int visibleRows = listH / ROW_HEIGHT;
        int end = Math.min(scrollOffset + visibleRows, filtered.size());

        String title = "Block-ESP — Blöcke auswählen";
        ctx.drawText(this.textRenderer, title,
                (width - this.textRenderer.getWidth(title)) / 2, 10, 0xFFFFFFFF, true);

        for (int i = scrollOffset; i < end; i++) {
            Block block = filtered.get(i);
            int rowY = listStartY + (i - scrollOffset) * ROW_HEIGHT;
            boolean active = setting.contains(block);
            int btnX = panelX + PANEL_WIDTH - 18;

            // Zeilen-Hintergrund NUR für diese eine Zeile, direkt vor ihrem Text
            ctx.fill(panelX - PADDING, rowY - 2, panelX + PANEL_WIDTH + PADDING, rowY + ROW_HEIGHT - 2, 0xFF1A1A1A);

            boolean hoverRow = mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH - 22
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (hoverRow) {
                ctx.fill(panelX, rowY, panelX + PANEL_WIDTH, rowY + ROW_HEIGHT, 0x33FFFFFF);
            }

            boolean hoverBtn = mouseX >= btnX && mouseX <= btnX + 14
                    && mouseY >= rowY + 3 && mouseY < rowY + ROW_HEIGHT - 3;
            ctx.fill(btnX, rowY + 3, btnX + 14, rowY + ROW_HEIGHT - 3,
                    hoverBtn ? 0xAA444444 : 0x66222222);

            ctx.drawText(this.textRenderer, blockName(block),
                    panelX + 2, rowY + 6, active ? 0xFF55FF88 : 0xFFCCCCCC, true);
            ctx.drawText(this.textRenderer, active ? "-" : "+",
                    btnX + 4, rowY + 6, active ? 0xFFFF5555 : 0xFF55FF55, true);
        }

        if (filtered.size() > visibleRows) {
            String hint = (scrollOffset + visibleRows) + " / " + filtered.size();
            ctx.drawText(this.textRenderer, hint,
                    (width - this.textRenderer.getWidth(hint)) / 2, height - 8, 0xFF888888, false);        }
    }
    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        int panelX = (width - PANEL_WIDTH) / 2;
        int listStartY = 30 + SEARCH_H + 6;
        int listH = height - listStartY - 10;
        int visibleRows = listH / ROW_HEIGHT;

        int end = Math.min(scrollOffset + visibleRows, filtered.size());
        for (int i = scrollOffset; i < end; i++) {
            Block block = filtered.get(i);
            int rowY = listStartY + (i - scrollOffset) * ROW_HEIGHT;
            int btnX = panelX + PANEL_WIDTH - 18;

            if (mouseX >= btnX && mouseX <= btnX + 14
                    && mouseY >= rowY + 3 && mouseY < rowY + ROW_HEIGHT - 3) {
                if (setting.contains(block)) setting.remove(block);
                else setting.add(block);
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double horizontalAmount, double verticalAmount) {
        int listH = height - (30 + SEARCH_H + 6) - 10;
        int visibleRows = listH / ROW_HEIGHT;
        int maxScroll = Math.max(0, filtered.size() - visibleRows);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount));
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }
}